package lostembers.fluf.gradle.tasks.compile.forge;

import lostembers.fluf.gradle.tasks.compile.common.APICallMutator;
import lostembers.fluf.gradle.tasks.compile.fabric.FabricRegistryMutator;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashMap;

public class ForgeAPIMutator {
	public static void process(ClassNode node) {
		MethodNode[] nodes = node.methods.toArray(new MethodNode[0]);
		for (MethodNode method : nodes) {
			HashMap<AbstractInsnNode, APICallMutator> mutatorHashMap = new HashMap<>();
			for (AbstractInsnNode instruction : method.instructions) {
				if (instruction instanceof MethodInsnNode) {
					if (((MethodInsnNode) instruction).owner.startsWith("lostembers/fluf/api")) {
						APICallMutator mutator = lookupMutator(((MethodInsnNode) instruction).owner, ((MethodInsnNode) instruction).name);
						mutatorHashMap.put(instruction, mutator);
					}
				}
			}
			for (AbstractInsnNode abstractInsnNode : mutatorHashMap.keySet()) {
				mutatorHashMap.get(abstractInsnNode).handle(abstractInsnNode, method.instructions, node);
				mutatorHashMap.get(abstractInsnNode).reset();
			}
		}
	}
	
	private static APICallMutator[] defaultMutators = new APICallMutator[]{
		new ForgeRegistryMutator(),
	};
	
	private static APICallMutator lookupMutator(String className, String functionName) {
		for (APICallMutator mutator : defaultMutators) {
			if (mutator.processesCall(className, functionName))
				return mutator;
		}
		return null;
	}
}
