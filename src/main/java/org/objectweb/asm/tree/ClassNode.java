package org.objectweb.asm.tree;

import org.objectweb.asm.ClassVisitor;

import java.util.List;

public class ClassNode extends ClassVisitor {
	public int version;
	public int access;
	public String name;
	public String signature;
	public String superName;
	public List<String> interfaces;
	public String sourceFile;
	public String sourceDebug;
	public String outerClass;
	public String outerMethod;
	public String outerMethodDesc;
	public String nestHostClass;
	/*
	public ModuleNode module;
	public List<AnnotationNode> visibleAnnotations;
	public List<AnnotationNode> invisibleAnnotations;
	public List<TypeAnnotationNode> visibleTypeAnnotations;
	public List<TypeAnnotationNode> invisibleTypeAnnotations;
	public List<Attribute> attrs;
	public List<InnerClassNode> innerClasses;
	public List<String> nestMembers;
	public List<String> permittedSubclasses;
	public List<RecordComponentNode> recordComponents;
	public List<FieldNode> fields;
	public List<MethodNode> methods;
	 */
	
	public ClassNode() {
	}
	
	public void accept(final ClassVisitor classVisitor) {
	}
}
