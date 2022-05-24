package lostembers.fluf.gradle.tasks.compile.fabric;

import lostembers.fluf.gradle.tasks.compile.common.APICallMutator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.reflect.Modifier;

public class FabricRegistryMutator extends APICallMutator {
	private static final ClassNode templateNode;
	
	static {
		try {
			InputStream stream = FabricRegistryMutator.class.getClassLoader().getResourceAsStream("fabric/FlufRegister.class");
			byte[] bytes = stream.readAllBytes();
			stream.close();
			
			templateNode = new ClassNode();
			ClassReader reader = new ClassReader(bytes);
			reader.accept(templateNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
		} catch (Throwable err) {
			err.printStackTrace();
			throw new RuntimeException("Failed to read the Registry class from Fluf API");
		}
	}
	
	@Override
	public void handle(AbstractInsnNode target, InsnList instructions, ClassNode nd) {
		((MethodInsnNode) target).owner = nd.name;
		((MethodInsnNode) target).name = "FlufAPI::register";
		for (MethodNode method : nd.methods) {
			if (method.name.equals("FlufAPI::register")) {
				break;
			}
		}
		// TODO: get rid of this
		// this should be inlined with the actual call
		for (MethodNode method : templateNode.methods) {
			if (method.name.startsWith("lambda")) {
				method.access = Modifier.PRIVATE | Modifier.STATIC | Opcodes.ACC_SYNTHETIC;
				method.name = method.name.replace("$register$", "$_$_flufapi$register");
				for (AbstractInsnNode instruction : method.instructions) {
					if (instruction instanceof MethodInsnNode) {
						if (((MethodInsnNode) instruction).owner.equals("lostembers/fluf/api/FlufRegister")) {
							((MethodInsnNode) instruction).owner = nd.name;
						}
					}
				}
				nd.methods.add(method);
			} else if (method.name.equals("register") || method.name.equals("FlufAPI::register")) {
				method.access = Modifier.PRIVATE | Modifier.STATIC | Opcodes.ACC_SYNTHETIC;
				for (AbstractInsnNode instruction : method.instructions) {
					if (instruction instanceof MethodInsnNode) {
						if (((MethodInsnNode) instruction).owner.equals("lostembers/fluf/api/FlufRegister")) {
							((MethodInsnNode) instruction).owner = nd.name;
							((MethodInsnNode) instruction).name = ((MethodInsnNode) instruction).name.replace("$register$", "$_$_flufapi$register");
						}
					} else if (instruction instanceof InvokeDynamicInsnNode) {
						for (int i = 0; i < ((InvokeDynamicInsnNode) instruction).bsmArgs.length; i++) {
							Object a = ((InvokeDynamicInsnNode) instruction).bsmArgs[i];
							if (a instanceof Handle) {
								if (((Handle) a).getOwner().equals("lostembers/fluf/api/FlufRegister")) {
									Handle newHandle = new Handle(
											((Handle) a).getTag(),
											nd.name, ((Handle) a).getName().replace("$register$", "$_$_flufapi$register"),
											((Handle) a).getDesc(), ((Handle) a).isInterface()
									);
									((InvokeDynamicInsnNode) instruction).bsmArgs[i] = newHandle;
								}
							}
						}
					}
				}
				method.name = "FlufAPI::register";
				nd.methods.add(method);
			}
		}
	}
	
	@Override
	public void handle(AbstractInsnNode target, InsnList instructions) {
	}
	
	@Override
	public boolean processesCall(String className, String functionName) {
		return className.equals("lostembers/fluf/api/FlufRegister") && functionName.equals("register");
	}
}
