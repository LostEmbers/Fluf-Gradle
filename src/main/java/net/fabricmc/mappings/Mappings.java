package net.fabricmc.mappings;

import java.util.Collection;

public interface Mappings {
//	Collection<String> getNamespaces();
	Collection<ClassEntry> getClassEntries();
	Collection<FieldEntry> getFieldEntries();
	Collection<MethodEntry> getMethodEntries();
//
//	default Collection<MethodParameterEntry> getMethodParameterEntries() {
//		return new ArrayList();// 34
//	}
//
//	default Collection<LocalVariableEntry> getLocalVariableEntries() {
//		return new ArrayList();// 35
//	}
//
//	default Comments getComments() {
//		return new CommentsImpl(new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList(), new ArrayList());// 37
//	}
}
