package com.nyrds;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.io.Writer;
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

		for(Element clazz:fieldsByClass.keySet()){
			String clazzName = clazz.getSimpleName().toString();

			MethodSpec methodSpec = MethodSpec.methodBuilder(clazzName+"Pack")
					.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
					.returns(void.class)
					.addParameter(TypeName.get(clazz.asType()), "arg")
					.addParameter(ClassName.get("com.watabou.utils","Bundle"), "bundle")
					.build();
			BundleHelper = BundleHelper.toBuilder().addMethod(methodSpec).build();
		}

		JavaFile javaFile = JavaFile.builder("com.nydrs.generated", BundleHelper)
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
