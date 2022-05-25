package lostembers.fluf.gradle.tasks.run;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lostembers.fluf.gradle.FlufProject;
import lostembers.fluf.gradle.settings.Settings;
import lostembers.fluf.gradle.tasks.generic.FlufTask;
import lostembers.fluf.gradle.threading.ThreadObjectPool;
import lostembers.fluf.gradle.threading.ThreadPool;
import lostembers.fluf.gradle.util.DependencySpec;
import lostembers.fluf.gradle.util.Hierarchy;
import lostembers.fluf.gradle.util.mappings.Mojmap;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.jar.JarFile;

public class RunFabric extends FlufTask {
	String url = "https://maven.fabricmc.net/net/fabricmc/fabric-loader/%version%/fabric-loader-%version%.json";
	String urlLoader = "https://maven.fabricmc.net/net/fabricmc/fabric-loader/%version%/fabric-loader-%version%.jar";
	Gson gson = new Gson();
	public FlufProject project;
	
	@Override
	public void run() {
		ArrayList<DependencySpec> specs = new ArrayList<>();
		Settings settings = project.settings;
		JsonObject object = gson.fromJson(Mojmap.readUrl(url.replace("%version%", settings.fabricVersion)), JsonObject.class);
		specs.addAll(Arrays.asList(project.dependencies));
		JsonObject libs = object.get("libraries").getAsJsonObject();
		{
			JsonArray array = new JsonArray();
			{
				JsonObject obj = new JsonObject();
				obj.addProperty("name", "net.fabricmc:fabric-loader:" + settings.fabricVersion);
				obj.addProperty("url", "https://maven.fabricmc.net/");
				array.add(obj);
			}
			for (DependencySpec spec : specs) {
				JsonObject obj = new JsonObject();
				obj.addProperty("name", spec.name);
				obj.addProperty("url", spec.maven.toString());
				array.add(obj);
			}
			libs.add("fluf", array);
		}
		String targetDir = "fluf_gradle/" + settings.getVersion() + "/" + settings.getMappings() + "/";
		String srcDir = "fluf_gradle/" + settings.getVersion() + "/fabric/";
		{
			File fl = new File(srcDir);
			fl.mkdirs();
			fl = new File(targetDir);
			fl.mkdirs();
		}
		
		ArrayList<File> deps = new ArrayList<>();
		for (String s : libs.keySet()) {
			for (JsonElement element : libs.getAsJsonArray(s)) {
				try {
					JsonObject object1 = element.getAsJsonObject();
					String url = object1.get("url").getAsString();
					String name = object1.get("name").getAsString();
					String[] split = name.split(":");
					url = url +
							split[0].replace(".", "/") + "/" +
							split[1] + "/" +
							split[2] + "/" +
							split[1] + "-" + split[2] + ".jar"
					;
					File fl = new File(srcDir + split[1] + "-" + split[2] + ".jar").getAbsoluteFile();
					if (!fl.exists()) {
						BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(fl));
						URL url1 = new URL(url);
						URLConnection connection = url1.openConnection();
						InputStream stream = connection.getInputStream();
						int b;
						// TODO: this is slow
						while ((b = stream.read()) != -1) outputStream.write(b);
						outputStream.close();
						outputStream.flush();
						stream.close();
					}
					deps.add(fl);
				} catch (Throwable err) {
					System.err.println("Failed while downloading a dependency of fabric loader: " + element.getAsJsonObject().get("name"));
					err.printStackTrace();
				}
			}
		}
		String mappedJarDir = project.getGameJarPath();
		File fl = new File(mappedJarDir + ".jar").getAbsoluteFile();
		deps.add(fl);
		URL[] urls = new URL[deps.size()];
		try {
			for (int i = 0; i < deps.size(); i++) urls[i] = deps.get(i).toURL();
		} catch (Throwable ignored) {
		}
		Class<?> clazz;
		try {
			URLClassLoader classLoader = new URLClassLoader(urls);
			String main = object.getAsJsonObject("mainClass").getAsJsonPrimitive("client").getAsString();
			clazz = classLoader.loadClass(main);
		} catch (Throwable err) {
			System.err.println("Failed to find fabric's main class");
			err.printStackTrace();
			return;
		}
		try {
			System.setProperty("fabric.gameJarPath", fl.toString());
			System.setProperty("fabric.side", "client");
			System.setProperty("fabric.development", "true");
//			String cp = System.getProperty("java.class.path");
			System.setProperty("java.class.path", fl.toString().replace("/", File.separator));
			System.setProperty("fabric.debug.disableClassPathIsolation", "true");
			clazz.getMethod("main", String[].class).invoke(
					null,
					// TODO: make this configurable
					(Object) new String[]{
					}
			);
		} catch (Throwable err) {
			System.err.println("Failed while running Minecraft:");
			err.printStackTrace();
		}
	}
	
	// TODO: finish this
	public void remapDeps(ArrayList<File> deps) {
		// TODO: should this be configurable?
		ThreadPool pool0 = new ThreadPool(5);
		JarFile[] jars = new JarFile[deps.size()];
		// partially non-blocking loop to read jar headers
		for (int i = 0; i < deps.size(); i++) {
			final int ic = i;
			pool0.startNext(() -> {
				try {
					jars[ic] = new JarFile(deps.get(ic));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		
		HashMap<JarFile, HashMap<String, byte[]>> fileContents = new HashMap<>();
		{
			ThreadObjectPool<HashMap<JarFile, HashMap<String, byte[]>>> fileContentsPool = new ThreadObjectPool<>(HashMap::new);
			// partially non-blocking loop to read entries
			for (int i = 0; i < jars.length; i++) {
				final int ic = i;
				// if jar is null, await it
				while (jars[ic] == null) {
					try {
						Thread.sleep(1);
					} catch (Throwable ignored) {
					}
				}
				pool0.startNext(() -> {
					JarFile fl = jars[ic];
					
					HashMap<String, byte[]> entries = new HashMap<>();
					fl.entries().asIterator().forEachRemaining((entry) -> {
						try {
							InputStream stream = fl.getInputStream(entry);
							byte[] bytes = stream.readAllBytes();
							stream.close();
							entries.put(entry.getName(), bytes);
						} catch (Throwable ignored) {
						}
					});
					fileContentsPool.get().put(fl, entries);
				});
			}
			pool0.await();
			for (HashMap<JarFile, HashMap<String, byte[]>> value : fileContentsPool.getValues()) {
				for (JarFile file : value.keySet()) {
					fileContents.put(file, value.get(file));
				}
			}
		}
		
		Hierarchy hierarchy = new Hierarchy();
		{
			ThreadObjectPool<Hierarchy> hierarchyThreadObjectPool = new ThreadObjectPool<>(Hierarchy::new);
			ThreadObjectPool<ThreadPool> pools = new ThreadObjectPool<>(() -> new ThreadPool(10));
			// generate hierarchy listings
			for (int i = 0; i < jars.length; i++) {
				final int ic = i;
				pool0.startNext(() -> {
					JarFile file = jars[ic];
					HashMap<String, byte[]> entries;
					synchronized (fileContents) {
						entries = fileContents.get(file);
					}
					for (String s : entries.keySet()) {
						byte[] bytes = entries.get(s);
						
						ClassReader reader = new ClassReader(bytes);
						ClassNode nd = new ClassNode();
						reader.accept(nd, ClassReader.EXPAND_FRAMES);
						hierarchyThreadObjectPool.get().accept(nd);
					}
				});
			}
			pool0.await();
			// merge them
			StringBuilder builder = new StringBuilder();
			for (Hierarchy value : hierarchyThreadObjectPool.getValues()) builder.append(value.toString());
			hierarchy.read(builder.toString());
		}
		
		
	}
}
