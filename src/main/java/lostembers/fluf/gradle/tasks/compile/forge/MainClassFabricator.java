package lostembers.fluf.gradle.tasks.compile.forge;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class MainClassFabricator {
	public static void fabricateMain(File buildDir, JsonObject forgeSpecification, JsonObject entries, String modid) throws IOException {
		String str = buildDir + "/";
		String cname = forgeSpecification.get("main").getAsString();
		str += "classes/java/main/" + cname + ".class";
		System.err.println(buildDir);
		ClassNode node = new ClassNode();
		node.version = Opcodes.V1_8;
		node.name = cname;
		node.superName = "java/lang/Object";
		// TODO: should this be visible..?
		node.visibleAnnotations = new ArrayList<>();
		AnnotationNode node1 = new AnnotationNode("Lnet/minecraftforge/fml/common/Mod;");
		node1.values = new ArrayList<>();
		node1.values.add("value");
		node1.values.add(modid);
		node.visibleAnnotations.add(node1);
		node.access = Modifier.PUBLIC;
		MethodNode init = new MethodNode(
				Opcodes.ASM9, "<init>", "()V",
				null, null
		);
		init.instructions = new InsnList();
		init.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
		init.instructions.add(new MethodInsnNode(
				Opcodes.INVOKESPECIAL,
				"java/lang/Object", "<init>", "()V"
		));
		init.access = Modifier.PUBLIC;
		// TODO: other entries
		if (entries.has("common")) {
			JsonArray common = entries.getAsJsonArray("common");
			for (JsonElement element : common) {
				init.instructions.add(new TypeInsnNode(Opcodes.NEW, element.getAsString()));
				init.instructions.add(new InsnNode(Opcodes.DUP));
				init.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, element.getAsString(), "<init>", "()V"));
				init.instructions.add(new InsnNode(Opcodes.POP));
			}
		}
		init.instructions.add(new InsnNode(Opcodes.RETURN));
		node.methods.add(init);
		
		File f = new File(str);
		if (!f.exists()) {
			f.getParentFile().mkdirs();
			f.createNewFile();
		}
		
		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
		node.accept(writer);
		FileOutputStream outputStream = new FileOutputStream(f);
		outputStream.write(writer.toByteArray());
		outputStream.close();
		outputStream.flush();
	}
}
