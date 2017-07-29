package com.nyrds;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class PdAnnotationProcessor extends AbstractProcessor{
	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		StringBuilder builder = new StringBuilder()
				.append("package com.nyrds.generated;\n\n")
				.append("public class GeneratedClass {\n\n") // open class
				.append("\tpublic String getMessage() {\n") // open method
				.append("\t\treturn \"");


		// for each javax.lang.model.element.Element annotated with the CustomAnnotation
		for (Element element : roundEnvironment.getElementsAnnotatedWith(Packable.class)) {
			String objectType = element.getSimpleName().toString();

			// this is appending to the return statement
			builder.append(objectType).append(" says hello!\\n");

		}


		builder.append("\";\n") // end return
				.append("\t}\n") // close method
				.append("}\n"); // close class



		try { // write the file
			JavaFileObject source = processingEnv.getFiler().createSourceFile("com.nyrds.generated.GeneratedClass");


			Writer writer = source.openWriter();
			writer.write(builder.toString());
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
