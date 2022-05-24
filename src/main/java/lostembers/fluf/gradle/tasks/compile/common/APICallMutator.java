package lostembers.fluf.gradle.tasks.compile.common;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;

public abstract class APICallMutator {
	@Deprecated
	public void handle(AbstractInsnNode target, InsnList instructions, ClassNode nd) {
		handle(target, instructions);
	}
	
	public abstract void handle(AbstractInsnNode target, InsnList instructions);
	public abstract boolean processesCall(String className, String functionName);
}
