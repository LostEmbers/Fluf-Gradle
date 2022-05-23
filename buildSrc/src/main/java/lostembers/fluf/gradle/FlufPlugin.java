package lostembers.fluf.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class FlufPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		// TODO: fluf dependency
		
		String str = """
				fluf_gradle/libs/tiny-mappings-parser-0.2.2.14.jar
				fluf_gradle/libs/asm-util-9.3.jar
				fluf_gradle/libs/asm-commons-9.3.jar
				fluf_gradle/libs/asm-analysis-9.3.jar
				fluf_gradle/libs/asm-tree-9.3.jar
				fluf_gradle/libs/asm-9.3.jar
				fluf_gradle/libs/asm-util-9.3.jar
				fluf_gradle/libs/asm-commons-9.3.jar
				fluf_gradle/libs/asm-analysis-9.3.jar
				fluf_gradle/libs/asm-tree-9.3.jar
				fluf_gradle/libs/asm-9.3.jar
				fluf_gradle/libs/asm-util-9.3.jar
				fluf_gradle/libs/asm-commons-9.3.jar
				fluf_gradle/libs/asm-analysis-9.3.jar
				fluf_gradle/libs/asm-tree-9.3.jar
				fluf_gradle/libs/asm-9.3.jar
				fluf_gradle/libs/gson-2.9.0.jar
				""".replace("\t", "");
		String[] files = str.split("\n");
		URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				urls[i] = new File(files[i]).toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		URLClassLoader cloader = new BotchedURLClassLoader(urls, FlufPlugin.class.getClassLoader());
		try {
			Class<?> clazz = cloader.loadClass("lostembers.fluf.gradle.FlufProject");
			clazz.getConstructor(Project.class).newInstance(project);
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
}
