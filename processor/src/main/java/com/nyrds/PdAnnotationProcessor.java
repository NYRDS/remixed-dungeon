package com.nyrds;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
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
		if (generated) {
			return true; // Skip processing if we've already generated the file
		}

		messager = processingEnv.getMessager();

		Map<Element, Set<Element>> fieldsByClass = new HashMap<>();
		Map<Element, String> defaultValues = new HashMap<>();
		Set<Element> classesToImport = new HashSet<>();

		for (Element element : roundEnvironment.getElementsAnnotatedWith(Packable.class)) {
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
			CodeBlock.Builder packerBlock = CodeBlock.builder()
					.beginControlFlow("if (arg instanceof $T)", TypeName.get(clazz.asType()));

			CodeBlock.Builder unpackerBlock = CodeBlock.builder()
					.beginControlFlow("if (arg instanceof $T)", TypeName.get(clazz.asType()));

			for (Element field : fieldsByClass.get(clazz)) {
				String fieldName = field.getSimpleName().toString();
				TypeMirror fieldType = field.asType();
				String defaultValue = defaultValues.getOrDefault(field, "");

				packerBlock.addStatement("$T $L = $T.class.getDeclaredField($S)",
								Field.class, fieldName, TypeName.get(clazz.asType()), fieldName)
						.addStatement("$L.setAccessible(true)", fieldName);

				packerBlock.addStatement("bundle.put($S, ($T) $L.get(arg))",
						fieldName, TypeName.get(fieldType), fieldName);

				unpackerBlock.addStatement("$T $L = $T.class.getDeclaredField($S)",
								Field.class, fieldName, TypeName.get(clazz.asType()), fieldName)
						.addStatement("$L.setAccessible(true)", fieldName);

				CodeBlock unpackCode = generateUnpackCode(field, fieldName, fieldType, defaultValue);
				unpackerBlock.add(unpackCode);
			}

			packerBlock.endControlFlow();
			unpackerBlock.endControlFlow();

			packBuilder.addCode(packerBlock.build());
			unpackBuilder.addCode(unpackerBlock.build());
		}

		// Exception handling for pack
		packBuilder.nextControlFlow("catch ($T e)", NoSuchFieldException.class)
				.addStatement("throw new $T(e)", ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.nextControlFlow("catch ($T e)", IllegalAccessException.class)
				.addStatement("throw new $T(e)", ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.endControlFlow();

		// Exception handling for unpack
		unpackBuilder.nextControlFlow("catch ($T e)", NoSuchFieldException.class)
				.addStatement("throw new $T(e)", ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.nextControlFlow("catch ($T e)", IllegalAccessException.class)
				.addStatement("throw new $T(e)", ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.endControlFlow();

		bundleHelperBuilder.addMethod(packBuilder.build())
				.addMethod(unpackBuilder.build());

		// Add holder fields for imported classes
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

	private CodeBlock generateUnpackCode(Element field, String fieldName, TypeMirror fieldType, String defaultValue) {
		CodeBlock.Builder code = CodeBlock.builder();

		if (processingEnv.getTypeUtils().isSameType(fieldType, intType)) {
			defaultValue = defaultValue.isEmpty() ? "0" : defaultValue;
			code.addStatement("$L.setInt(arg, bundle.optInt($S, $L))", fieldName, fieldName, defaultValue);
		}  else if (processingEnv.getTypeUtils().isSameType(fieldType, booleanType)) {
			defaultValue = defaultValue.isEmpty() ? "false" : defaultValue;
			code.addStatement("$L.setBoolean(arg, bundle.optBoolean($S, $L))", fieldName, fieldName, defaultValue);
		} else if (processingEnv.getTypeUtils().isSameType(fieldType, floatType)) {
			defaultValue = defaultValue.isEmpty() ? "0.0f" : defaultValue;
			code.addStatement("$L.setFloat(arg, bundle.optFloat($S, $L))", fieldName, fieldName, defaultValue);
		} else if (processingEnv.getTypeUtils().isSameType(fieldType, stringType)) {
			defaultValue = defaultValue.isEmpty() ? "Unknown" : defaultValue;
			code.addStatement("$L.set(arg, bundle.optString($S, $S))", fieldName, fieldName, defaultValue);
		} else if (processingEnv.getTypeUtils().isAssignable(fieldType, bundlableType)) {
			if (defaultValue.isEmpty()) {
				code.addStatement("$L.set(arg, bundle.get($S))", fieldName, fieldName);
			} else {
				code.addStatement("$L.set(arg, bundle.opt($S, $L))", fieldName, fieldName, defaultValue);
			}
		} else if (isEnumType(fieldType)) {
			handleEnumUnpack(code, fieldName, fieldType, defaultValue);
		} else if (fieldType.getKind() == TypeKind.ARRAY) {
			handleArrayUnpack(code, fieldName, (ArrayType) fieldType);
		} else if (isCollectionType(fieldType)) {
			handleCollectionUnpack(code, fieldName, fieldType);
		} else if (isMapType(fieldType)) {
			handleMapUnpack(code, fieldName, fieldType);
		} else {
			messager.printMessage(Diagnostic.Kind.ERROR, "Unsupported field type: " + fieldType, field);
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

	private void handleEnumUnpack(CodeBlock.Builder code, String fieldName, TypeMirror enumType, String defaultValue) {
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

		code.addStatement("$L.set(arg, bundle.getEnum($S, $T.class, $L))",
				fieldName, fieldName, enumClassName, defaultValueExpr);
	}

	private void handleArrayUnpack(CodeBlock.Builder code, String fieldName, ArrayType arrayType) {
		TypeMirror componentType = arrayType.getComponentType();
		if (processingEnv.getTypeUtils().isSameType(componentType, intType)) {
			code.addStatement("$L.set(arg, bundle.getIntArray($S))", fieldName, fieldName);
		} else if (processingEnv.getTypeUtils().isSameType(componentType, booleanType)) {
			code.addStatement("$L.set(arg, bundle.getBooleanArray($S))", fieldName, fieldName);
		} else if (processingEnv.getTypeUtils().isSameType(componentType, stringType)) {
			code.addStatement("$L.set(arg, bundle.getStringArray($S))", fieldName, fieldName);
		} else {
			messager.printMessage(Diagnostic.Kind.ERROR, "Unsupported array component type: " + componentType);
		}
	}

	private void handleCollectionUnpack(CodeBlock.Builder code, String fieldName, TypeMirror collectionType) {
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
		code.addStatement("$L.set(arg, bundle.getCollection($S, $T.class))", fieldName, fieldName, elementClass);
	}

	private void handleMapUnpack(CodeBlock.Builder code, String fieldName, TypeMirror mapType) {
		DeclaredType declaredType = (DeclaredType) mapType;
		List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
		if (typeArgs.size() != 2 || !processingEnv.getTypeUtils().isSameType(typeArgs.get(0), stringType)) {
			messager.printMessage(Diagnostic.Kind.ERROR, "Map must have String keys");
			return;
		}
		TypeMirror valueType = typeArgs.get(1);
		ClassName valueClass = ClassName.get((TypeElement) processingEnv.getTypeUtils().asElement(valueType));
		code.addStatement("$L.set(arg, ($T) bundle.getMap($S))", fieldName, TypeName.get(mapType), fieldName);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return ImmutableSet.of(Packable.class.getCanonicalName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}