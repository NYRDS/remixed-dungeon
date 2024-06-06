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
import org.apache.commons.collections4.map.HashedMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import lombok.val;

@AutoService(Processor.class)
public class
PdAnnotationProcessor extends AbstractProcessor{


	public static final String COM_NYRDS_PLATFORM_UTIL = "com.nyrds.platform.util";
	public static final String TRACKED_RUNTIME_EXCEPTION = "TrackedRuntimeException";

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

		final TypeMirror bundlable = processingEnv.getElementUtils().getTypeElement("com.watabou.utils.Bundlable").asType();
		//final TypeMirror CharList = processingEnv.getElementUtils().getTypeElement("com.nyrds.pixeldungeon.utils.CharList").asType();

		ClassName charList = ClassName.get("com.nyrds.pixeldungeon.utils", "CharsList");
		ClassName itemsList = ClassName.get("com.nyrds.pixeldungeon.utils", "ItemsList");

		Map<Element, Set<Element>> fieldsByClass = new HashedMap<>();
		Map<Element, String>       defaultValues = new HashedMap<>();
		Set<Element>               classesToImport = new HashSet<>();

		for (Element element : roundEnvironment.getElementsAnnotatedWith(Packable.class)) {
			if(element.getKind().isClass()){
				classesToImport.add(element);
				continue;
			}

			Element parent = element.getEnclosingElement();

			String defaultValue = element.getAnnotation(Packable.class).defaultValue();
			if(!defaultValue.isEmpty()) {
				defaultValues.put(element,defaultValue);
			}

            fieldsByClass.computeIfAbsent(parent, k -> new HashSet<>());

			fieldsByClass.get(parent).add(element);
		}

		TypeSpec BundleHelper = TypeSpec.classBuilder("BundleHelper")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.build();

		MethodSpec pack = MethodSpec.methodBuilder("Pack")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(void.class)
				.addParameter(ClassName.get("com.watabou.utils","Bundlable"), "arg")
				.addParameter(ClassName.get("com.watabou.utils","Bundle"), "bundle")
				.beginControlFlow("try")
				.build();

		MethodSpec unpack = MethodSpec.methodBuilder("UnPack")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.returns(void.class)
				.addParameter(ClassName.get("com.watabou.utils","Bundlable"), "arg")
				.addParameter(ClassName.get("com.watabou.utils","Bundle"), "bundle")
				.beginControlFlow("try")
				.build();

		for(Element clazz:fieldsByClass.keySet()){

			CodeBlock packerBlock = CodeBlock.builder()
					.beginControlFlow("if(arg instanceof $T)",TypeName.get(clazz.asType()))
					.build();

			CodeBlock unpackerBlock = CodeBlock.builder()
					.beginControlFlow("if(arg instanceof $T)",TypeName.get(clazz.asType()))
					.build();

			for (Element field:fieldsByClass.get(clazz)) {
				String fieldName = field.getSimpleName().toString();
				packerBlock = packerBlock.toBuilder()
						.addStatement("$T $L = $T.class.getDeclaredField($S)", Field.class,fieldName,TypeName.get(clazz.asType()),fieldName)
						.addStatement("$L.setAccessible(true)",fieldName)
						.addStatement("bundle.put($S,($T)$L.get(arg))", fieldName, TypeName.get(field.asType()), fieldName)
						.build();

				unpackerBlock = unpackerBlock.toBuilder()
						.addStatement("$T $L = $T.class.getDeclaredField($S)", Field.class,fieldName,TypeName.get(clazz.asType()),fieldName)
						.addStatement("$L.setAccessible(true)",fieldName)
						.build();

				String fieldType = TypeName.get(field.asType()).toString();

				String defaultValue = defaultValues.get(field);
				if(fieldType.equals("int")) {
					if(defaultValue==null || defaultValue.isEmpty()) {
						defaultValue = "0";
					}
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.setInt(arg,bundle.optInt($S,$L))", fieldName,fieldName,defaultValue)
							.build();
					continue;
				}
				if(fieldType.equals("boolean")) {
					if(defaultValue==null || defaultValue.isEmpty()) {
						defaultValue = "false";
					}
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.setBoolean(arg,bundle.optBoolean($S,$L))", fieldName,fieldName,defaultValue)
							.build();
					continue;
				}
				if(fieldType.equals("float")) {
					if(defaultValue==null || defaultValue.isEmpty()) {
						defaultValue = "0.0f";
					}
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.setFloat(arg,bundle.optFloat($S,$L))", fieldName,fieldName,defaultValue)
							.build();
					continue;
				}

				if(fieldType.equals("java.lang.String")) {
					if(defaultValue==null || defaultValue.isEmpty()) {
						defaultValue = "Unknown";
					}
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.set(arg,bundle.optString($S,$S))", fieldName,fieldName,defaultValue)
							.build();
					continue;
				}


/*
				if(fieldType.equals("Collection")) {
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.set(arg,bundle.getCollection($S,$S))", fieldName,fieldName,fieldType+".class")
							.build();
					continue;
				}
*/

				if(processingEnv.getTypeUtils().isAssignable(field.asType(),bundlable)) {
					if(defaultValue==null) {
						unpackerBlock = unpackerBlock.toBuilder()
								.addStatement("$L.set(arg,bundle.get($S))", fieldName, fieldName)
								.build();
						continue;
					}
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.set(arg,bundle.opt($S,$L))", fieldName, fieldName, defaultValue)
							.build();
					continue;
				}
			}

			packerBlock = packerBlock.toBuilder().endControlFlow().build();
			unpackerBlock = unpackerBlock.toBuilder().endControlFlow().build();

			pack = pack.toBuilder().addCode(packerBlock).build();
			unpack = unpack.toBuilder().addCode(unpackerBlock).build();
		}

		pack = pack.toBuilder().nextControlFlow("catch ($T e)", NoSuchFieldException.class)
				.addStatement("throw new $T(e)",ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.nextControlFlow("catch ($T e)", IllegalAccessException.class)
				.addStatement("throw new $T(e)",ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.endControlFlow().build();

		unpack = unpack.toBuilder().nextControlFlow("catch ($T e)", NoSuchFieldException.class)
				.addStatement("throw new $T(e)",ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.nextControlFlow("catch ($T e)", IllegalAccessException.class)
				.addStatement("throw new $T(e)",ClassName.get(COM_NYRDS_PLATFORM_UTIL, TRACKED_RUNTIME_EXCEPTION))
				.endControlFlow().build();

		BundleHelper = BundleHelper.toBuilder()
				.addMethod(pack)
				.addMethod(unpack).build();

		for (val clazz: classesToImport) {
			BundleHelper = BundleHelper.toBuilder()
					.addField(ClassName.bestGuess(clazz.asType().toString()), String.format("holder_%s", clazz.asType().hashCode()), Modifier.PRIVATE)
					.build();
		}

		JavaFile javaFile = JavaFile.builder("com.nyrds.generated", BundleHelper)
				.build();

		try { // write the file
			JavaFileObject source = processingEnv.getFiler().createSourceFile("com.nyrds.generated.BundleHelper");

			Writer writer = source.openWriter();
			javaFile.writeTo(writer);
			writer.flush();
			writer.close();

		} catch (IOException e) {
			// Note: calling e.printStackTrace() will print IO errors
			// that occur from the file already existing after its first run, this is normal
		}

		return true;

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
