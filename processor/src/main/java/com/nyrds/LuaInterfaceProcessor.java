package com.nyrds;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * Annotation processor for @LuaInterface.
 * Generates a JSON map of all classes with @LuaInterface annotations and their accessible members.
 * This map is used by TeaVM ReflectionSupplier and future runtime sandbox enforcement.
 */
@AutoService(Processor.class)
public class LuaInterfaceProcessor extends AbstractProcessor {

    private Messager messager;
    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private TypeElement luaInterfaceAnnotation;
    private boolean generated = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.luaInterfaceAnnotation = elementUtils.getTypeElement(LuaInterface.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (generated) {
            return true;
        }

        // Collect all elements annotated with @LuaInterface
        Map<TypeElement, ClassData> classDataMap = new HashMap<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(luaInterfaceAnnotation)) {
            TypeElement enclosingClass = getEnclosingClass(element);
            if (enclosingClass == null) {
                continue;
            }

            ClassData data = classDataMap.computeIfAbsent(enclosingClass, ClassData::new);

            switch (element.getKind()) {
                case CLASS:
                case INTERFACE:
                    data.hasClassAnnotation = true;
                    break;
                case METHOD: {
                    ExecutableElement method = (ExecutableElement) element;
                    if (method.getModifiers().contains(Modifier.PUBLIC)) {
                        data.methods.add(method.getSimpleName().toString());
                        // Collect return type and parameter types for transitive closure
                        data.referencedTypes.add(method.getReturnType());
                        for (VariableElement param : method.getParameters()) {
                            data.referencedTypes.add(param.asType());
                        }
                    }
                    break;
                }
                case FIELD: {
                    VariableElement field = (VariableElement) element;
                    if (field.getModifiers().contains(Modifier.PUBLIC)) {
                        data.fields.add(field.getSimpleName().toString());
                        // Collect field type for transitive closure
                        data.referencedTypes.add(field.asType());
                    }
                    break;
                }
                case CONSTRUCTOR: {
                    ExecutableElement constructor = (ExecutableElement) element;
                    if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
                        data.constructors.add("<init>");
                    }
                    break;
                }
                default:
                    // Ignore other element types (packages, modules, etc.)
                    break;
            }
        }

        // Filter: only keep classes that have @LuaInterface on the class itself
        // OR have at least one annotated member
        // Actually, we want all classes that have ANY @LuaInterface annotation
        // The ReflectionSupplier checks for class-level annotation, so we include both

        if (classDataMap.isEmpty()) {
            return false;
        }

        // Compute transitive closure: find all classes reachable via annotated method returns/params/fields
        computeTransitiveClosure(classDataMap);

        // Build and write JSON output using custom writer (no GSON dependency)
        try {
            FileObject resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "lua-interface-map.json");
            try (Writer writer = resource.openWriter()) {
                writeJsonMap(writer, classDataMap);
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "Generated lua-interface-map.json with " + classDataMap.size() + " classes");
            generated = true;
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write lua-interface-map.json: " + e.getMessage());
        }

        return true;
    }

    /**
     * Writes the class data map as JSON to the writer.
     * Custom implementation to avoid GSON dependency.
     */
    private void writeJsonMap(Writer writer, Map<TypeElement, ClassData> classDataMap) throws IOException {
        writer.write("{\n");
        boolean firstClass = true;
        for (Map.Entry<TypeElement, ClassData> entry : classDataMap.entrySet()) {
            TypeElement clazz = entry.getKey();
            ClassData data = entry.getValue();

            String className = clazz.getQualifiedName().toString();

            if (!firstClass) {
                writer.write(",\n");
            }
            firstClass = false;

            writer.write("  \"");
            writer.write(escapeJson(className));
            writer.write("\": {\n");

            boolean firstField = true;

            if (!data.methods.isEmpty()) {
                if (!firstField) writer.write(",\n");
                firstField = false;
                writer.write("    \"methods\": ");
                writeJsonArray(writer, new ArrayList<>(data.methods));
            }
            if (!data.fields.isEmpty()) {
                if (!firstField) writer.write(",\n");
                firstField = false;
                writer.write("    \"fields\": ");
                writeJsonArray(writer, new ArrayList<>(data.fields));
            }
            if (!data.constructors.isEmpty()) {
                if (!firstField) writer.write(",\n");
                firstField = false;
                writer.write("    \"constructors\": ");
                writeJsonArray(writer, new ArrayList<>(data.constructors));
            }

            if (firstField) {
                // Class has annotation but no public members - still include it
                writer.write("    \"classAnnotated\": true");
            }

            writer.write("\n  }");
        }
        writer.write("\n}\n");
    }

    private void writeJsonArray(Writer writer, List<String> list) throws IOException {
        writer.write("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) writer.write(", ");
            writer.write("\"");
            writer.write(escapeJson(list.get(i)));
            writer.write("\"");
        }
        writer.write("]");
    }

    private String escapeJson(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    private TypeElement getEnclosingClass(Element element) {
        Element enclosing = element.getEnclosingElement();
        while (enclosing != null && enclosing.getKind() != ElementKind.CLASS && enclosing.getKind() != ElementKind.INTERFACE) {
            enclosing = enclosing.getEnclosingElement();
        }
        return (TypeElement) enclosing;
    }

    /**
     * Computes transitive closure of @LuaInterface classes.
     * If an annotated method returns Type X, or takes Type X as parameter, or has a field of Type X,
     * and X has @LuaInterface on it (class or members), include X in the map.
     * Repeat until fixed point.
     */
    private void computeTransitiveClosure(Map<TypeElement, ClassData> classDataMap) {
        // Track which classes we've already processed for closure
        Set<TypeElement> visited = new HashSet<>();
        // Queue of classes to process
        java.util.Queue<TypeElement> queue = new java.util.LinkedList<>();

        // Initially, add all classes that have class-level @LuaInterface annotation
        for (Map.Entry<TypeElement, ClassData> entry : classDataMap.entrySet()) {
            if (entry.getValue().hasClassAnnotation) {
                queue.add(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            TypeElement current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            ClassData currentData = classDataMap.get(current);
            if (currentData == null) continue;

            // For each referenced type, find its TypeElement
            for (TypeMirror typeMirror : currentData.referencedTypes) {
                TypeElement referencedClass = getTypeElement(typeMirror);
                if (referencedClass == null) continue;

                // Check if this class has @LuaInterface (class-level or member-level)
                // We need to check the roundEnv for this class since it might not be in classDataMap yet
                boolean hasLuaInterface = hasLuaInterfaceAnnotation(referencedClass);

                if (hasLuaInterface) {
                    // Ensure it's in our map
                    if (!classDataMap.containsKey(referencedClass)) {
                        ClassData refData = new ClassData(referencedClass);
                        // Populate its members from roundEnv
                        populateClassData(referencedClass, refData);
                        classDataMap.put(referencedClass, refData);
                    }
                    // Queue for further processing if not already visited
                    if (!visited.contains(referencedClass)) {
                        queue.add(referencedClass);
                    }
                }
            }
        }
    }

    /**
     * Checks if a class has @LuaInterface annotation (class-level or member-level).
     */
    private boolean hasLuaInterfaceAnnotation(TypeElement clazz) {
        // Check class-level annotation
        if (clazz.getAnnotation(LuaInterface.class) != null) {
            return true;
        }
        // Check members - we'd need roundEnv, but we can approximate by checking if
        // the class is already in our map (from the current round)
        return false; // Conservative: only follow class-level annotations for now
    }

    /**
     * Populates ClassData for a class by scanning its members for @LuaInterface.
     */
    private void populateClassData(TypeElement clazz, ClassData data) {
        for (Element enclosed : clazz.getEnclosedElements()) {
            if (enclosed.getAnnotation(LuaInterface.class) == null) continue;
            
            switch (enclosed.getKind()) {
                case METHOD: {
                    ExecutableElement method = (ExecutableElement) enclosed;
                    if (method.getModifiers().contains(Modifier.PUBLIC)) {
                        data.methods.add(method.getSimpleName().toString());
                        data.referencedTypes.add(method.getReturnType());
                        for (VariableElement param : method.getParameters()) {
                            data.referencedTypes.add(param.asType());
                        }
                    }
                    break;
                }
                case FIELD: {
                    VariableElement field = (VariableElement) enclosed;
                    if (field.getModifiers().contains(Modifier.PUBLIC)) {
                        data.fields.add(field.getSimpleName().toString());
                        data.referencedTypes.add(field.asType());
                    }
                    break;
                }
                case CONSTRUCTOR: {
                    ExecutableElement constructor = (ExecutableElement) enclosed;
                    if (constructor.getModifiers().contains(Modifier.PUBLIC)) {
                        data.constructors.add("<init>");
                    }
                    break;
                }
            }
        }
    }

    /**
     * Extracts the TypeElement from a TypeMirror, handling arrays, generics, etc.
     */
    private TypeElement getTypeElement(TypeMirror typeMirror) {
        // Handle primitive types
        if (typeMirror.getKind().isPrimitive()) {
            return null;
        }

        // Handle array types - get component type
        if (typeMirror.getKind() == javax.lang.model.type.TypeKind.ARRAY) {
            javax.lang.model.type.ArrayType arrayType = (javax.lang.model.type.ArrayType) typeMirror;
            return getTypeElement(arrayType.getComponentType());
        }

        // Handle declared types (classes, interfaces, parameterized types)
        if (typeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            Element element = declaredType.asElement();
            if (element instanceof TypeElement) {
                return (TypeElement) element;
            }
        }

        // Handle type variables - try to get their upper bound
        if (typeMirror.getKind() == javax.lang.model.type.TypeKind.TYPEVAR) {
            javax.lang.model.type.TypeVariable typeVar = (javax.lang.model.type.TypeVariable) typeMirror;
            return getTypeElement(typeVar.getUpperBound());
        }

        return null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(LuaInterface.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private static class ClassData {
        boolean hasClassAnnotation = false;
        Set<String> methods = new HashSet<>();
        Set<String> fields = new HashSet<>();
        Set<String> constructors = new HashSet<>();
        // For transitive closure: track return/parameter types of annotated methods
        Set<TypeMirror> referencedTypes = new HashSet<>();

        ClassData(TypeElement clazz) {
            // Check if class itself has @LuaInterface
            this.hasClassAnnotation = clazz.getAnnotation(LuaInterface.class) != null;
        }
    }
}