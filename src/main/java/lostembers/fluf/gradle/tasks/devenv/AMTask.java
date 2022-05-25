package lostembers.fluf.gradle.tasks.devenv;

import lostembers.fluf.gradle.FlufProject;
import lostembers.fluf.gradle.tasks.devenv.am.AMEntry;
import lostembers.fluf.gradle.tasks.generic.FlufTask;
import lostembers.fluf.gradle.threading.ThreadObjectPool;
import lostembers.fluf.gradle.threading.ThreadPool;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class AMTask extends FlufTask {
	public FlufProject project;
	
	@Override
	public void run() {
		try {
			File buildDir = project.project.getBuildDir();
			File projDir = project.project.getProjectDir();
			// TODO: better support for stuff
			File fl1 = new File(projDir + "/src/main/resources/named.am");
			if (!fl1.exists()) return;
			
			FileInputStream inputStream = new FileInputStream(fl1);
			byte[] bytes1 = inputStream.readAllBytes();
			inputStream.close();
			
			ArrayList<AMEntry> entries = new ArrayList<>();
			
			String str = new String(bytes1);
			for (String s : str.split("\n")) {
				s = s.replace("\r", ""); // windows moment
				System.out.println(s);
				String[] split = s.split(":");
				String modif = split[0];
				String entry = split[1];
				
				System.out.println(modif);
				System.out.println(entry);
				
				split = entry.split("#");
				String cname = null;
				String ename;
				if (split.length == 1) {
					ename = split[0];
				} else {
					cname = split[0];
					if (cname.equals("")) cname = null;
					ename = split[1];
				}
				// TODO: methods
				
				AMEntry entry1 = new AMEntry(modif, cname, ename);
				entries.add(entry1);
			}
			
			File amJar = new File(buildDir + "/fluf_gradle/post_am.jar");
			String jarDir = project.getGameJarPath();
			File fl = new File(jarDir + ".jar").getAbsoluteFile();
			
			JarFile file = new JarFile(fl);
			boolean[] anyChanged = new boolean[]{false};
			ThreadPool pool = new ThreadPool(project.settings.remapMCThreads);
			ThreadObjectPool<HashMap<String, byte[]>> classes = new ThreadObjectPool<>(HashMap::new);
			file.entries().asIterator().forEachRemaining(entry -> {
				try {
					InputStream stream = file.getInputStream(entry);
					byte[] bytes = stream.readAllBytes();
					stream.close();
					pool.startNext(() -> {
						ClassNode node = new ClassNode();
						ClassReader reader = new ClassReader(bytes);
						reader.accept(node, ClassReader.EXPAND_FRAMES);
						
						boolean hasChanged = false;
						
						// this is mess
						// TODO: make it not be mess
						for (FieldNode field : node.fields) {
							for (AMEntry amEntry : entries) {
								if (amEntry.matches(node.name, field.name, null)) {
									int newAcc = 0;
									if (Modifier.isStatic(field.access)) newAcc = newAcc | Modifier.STATIC;
									if (Modifier.isAbstract(field.access)) newAcc = newAcc | Modifier.ABSTRACT;
									if (Modifier.isStrict(field.access)) newAcc = newAcc | Modifier.STRICT;
									if (Modifier.isFinal(field.access)) newAcc = newAcc | Modifier.FINAL;
									if (Modifier.isInterface(field.access)) newAcc = newAcc | Modifier.INTERFACE;
									if (Modifier.isVolatile(field.access)) newAcc = newAcc | Modifier.VOLATILE;
									if (Modifier.isNative(field.access)) newAcc = newAcc | Modifier.NATIVE;
									if (Modifier.isSynchronized(field.access)) newAcc = newAcc | Modifier.SYNCHRONIZED;
									if (Modifier.isTransient(field.access)) newAcc = newAcc | Modifier.TRANSIENT;
									if (amEntry.level.equals("public") && (Modifier.isPrivate(field.access) || Modifier.isProtected(field.access))) newAcc = newAcc | Modifier.PUBLIC;
									if (amEntry.level.equals("protected") && Modifier.isPrivate(field.access)) newAcc = newAcc | Modifier.PROTECTED;
									if (amEntry.level.equals("private")) newAcc = newAcc | Modifier.PRIVATE;
									hasChanged = hasChanged || (newAcc != field.access);
									field.access = newAcc;
								}
							}
						}
						
						if (hasChanged) {
							ClassWriter writer = new ClassWriter(0);
							node.accept(writer);
							byte[] bytes2 = writer.toByteArray();
							classes.get().put(node.name, bytes2);
							anyChanged[0] = true;
						} else classes.get().put(node.name, bytes);
					});
				} catch (Throwable ignored) {
				}
			});
			pool.await();
			pool.kill();
			
			if (anyChanged[0]) {
				if (amJar.exists()) amJar.delete();
				if (!amJar.exists()) {
					amJar.getParentFile().mkdirs();
					amJar.createNewFile();
				}
				FileOutputStream remappedStream = new FileOutputStream(amJar.getAbsoluteFile());
				JarOutputStream out = new JarOutputStream(remappedStream);
				for (HashMap<String, byte[]> value : classes.getValues()) {
					for (String s : value.keySet()) {
						out.putNextEntry(new JarEntry(s + ".class"));
						out.write(value.get(s));
						out.closeEntry();
					}
				}
				out.close();
				out.flush();
			}
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException("");
		}
	}
}
