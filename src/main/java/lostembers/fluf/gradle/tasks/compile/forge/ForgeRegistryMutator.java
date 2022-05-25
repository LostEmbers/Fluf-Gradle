package lostembers.fluf.gradle.tasks.compile.forge;

import lostembers.fluf.gradle.tasks.compile.common.APICallMutator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class ForgeRegistryMutator extends APICallMutator {
	private ClassNode templateNode;
	
	public ForgeRegistryMutator() {
		reset();
	}
	
	@Override
	public void handle(AbstractInsnNode target, InsnList instructions, ClassNode nd) {
		((MethodInsnNode) target).owner = nd.name;
		((MethodInsnNode) target).name = "FlufAPI::register";
		for (MethodNode method : nd.methods) {
			if (method.name.equals("FlufAPI::register")) {
				return;
			}
		}
		// TODO: get rid of this
		// this should be inlined with the actual call
		int ac = Modifier.PRIVATE | Modifier.STATIC | Opcodes.ACC_SYNTHETIC;
		for (FieldNode field : templateNode.fields) {
			field.access = ac;
			if (!field.name.startsWith("FlufAPI::")) field.name = "FlufAPI::" + field.name;
			nd.fields.add(field);
			break;
		}
		for (MethodNode method : templateNode.methods) {
			if (method.name.startsWith("lambda")) {
				method.access = ac;
				method.name = method.name.replace("$register$", "$FlufAPI::register$");
				for (AbstractInsnNode instruction : method.instructions) {
					if (instruction instanceof MethodInsnNode) {
						if (((MethodInsnNode) instruction).owner.equals("lostembers/fluf/api/FlufRegister")) {
							((MethodInsnNode) instruction).owner = nd.name;
						}
					}
				}
				nd.methods.add(method);
			} else if (method.name.equals("register") || method.name.equals("FlufAPI::register")) {
				method.access = ac;
				for (AbstractInsnNode instruction : method.instructions) {
					if (instruction instanceof MethodInsnNode) {
						if (((MethodInsnNode) instruction).owner.equals("lostembers/fluf/api/FlufRegister")) {
							((MethodInsnNode) instruction).owner = nd.name;
							((MethodInsnNode) instruction).name = ((MethodInsnNode) instruction).name.replace("$register$", "$FlufAPI::register$");
						}
					} else if (instruction instanceof InvokeDynamicInsnNode) {
						for (int i = 0; i < ((InvokeDynamicInsnNode) instruction).bsmArgs.length; i++) {
							Object a = ((InvokeDynamicInsnNode) instruction).bsmArgs[i];
							if (a instanceof Handle) {
								if (((Handle) a).getOwner().equals("lostembers/fluf/api/FlufRegister")) {
									Handle newHandle = new Handle(
											((Handle) a).getTag(),
											nd.name, ((Handle) a).getName().replace("$register$", "$FlufAPI::register$"),
											((Handle) a).getDesc(), ((Handle) a).isInterface()
									);
									((InvokeDynamicInsnNode) instruction).bsmArgs[i] = newHandle;
								}
							}
						}
					} else if (instruction instanceof FieldInsnNode) {
						if (((FieldInsnNode) instruction).owner.startsWith("lostembers/fluf/api/FlufRegister")) {
							((FieldInsnNode) instruction).owner = nd.name;
							if (!((FieldInsnNode) instruction).name.startsWith("FlufAPI::"))
								((FieldInsnNode) instruction).name = "FlufAPI::" + ((FieldInsnNode) instruction).name;
						}
					}
				}
				method.name = "FlufAPI::register";
				nd.methods.add(method);
			} else if (method.name.equals("<clinit>")) {
				for (AbstractInsnNode instruction : method.instructions) {
					if (instruction instanceof FieldInsnNode) {
						if (((FieldInsnNode) instruction).owner.startsWith("lostembers/fluf/api/FlufRegister")) {
							((FieldInsnNode) instruction).owner = nd.name;
							if (!((FieldInsnNode) instruction).name.startsWith("FlufAPI::"))
								((FieldInsnNode) instruction).name = "FlufAPI::" + ((FieldInsnNode) instruction).name;
						}
					}
				}
				boolean foundCinit = false;
				for (MethodNode node : nd.methods) {
					if (node.name.equals("<clinit>")) {
						ArrayList<AbstractInsnNode> insns = new ArrayList<>();
						for (AbstractInsnNode instruction : method.instructions) {
							if (instruction instanceof InsnNode)
								if (instruction.getOpcode() == Opcodes.RETURN)
									break;
							insns.add(instruction);
						}
						for (AbstractInsnNode insn : insns) method.instructions.remove(insn);
						InsnList list = new InsnList();
						for (AbstractInsnNode insn : insns) list.add(insn);
						node.instructions.insert(list);
						foundCinit = true;
						break;
					}
				}
				if (!foundCinit) {
					nd.methods.add(method);
				}
			}
		}
	}
	
	@Override
	public void handle(AbstractInsnNode target, InsnList instructions) {
	}
	
	@Override
	public void reset() {
		try {
			InputStream stream = ForgeRegistryMutator.class.getClassLoader().getResourceAsStream("forge/FlufRegister.class");
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
	public boolean processesCall(String className, String functionName) {
		return className.equals("lostembers/fluf/api/FlufRegister") && functionName.equals("register");
	}
}
