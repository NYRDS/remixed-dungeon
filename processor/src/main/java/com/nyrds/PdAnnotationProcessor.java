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
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class PdAnnotationProcessor extends AbstractProcessor{
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {


		Map<Element, Set<Element>> fieldsByClass = new HashMap<>();
		// for each javax.lang.model.element.Element annotated with the CustomAnnotation
		for (Element element : roundEnvironment.getElementsAnnotatedWith(Packable.class)) {
			Element parent = element.getEnclosingElement();

			if(fieldsByClass.get(parent)==null) {
				fieldsByClass.put(parent,new HashSet<Element>());
			}

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
						.addStatement("bundle.put($S,($T)$L.get(arg))", fieldName,TypeName.get(field.asType()), fieldName)
						.build();

				unpackerBlock = unpackerBlock.toBuilder()
						.addStatement("$T $L = $T.class.getDeclaredField($S)", Field.class,fieldName,TypeName.get(clazz.asType()),fieldName)
						.addStatement("$L.setAccessible(true)",fieldName)
						.build();

				String fieldType = TypeName.get(field.asType()).toString();
				if(fieldType.equals("int")) {
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.setInt(arg,bundle.getInt($S))", fieldName,fieldName)
							.build();
				}
				if(fieldType.equals("boolean")) {
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.setBoolean(arg,bundle.getBoolean($S))", fieldName,fieldName)
							.build();
				}
				if(fieldType.equals("float")) {
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.setFloat(arg,bundle.getFloat($S))", fieldName,fieldName)
							.build();
				}
				if(fieldType.equals("String")) {
					unpackerBlock = unpackerBlock.toBuilder()
							.addStatement("$L.set(arg,bundle.getString($S))", fieldName,fieldName)
							.build();
				}
			}

			packerBlock = packerBlock.toBuilder().endControlFlow().build();
			unpackerBlock = unpackerBlock.toBuilder().endControlFlow().build();

			pack = pack.toBuilder().addCode(packerBlock).build();
			unpack = unpack.toBuilder().addCode(unpackerBlock).build();
		}

		pack = pack.toBuilder().nextControlFlow("catch ($T e)", NoSuchFieldException.class)
				.addStatement("throw new $T(e)",ClassName.get("com.nyrds.android.util","TrackedRuntimeException"))
				.nextControlFlow("catch ($T e)", IllegalAccessException.class)
				.addStatement("throw new $T(e)",ClassName.get("com.nyrds.android.util","TrackedRuntimeException"))
				.endControlFlow().build();

		unpack = unpack.toBuilder().nextControlFlow("catch ($T e)", NoSuchFieldException.class)
				.addStatement("throw new $T(e)",ClassName.get("com.nyrds.android.util","TrackedRuntimeException"))
				.nextControlFlow("catch ($T e)", IllegalAccessException.class)
				.addStatement("throw new $T(e)",ClassName.get("com.nyrds.android.util","TrackedRuntimeException"))
				.endControlFlow().build();

		BundleHelper = BundleHelper.toBuilder()
				.addMethod(pack)
				.addMethod(unpack)
				.build();

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
