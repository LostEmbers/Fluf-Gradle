package lostembers.fluf.gradle;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;

public class BotchedURLClassLoader extends URLClassLoader {
	public BotchedURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}
	
	public BotchedURLClassLoader(URL[] urls) {
		super(urls);
	}
	
	public BotchedURLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(urls, parent, factory);
	}
	
	public BotchedURLClassLoader(String name, URL[] urls, ClassLoader parent) {
		super(name, urls, parent);
	}
	
	public BotchedURLClassLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
		super(name, urls, parent, factory);
	}
	
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return findClass(className);
	}
	
	@Override
	protected Class<?> loadClass(String className, boolean resolveClass) throws ClassNotFoundException {
		return findClass(className);
	}
	
	HashMap<String, Class<?>> classes = new HashMap<>();
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (
				name.startsWith("org.objectweb") ||
						name.startsWith("net.fabricmc") ||
						name.startsWith("com.google")
		) {
			if (!classes.containsKey(name)) {
				try {
					InputStream stream = findResource(name.replace(".", "/") + ".class").openStream();
					byte[] bytes;
					Class<?> clazz = defineClass(bytes = stream.readAllBytes(), 0, bytes.length);
					stream.close();
					classes.put(name, clazz);
					return classes.get(name);
				} catch (Throwable ignored) {
				}
			}
		}
		if (name.startsWith("lostembers")) {
			if (!classes.containsKey(name)) {
				try {
					InputStream stream = getParent().getResourceAsStream(name.replace(".", "/") + ".class");
					byte[] bytes;
					Class<?> clazz = defineClass(bytes = stream.readAllBytes(), 0, bytes.length);
					stream.close();
					classes.put(name, clazz);
					return classes.get(name);
				} catch (Throwable ignored) {
					return null;
				}
			}
		}
//		return super.findClass(name);
		if (!classes.containsKey(name)) classes.put(name, super.loadClass(name, false));
		return classes.get(name);
	}
}
