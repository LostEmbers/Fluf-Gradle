package lostembers.fluf.gradle.tasks.devenv;

import lostembers.fluf.gradle.FlufProject;
import lostembers.fluf.gradle.FlufRemapper;
import lostembers.fluf.gradle.RemapperStack;
import lostembers.fluf.gradle.tasks.generic.FlufTask;
import lostembers.fluf.gradle.threading.ThreadObjectPool;
import lostembers.fluf.gradle.threading.ThreadPool;
import lostembers.fluf.gradle.util.Hierarchy;
import lostembers.fluf.gradle.util.mappings.Mojmap;
import lostembers.fluf.gradle.util.mappings.Tsrg2;
import net.fabricmc.mappings.MappingsProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public abstract class RemapMCJarTask extends FlufTask {
	public FlufProject project;
	public File buildDir;
	
	public void run() {
		File dir = project.project.getBuildDir();
		String targetDir = project.getGameJarPath();
		File fl = new File(targetDir + ".jar").getAbsoluteFile();
		File src = new File(dir + "/1.18.2.jar"); // TODO: make this more convenient
		try {
			JarFile file = new JarFile(src);
			ThreadPool pool = new ThreadPool(project.settings.remapMCThreads);
			ThreadObjectPool<HashMap<String, byte[]>> objectPool = new ThreadObjectPool<>(HashMap::new);
			ThreadObjectPool<ArrayList<String>> dirs = new ThreadObjectPool<>(ArrayList::new);
			ThreadObjectPool<ArrayList<String>> fails = new ThreadObjectPool<>(ArrayList::new);
			
			final Remapper remapper = switch (project.settings.getMappings()) {
				case "intermediary" -> new FlufRemapper(
						MappingsProvider.readTinyMappings(new URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + project.settings.getVersion() + ".tiny").openStream()),
						true
				);
				case "tsrg2" -> new FlufRemapper(
						Tsrg2.parseMappings(project.settings.getVersion()),
						true
				);
				case "mojmap" -> new FlufRemapper(
						Mojmap.parseMappings(project.settings.getVersion()),
						true, true
				);
				default -> new Remapper() {
				};
			};
			
			Hierarchy hierarchy = new Hierarchy();
			{
				// generate hierarchy
				ThreadObjectPool<Hierarchy> hierarchies = new ThreadObjectPool<>(Hierarchy::new);
				ThreadObjectPool<Hierarchy> hierarchiesObf = new ThreadObjectPool<>(Hierarchy::new);
				pool.completeAsync(
						() -> file.entries().asIterator().forEachRemaining(jarEntry -> {
							if (jarEntry.getName().endsWith(".class")) {
								try {
									InputStream stream = file.getInputStream(jarEntry);
									byte[] bytes = stream.readAllBytes();
									stream.close();
									pool.startNext(() -> {
										ClassNode node = new ClassNode();
										ClassReader reader = new ClassReader(bytes);
										reader.accept(node, ClassReader.EXPAND_FRAMES);
										hierarchiesObf.get().accept(node);
										String name = remapper.map(node.name);
										ArrayList<String> inherit = new ArrayList<>();
										for (String anInterface : node.interfaces)
											inherit.add(remapper.map(anInterface));
										inherit.add(remapper.map(node.superName));
										hierarchies.get().add(name, inherit);
									});
								} catch (Throwable ignored) {
								}
							}
						})
				);
				StringBuilder txt = new StringBuilder();
				for (Hierarchy value : hierarchies.getValues()) {
					txt.append(value.toString());
				}
				hierarchy.read(txt.toString());
				
				Hierarchy hierarchyObf = new Hierarchy();
				txt = new StringBuilder();
				for (Hierarchy value : hierarchiesObf.getValues()) {
					txt.append(value.toString());
				}
				hierarchyObf.read(txt.toString());
				if (remapper instanceof FlufRemapper) ((FlufRemapper) remapper).hierarchy = hierarchyObf;
				else if (remapper instanceof RemapperStack) ((RemapperStack) remapper).setHierarchy(hierarchyObf);
			}
			
			AtomicInteger countDone = new AtomicInteger();
			int[] count = new int[]{0};
			ArrayList<String> entries = new ArrayList<>();
			file.entries().asIterator().forEachRemaining(entry -> {
				if (entry.getName().endsWith(".class")) {
					count[0] = count[0] + 1;
					entries.add(entry.getName());
				}
			});
			final Object lock = new Object();
			ThreadObjectPool<ArrayList<String>> processed = new ThreadObjectPool<>(ArrayList::new);
			pool.completeAsync(
					// TODO: figure out why some classes get missed
					() -> file.entries().asIterator().forEachRemaining(jarEntry -> {
						if (jarEntry.isDirectory()) {
							dirs.get().add(jarEntry.getName());
							return;
						}
						if (jarEntry.getName().endsWith(".class")) {
							countDone.getAndIncrement();
							try {
								InputStream stream = file.getInputStream(jarEntry);
								byte[] bytes = stream.readAllBytes();
								stream.close();
								pool.startNext(() -> {
									for (int i = 0; i < 10; i++) {
										try {
											ClassNode node = new ClassNode();
											ClassReader reader = new ClassReader(bytes);
											reader.accept(node, ClassReader.EXPAND_FRAMES);
											ClassNode remapped = new ClassNode();
											ClassRemapper cmapper = new ClassRemapper(remapped, remapper);
											node.accept(cmapper);
//											ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
											ClassWriter out = new ClassWriter(0);
											String name = remapped.name;
											remapped.accept(out);
											objectPool.get().put(name.replace(".", "/") + ".class", out.toByteArray());
											processed.get().add(jarEntry.getName());
											return;
										} catch (Throwable err) {
											synchronized (lock) {
												// TODO: fix this
												System.err.println("Failed while remapping file: " + jarEntry.getName());
												err.printStackTrace();
											}
										}
									}
									fails.get().add(jarEntry.getName());
									processed.get().add(jarEntry.getName());
								});
							} catch (Throwable err) {
								err.printStackTrace();
							}
							System.out.println("Remapping: " + jarEntry.getName() + ", " + countDone.get() + "/" + count[0]);
						}
					})
			);
			System.out.println("Finished iterating over files");
			pool.kill();
			if (fl.exists()) fl.delete();
			if (!fl.exists()) {
				fl.getParentFile().mkdirs();
				fl.createNewFile();
			}
			FileOutputStream remappedStream = new FileOutputStream(fl.getAbsoluteFile());
			JarOutputStream out = new JarOutputStream(remappedStream);
			int c = 0;
			for (HashMap<String, byte[]> value : objectPool.getValues()) {
				for (String s : value.keySet()) {
					out.putNextEntry(new JarEntry(s));
					out.write(value.get(s));
					out.closeEntry();
					c += 1;
				}
			}
			System.out.println(c + "/" + count[0]);
			out.close();
			out.flush();
			
			{
				File fl1 = new File(targetDir.replace("mapped", "hierarchy") + ".txt").getAbsoluteFile();
				if (fl1.exists()) {
					fl1.delete();
					fl1.createNewFile();
				}
				FileOutputStream outputStream = new FileOutputStream(fl1);
				outputStream.write(hierarchy.toString().getBytes());
				outputStream.close();
				outputStream.flush();
			}
			{
				File fl1 = new File(targetDir.replace("mapped", "log") + ".txt").getAbsoluteFile();
				if (fl1.exists()) {
					fl1.delete();
					fl1.createNewFile();
				}
				StringBuilder txt = new StringBuilder();
				txt.append("Remapped: ").append(c).append("/").append(count[0]).append(" classes\n");
				boolean failedAny = !fails.getValues().isEmpty();
				if (failedAny) {
					txt.append("Failed on the following classes:\n");
					for (ArrayList<String> value : fails.getValues()) {
						for (String s : value) {
							txt.append("\t").append(s).append("\n");
						}
					}
				}
				// TODO: don't append this if nothing was missed
				txt.append("Missed the following classes:\n");
				loopEntries:
				for (String entry : entries) {
					for (ArrayList<String> value : processed.getValues()) {
						if (value.contains(entry)) {
							continue loopEntries;
						}
					}
					txt.append("\t").append(entry).append("\n");
				}
				FileOutputStream outputStream = new FileOutputStream(fl1);
				outputStream.write(txt.toString().getBytes());
				outputStream.close();
				outputStream.flush();
			}
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}
}
