package com.nyrds;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class PdAnnotationProcessor extends AbstractProcessor {
	private Messager messager;

	public static final String COM_NYRDS_PLATFORM_UTIL = "com.nyrds.platform.util";
	public static final String TRACKED_RUNTIME_EXCEPTION = "TrackedRuntimeException";

	private TypeMirror intType;
	private TypeMirror booleanType;
	private TypeMirror floatType;
	private TypeMirror stringType;
	private TypeMirror bundlableType;
	private TypeMirror collectionType;
	private TypeMirror mapType;

	private boolean generated = false;

	@Override
	public synchronized void init(javax.annotation.processing.ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		Types typeUtils = processingEnv.getTypeUtils();

		intType = typeUtils.getPrimitiveType(TypeKind.INT);
		booleanType = typeUtils.getPrimitiveType(TypeKind.BOOLEAN);
		floatType = typeUtils.getPrimitiveType(TypeKind.FLOAT);
		stringType = processingEnv.getElementUtils().getTypeElement("java.lang.String").asType();
		bundlableType = processingEnv.getElementUtils().getTypeElement("com.watabou.utils.Bundlable").asType();
		collectionType = processingEnv.getElementUtils().getTypeElement("java.util.Collection").asType();
		mapType = processingEnv.getElementUtils().getTypeElement("java.util.Map").asType();
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		messager = processingEnv.getMessager();

		// Process @LuaInterface annotations
		processLuaInterface(roundEnvironment);

		if (generated) {
			return true;
		}

		Map<Element, Set<Element>> fieldsByClass = new HashMap<>();
		Map<Element, String> defaultValues = new HashMap<>();
		Set<Element> classesToImport = new HashSet<>();

		for (Element element : roundEnvironment.getElementsAnnotatedWith(Packable.class)) {
			if (element.getKind() == ElementKind.FIELD) {
				// VALIDATION 1: Enforce public modifier
				if (!element.getModifiers().contains(Modifier.PUBLIC)) {
					messager.printMessage(Diagnostic.Kind.ERROR,
							"Field annotated with @Packable must be public.", element);
				}
				// MODIFICATION 1: VALIDATION - Enforce non-final modifier
				if (element.getModifiers().contains(Modifier.FINAL)) {
					messager.printMessage(Diagnostic.Kind.ERROR,
							"Field annotated with @Packable cannot be final, as it cannot be restored during unpacking.", element);
				}
			}

			if (element.getKind().isClass()) {
				classesToImport.add(element);
				continue;
			}

			Element parent = element.getEnclosingElement();
			String defaultValue = element.getAnnotation(Packable.class).defaultValue();
			if (!defaultValue.isEmpty()) {
				defaultValues.put(element, defaultValue);
			}

			fieldsByClass.computeIfAbsent(parent, k -> new HashSet<>()).add(element);
		}

		TypeSpec.Builder bundleHelperBuilder = TypeSpec.classBuilder("BundleHelper")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

		MethodSpec.Builder packBuilder = MethodSpec.methodBuilder("Pack")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(void.class)
				.addParameter(ClassName.get("com.watabou.utils", "Bundlable"), "arg")
				.addParameter(ClassName.get("com.watabou.utils", "Bundle"), "bundle")
				.beginControlFlow("try");

		MethodSpec.Builder unpackBuilder = MethodSpec.methodBuilder("UnPack")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(void.class)
				.addParameter(ClassName.get("com.watabou.utils", "Bundlable"), "arg")
				.addParameter(ClassName.get("com.watabou.utils", "Bundle"), "bundle")
				.beginControlFlow("try");

		for (Element clazz : fieldsByClass.keySet()) {
			TypeName clazzType = TypeName.get(clazz.asType());

			CodeBlock.Builder packerBlock = CodeBlock.builder()
					.beginControlFlow("if (arg instanceof $T)", clazzType)
					.addStatement("$T typedArg = ($T) arg", clazzType, clazzType);

			CodeBlock.Builder unpackerBlock = CodeBlock.builder()
					.beginControlFlow("if (arg instanceof $T)", clazzType)
					.addStatement("$T typedArg = ($T) arg", clazzType, clazzType);

			for (Element field : fieldsByClass.get(clazz)) {
				// Don't generate unpack logic for final fields
				if (!field.getModifiers().contains(Modifier.FINAL)) {
					String fieldName = field.getSimpleName().toString();
					TypeMirror fieldType = field.asType();
					String defaultValue = defaultValues.getOrDefault(field, "");

					packerBlock.addStatement("bundle.put($S, typedArg.$L)", fieldName, fieldName);

					CodeBlock unpackCode = generateDirectUnpackCode(fieldName, fieldType, defaultValue);
					unpackerBlock.add(unpackCode);
				}
			}

			packerBlock.endControlFlow();
			unpackerBlock.endControlFlow();

			packBuilder.addCode(packerBlock.build());
			unpackBuilder.addCode(unpackerBlock.build());
		}

		packBuilder.nextControlFlow("catch ($T e)", Exception.class)
				.addStatement("throw new $T(e)", ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.endControlFlow();

		unpackBuilder.nextControlFlow("catch ($T e)", Exception.class)
				.addStatement("throw new $T(e)", ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.endControlFlow();

		bundleHelperBuilder.addMethod(packBuilder.build())
				.addMethod(unpackBuilder.build());

		for (Element clazz : classesToImport) {
			bundleHelperBuilder.addField(ClassName.bestGuess(clazz.asType().toString()),
					"holder_" + clazz.asType().hashCode(), Modifier.PRIVATE);
		}

		if (fieldsByClass.isEmpty() && classesToImport.isEmpty()) {
			return false;
		}

		try {
			JavaFile javaFile = JavaFile.builder("com.nyrds.generated", bundleHelperBuilder.build()).build();
			JavaFileObject source = processingEnv.getFiler().createSourceFile("com.nyrds.generated.BundleHelper");
			Writer writer = source.openWriter();
			javaFile.writeTo(writer);
			writer.close();
			generated = true;
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write file: " + e.getMessage());
		}

		return true;
	}

	private CodeBlock generateDirectUnpackCode(String fieldName, TypeMirror fieldType, String defaultValue) {
		CodeBlock.Builder code = CodeBlock.builder();
		String statementTemplate = "typedArg.$L = ";

		if (processingEnv.getTypeUtils().isSameType(fieldType, intType)) {
			defaultValue = defaultValue.isEmpty() ? "0" : defaultValue;
			code.addStatement(statementTemplate + "bundle.optInt($S, $L)", fieldName, fieldName, defaultValue);
		}  else if (processingEnv.getTypeUtils().isSameType(fieldType, booleanType)) {
			defaultValue = defaultValue.isEmpty() ? "false" : defaultValue;
			code.addStatement(statementTemplate + "bundle.optBoolean($S, $L)", fieldName, fieldName, defaultValue);
		} else if (processingEnv.getTypeUtils().isSameType(fieldType, floatType)) {
			defaultValue = defaultValue.isEmpty() ? "0.0f" : defaultValue;
			code.addStatement(statementTemplate + "bundle.optFloat($S, $L)", fieldName, fieldName, defaultValue);
		} else if (processingEnv.getTypeUtils().isSameType(fieldType, stringType)) {
			defaultValue = defaultValue.isEmpty() ? "Unknown" : defaultValue;
			code.addStatement(statementTemplate + "bundle.optString($S, $S)", fieldName, fieldName, defaultValue);
		} else if (processingEnv.getTypeUtils().isAssignable(fieldType, bundlableType)) {
			// MODIFICATION 2: BUG FIX - Add explicit cast for Bundlable subtypes
			TypeName fieldTypeName = TypeName.get(fieldType);
			if (defaultValue.isEmpty()) {
				code.addStatement(statementTemplate + "($T) bundle.get($S)", fieldName, fieldTypeName, fieldName);
			} else {
				code.addStatement(statementTemplate + "($T) bundle.opt($S, $L)", fieldName, fieldTypeName, fieldName, defaultValue);
			}
		} else if (isEnumType(fieldType)) {
			handleEnumUnpack(code, fieldName, fieldType, defaultValue, statementTemplate);
		} else if (fieldType.getKind() == TypeKind.ARRAY) {
			handleArrayUnpack(code, fieldName, (ArrayType) fieldType, statementTemplate);
		} else if (isCollectionType(fieldType)) {
			handleCollectionUnpack(code, fieldName, fieldType, statementTemplate);
		} else if (isMapType(fieldType)) {
			handleMapUnpack(code, fieldName, fieldType, statementTemplate);
		} else {
			messager.printMessage(Diagnostic.Kind.ERROR, "Unsupported field type for direct access: " + fieldType);
		}

		return code.build();
	}

	private boolean isEnumType(TypeMirror type) {
		Element element = processingEnv.getTypeUtils().asElement(type);
		return element != null && element.getKind() == ElementKind.ENUM;
	}

	private boolean isCollectionType(TypeMirror type) {
		return processingEnv.getTypeUtils().isAssignable(type, collectionType);
	}

	private boolean isMapType(TypeMirror type) {
		return processingEnv.getTypeUtils().isAssignable(type, mapType);
	}

	private void handleEnumUnpack(CodeBlock.Builder code, String fieldName, TypeMirror enumType, String defaultValue, String template) {
		TypeElement enumElement = (TypeElement) processingEnv.getTypeUtils().asElement(enumType);
		ClassName enumClassName = ClassName.get(enumElement);

		CodeBlock defaultValueExpr;
		if (defaultValue.isEmpty()) {
			List<? extends Element> constants = enumElement.getEnclosedElements().stream()
					.filter(e -> e.getKind() == ElementKind.ENUM_CONSTANT)
					.collect(Collectors.toList());
			if (constants.isEmpty()) {
				messager.printMessage(Diagnostic.Kind.ERROR, "Enum has no constants", enumElement);
				return;
			}
			String firstConstant = constants.get(0).getSimpleName().toString();
			defaultValueExpr = CodeBlock.of("$T.$L", enumClassName, firstConstant);
		} else {
			defaultValueExpr = CodeBlock.of("$T.valueOf($S)", enumClassName, defaultValue);
		}

		code.addStatement(template + "bundle.getEnum($S, $T.class, $L)",
				fieldName, fieldName, enumClassName, defaultValueExpr);
	}

	private void handleArrayUnpack(CodeBlock.Builder code, String fieldName, ArrayType arrayType, String template) {
		TypeMirror componentType = arrayType.getComponentType();
		if (processingEnv.getTypeUtils().isSameType(componentType, intType)) {
			code.addStatement(template + "bundle.getIntArray($S)", fieldName, fieldName);
		} else if (processingEnv.getTypeUtils().isSameType(componentType, booleanType)) {
			code.addStatement(template + "bundle.getBooleanArray($S)", fieldName, fieldName);
		} else if (processingEnv.getTypeUtils().isSameType(componentType, stringType)) {
			code.addStatement(template + "bundle.getStringArray($S)", fieldName, fieldName);
		} else {
			messager.printMessage(Diagnostic.Kind.ERROR, "Unsupported array component type: " + componentType);
		}
	}

	private void handleCollectionUnpack(CodeBlock.Builder code, String fieldName, TypeMirror collectionType, String template) {
		DeclaredType declaredType = (DeclaredType) collectionType;
		List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
		if (typeArgs.size() != 1) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Collection must have a single type parameter");
			return;
		}
		TypeMirror elementType = typeArgs.get(0);
		if (!processingEnv.getTypeUtils().isAssignable(elementType, bundlableType)) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Collection element must be Bundlable");
			return;
		}
		ClassName elementClass = ClassName.get((TypeElement) processingEnv.getTypeUtils().asElement(elementType));
		code.addStatement(template + "bundle.getCollection($S, $T.class)", fieldName, fieldName, elementClass);
	}

	private void handleMapUnpack(CodeBlock.Builder code, String fieldName, TypeMirror mapType, String template) {
		DeclaredType declaredType = (DeclaredType) mapType;
		List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
		if (typeArgs.size() != 2 || !processingEnv.getTypeUtils().isSameType(typeArgs.get(0), stringType)) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Map must have String keys");
			return;
		}
		code.addStatement(template + "($T) bundle.getMap($S)", fieldName, TypeName.get(mapType), fieldName);
	}

	/**
	 * Process @LuaInterface annotations and generate LuaClassMap.java
	 * with all classes, methods, fields, and constructors needed for
	 * TeaVM reflective access from Lua scripts.
	 */
	private boolean luaClassMapGenerated = false;

	private void processLuaInterface(RoundEnvironment roundEnvironment) {
		if (luaClassMapGenerated) {
			return;
		}

		// className -> set of method names
		Map<String, Set<String>> methodsByClass = new HashMap<>();
		// className -> set of field names
		Map<String, Set<String>> fieldsByClass = new HashMap<>();
		// className -> has constructor
		Set<String> hasConstructor = new HashSet<>();
		// All class names seen
		Set<String> allClasses = new HashSet<>();

		for (Element element : roundEnvironment.getElementsAnnotatedWith(LuaInterface.class)) {
			ElementKind kind = element.getKind();
			String className;

			if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE) {
				// Type-level @LuaInterface: include all public methods
				className = ((TypeElement) element).getQualifiedName().toString();
				allClasses.add(className);
				for (Element enclosed : element.getEnclosedElements()) {
					if (enclosed.getKind() == ElementKind.METHOD
							&& enclosed.getModifiers().contains(Modifier.PUBLIC)) {
						methodsByClass.computeIfAbsent(className, k -> new HashSet<>())
								.add(enclosed.getSimpleName().toString());
					}
				}
			} else if (kind == ElementKind.METHOD || kind == ElementKind.CONSTRUCTOR) {
				Element parent = element.getEnclosingElement();
				if (parent.getKind() != ElementKind.CLASS && parent.getKind() != ElementKind.INTERFACE) {
					continue;
				}
				className = ((TypeElement) parent).getQualifiedName().toString();
				allClasses.add(className);

				if (kind == ElementKind.CONSTRUCTOR) {
					hasConstructor.add(className);
				} else {
					methodsByClass.computeIfAbsent(className, k -> new HashSet<>())
							.add(element.getSimpleName().toString());
				}
			} else if (kind == ElementKind.FIELD) {
				Element parent = element.getEnclosingElement();
				if (parent.getKind() != ElementKind.CLASS && parent.getKind() != ElementKind.INTERFACE) {
					continue;
				}
				className = ((TypeElement) parent).getQualifiedName().toString();
				allClasses.add(className);
				fieldsByClass.computeIfAbsent(className, k -> new HashSet<>())
						.add(element.getSimpleName().toString());
			}
		}

		if (allClasses.isEmpty() && methodsByClass.isEmpty()) {
			return;
		}

		// Add hardcoded JDK types needed as return types from Lua-accessible methods
		allClasses.add("java.util.List");
		allClasses.add("java.util.ArrayList");
		allClasses.add("java.util.Map");
		allClasses.add("java.util.HashMap");
		allClasses.add("java.util.Set");
		allClasses.add("java.util.HashSet");
		allClasses.add("java.util.Collection");

		// List methods — registered on both the interface AND concrete ArrayList,
		// because TeaVM reflection is per-class and the hierarchy walk follows
		// extends (not implements), so interface methods won't be found at runtime.
		java.util.List<String> listMethods = java.util.Arrays.asList("size", "get", "add", "remove",
				"isEmpty", "contains", "indexOf", "iterator", "toArray", "containsAll", "addAll",
				"removeAll", "clear", "listIterator", "subList", "set", "lastIndexOf");
		methodsByClass.computeIfAbsent("java.util.List", k -> new HashSet<>()).addAll(listMethods);
		methodsByClass.computeIfAbsent("java.util.ArrayList", k -> new HashSet<>()).addAll(listMethods);
		methodsByClass.get("java.util.ArrayList").add("<init>");

		java.util.List<String> mapMethods = java.util.Arrays.asList("get", "put", "containsKey", "size",
				"keySet", "values", "entrySet", "isEmpty", "remove", "containsValue", "getOrDefault",
				"putIfAbsent");
		methodsByClass.computeIfAbsent("java.util.Map", k -> new HashSet<>()).addAll(mapMethods);
		methodsByClass.computeIfAbsent("java.util.HashMap", k -> new HashSet<>()).addAll(mapMethods);
		methodsByClass.get("java.util.HashMap").add("<init>");

		java.util.List<String> setMethods = java.util.Arrays.asList("size", "isEmpty", "contains",
				"iterator", "toArray", "add", "remove", "containsAll", "addAll", "retainAll",
				"removeAll", "clear");
		methodsByClass.computeIfAbsent("java.util.Set", k -> new HashSet<>()).addAll(setMethods);
		methodsByClass.computeIfAbsent("java.util.HashSet", k -> new HashSet<>()).addAll(setMethods);
		methodsByClass.get("java.util.HashSet").add("<init>");

		java.util.List<String> collectionMethods = java.util.Arrays.asList("size", "isEmpty", "contains",
				"iterator", "toArray", "add", "remove", "containsAll", "addAll", "removeAll", "clear");
		methodsByClass.computeIfAbsent("java.util.Collection", k -> new HashSet<>()).addAll(collectionMethods);

		hasConstructor.add("java.util.ArrayList");
		hasConstructor.add("java.util.HashMap");
		hasConstructor.add("java.util.HashSet");

		generateLuaClassMap(allClasses, methodsByClass, fieldsByClass, hasConstructor);
		luaClassMapGenerated = true;
	}

	private void generateLuaClassMap(
			Set<String> allClasses,
			Map<String, Set<String>> methodsByClass,
			Map<String, Set<String>> fieldsByClass,
			Set<String> hasConstructor) {
		try {
			TypeSpec.Builder builder = TypeSpec.classBuilder("LuaClassMap")
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

			// ALL_CLASSES set
			builder.addField(FieldSpec.builder(
					ClassName.get(Set.class),
					"ALL_CLASSES",
					Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer(buildStringSetInitializer(allClasses))
					.build());

			// METHODS map
			builder.addField(FieldSpec.builder(
					ParameterizedTypeName.get(
							ClassName.get(Map.class),
							ClassName.get(String.class),
							ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
					"METHODS",
					Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer(buildStringMapOfSetsInitializer(methodsByClass))
					.build());

			// FIELDS map
			builder.addField(FieldSpec.builder(
					ParameterizedTypeName.get(
							ClassName.get(Map.class),
							ClassName.get(String.class),
							ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class))),
					"FIELDS",
					Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer(buildStringMapOfSetsInitializer(fieldsByClass))
					.build());

			// HAS_CONSTRUCTORS set
			builder.addField(FieldSpec.builder(
					ClassName.get(Set.class),
					"HAS_CONSTRUCTORS",
					Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
					.initializer(buildStringSetInitializer(hasConstructor))
					.build());

			JavaFile javaFile = JavaFile.builder("com.nyrds.generated", builder.build()).build();
			JavaFileObject source = processingEnv.getFiler().createSourceFile("com.nyrds.generated.LuaClassMap");
			Writer writer = source.openWriter();
			javaFile.writeTo(writer);
			writer.close();
		} catch (IOException e) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write LuaClassMap: " + e.getMessage());
		}
	}

	private CodeBlock buildStringSetInitializer(Set<String> values) {
		List<String> sorted = new java.util.ArrayList<>(values);
		java.util.Collections.sort(sorted);

		CodeBlock.Builder builder = CodeBlock.builder();
		builder.add("new $T<$T>() {{\n", HashSet.class, String.class);
		for (String value : sorted) {
			builder.add("  add($S);\n", value);
		}
		builder.add("}}");
		return builder.build();
	}

	private CodeBlock buildStringMapOfSetsInitializer(Map<String, Set<String>> map) {
		List<String> sortedKeys = new java.util.ArrayList<>(map.keySet());
		java.util.Collections.sort(sortedKeys);

		ParameterizedTypeName setOfString = ParameterizedTypeName.get(ClassName.get(Set.class), ClassName.get(String.class));

		CodeBlock.Builder builder = CodeBlock.builder();
		builder.add("new $T<$T, $T>() {{\n", HashMap.class, String.class, setOfString);
		for (String key : sortedKeys) {
			Set<String> values = map.get(key);
			List<String> sortedValues = new java.util.ArrayList<>(values);
			java.util.Collections.sort(sortedValues);
			builder.add("  put($S, new $T<$T>() {{\n", key, HashSet.class, String.class);
			for (String value : sortedValues) {
				builder.add("    add($S);\n", value);
			}
			builder.add("  }});\n");
		}
		builder.add("}}");
		return builder.build();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return ImmutableSet.of(
			Packable.class.getCanonicalName(),
			LuaInterface.class.getCanonicalName()
		);
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}