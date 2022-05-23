package lostembers;

import lostembers.fluf.gradle.BotchedURLClassLoader;
import lostembers.fluf.gradle.FlufPlugin;
import lostembers.fluf.gradle.util.mappings.FlufMappings;
import lostembers.fluf.gradle.util.mappings.Mojmap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class MojmapTest {
	public static void main(String[] args) {
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
				urls[i] = new File("C:/Users/Owner/.gradle/daemon/7.3/" + files[i]).toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		URLClassLoader cloader = new BotchedURLClassLoader(urls, FlufPlugin.class.getClassLoader());
		try {
			Class<?> clazz = cloader.loadClass("lostembers.MojmapTest");
			clazz.getConstructor(Object.class).newInstance(new Object());
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
	
	public MojmapTest() {
	}
	
	public MojmapTest(Object obj) {
		System.out.println(MojmapTest.class.getClassLoader());
		FlufMappings mappings = Mojmap.parseMappings("1.18.2");
		System.out.println(mappings);
	}
}
