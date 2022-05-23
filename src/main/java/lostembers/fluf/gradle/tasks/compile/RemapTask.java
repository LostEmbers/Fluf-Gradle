package lostembers.fluf.gradle.tasks.compile;

import lostembers.fluf.gradle.FlufRemapper;
import lostembers.fluf.gradle.NameOnlyMojmapRemapper;
import lostembers.fluf.gradle.RemapperStack;
import lostembers.fluf.gradle.settings.Loader;
import lostembers.fluf.gradle.settings.Settings;
import lostembers.fluf.gradle.tasks.generic.FlufTask;
import lostembers.fluf.gradle.threading.ThreadPool;
import lostembers.fluf.gradle.util.mappings.FlufMappings;
import lostembers.fluf.gradle.util.mappings.Mojmap;
import lostembers.fluf.gradle.util.mappings.Tsrg2;
import net.fabricmc.mappings.MappingsProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

public abstract class RemapTask extends FlufTask {
	public File buildDir = null;
	//	protected final FlufRemapper remapper = new FlufRemapper();
	protected Remapper remapper;
	public FlufMappings tsrg;
	public Settings settings;
	protected ThreadPool pool;
	
	protected void walk(File root, Consumer<File> action) {
		for (File file : root.listFiles()) {
			if (file.isDirectory()) {
				walk(file, action);
			} else {
				// little bit of parallelism
				pool.startNext(() -> action.accept(file));
			}
		}
	}
	
	public void setup(String src) {
		try {
			if (Loader.target == null) return;
			RemapperStack stack = new RemapperStack();
			if (!src.equals("obsfucation")) {
				if (src.equals("intermediary")) {
					remapper = new FlufRemapper(
							MappingsProvider.readTinyMappings(new URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + settings.getVersion() + ".tiny").openStream()),
							false
					);
				} else if (src.equals("tsrg2")) {
					remapper = new FlufRemapper(
							Tsrg2.parseMappings(settings.getVersion()),
							false
					);
				} else if (src.equals("mojmap")) {
					remapper = new FlufRemapper(
							Mojmap.parseMappings(settings.getVersion()),
							false, true
					);
				}
				stack.add(remapper);
			}
			if (!Loader.target.equals("vanilla")) {
				if (Loader.target.equals("forge")) {
					remapper = new FlufRemapper(
							Tsrg2.parseMappings(settings.getVersion()),
							true, false, true
					);
					stack.add(remapper);
					remapper = new NameOnlyMojmapRemapper(Mojmap.parseMappings(settings.getVersion()), true);
				} else if (Loader.target.equals("fabric")) {
					remapper = new FlufRemapper(
							MappingsProvider.readTinyMappings(new URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + settings.getVersion() + ".tiny").openStream()),
							true
					);
				}
				stack.add(remapper);
			}
			remapper = stack;
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException("Failed to generate remapper");
		}
	}

//	public final Mappings intermediary;
	
	public RemapTask() {
//		try {
//			// TODO: make sure this closes the stream
////			intermediary = MappingsProvider.readTinyMappings(new URL("https://raw.githubusercontent.com/FabricMC/intermediary/master/mappings/" + Defaults.GAME_VERSION + ".tiny").openStream());
////			tsrg = Tsrg2.parseMappings(Defaults.GAME_VERSION);
////			remapper = new FlufRemapper(intermediary, false);
//		} catch (Throwable ignored) {
//			ignored.printStackTrace();
//			throw new RuntimeException("");
//		}
	}
	
	public void run() {
		pool = new ThreadPool(settings.remapModThreads);
		if (remapper == null) return;
		File classesDir = new File(buildDir + "/classes");
		walk(classesDir, (file) -> {
			try {
				ClassReader reader = new ClassReader(new FileInputStream(file));
				ClassNode nd = new ClassNode();
				reader.accept(nd, ClassReader.EXPAND_FRAMES);
				ClassWriter out = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
				ClassRemapper cmapper = new ClassRemapper(out, remapper);
				nd.accept(cmapper);
				FileOutputStream outputStream = new FileOutputStream(file);
				outputStream.write(out.toByteArray());
				outputStream.close();
			} catch (IOException err) {
				err.printStackTrace();
				System.err.println("An error occurred while trying to remap classes.");
				System.err.println("Perhaps `gradle --stop` needs to be run?");
			}
		});
		pool.await();
		pool.kill();
	}
}
