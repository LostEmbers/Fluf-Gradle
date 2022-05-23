package org.objectweb.asm.commons;

public abstract class Remapper {
	public String mapDesc(final String descriptor) {
		return null;
	}

//	private Type mapType(final Type type) {
//		switch (type.getSort()) {
//			case Type.ARRAY:
//				StringBuilder remappedDescriptor = new StringBuilder();
//				for (int i = 0; i < type.getDimensions(); ++i) {
//					remappedDescriptor.append('[');
//				}
//				remappedDescriptor.append(mapType(type.getElementType()).getDescriptor());
//				return Type.getType(remappedDescriptor.toString());
//			case Type.OBJECT:
//				String remappedInternalName = map(type.getInternalName());
//				return remappedInternalName != null ? Type.getObjectType(remappedInternalName) : type;
//			case Type.METHOD:
//				return Type.getMethodType(mapMethodDesc(type.getDescriptor()));
//			default:
//				return type;
//		}
//	}
	
	public String mapType(final String internalName) {
		return null;
	}
	
//	public String[] mapTypes(final String[] internalNames) {
//		String[] remappedInternalNames = null;
//		for (int i = 0; i < internalNames.length; ++i) {
//			String internalName = internalNames[i];
//			String remappedInternalName = mapType(internalName);
//			if (remappedInternalName != null) {
//				if (remappedInternalNames == null) {
//					remappedInternalNames = internalNames.clone();
//				}
//				remappedInternalNames[i] = remappedInternalName;
//			}
//		}
//		return remappedInternalNames != null ? remappedInternalNames : internalNames;
//	}

//	public String mapMethodDesc(final String methodDescriptor) {
//		if ("()V".equals(methodDescriptor)) {
//			return methodDescriptor;
//		}
//
//		StringBuilder stringBuilder = new StringBuilder("(");
//		for (Type argumentType : Type.getArgumentTypes(methodDescriptor)) {
//			stringBuilder.append(mapType(argumentType).getDescriptor());
//		}
//		Type returnType = Type.getReturnType(methodDescriptor);
//		if (returnType == Type.VOID_TYPE) {
//			stringBuilder.append(")V");
//		} else {
//			stringBuilder.append(')').append(mapType(returnType).getDescriptor());
//		}
//		return stringBuilder.toString();
//	}

//	public Object mapValue(final Object value) {
//		if (value instanceof Type) {
//			return mapType((Type) value);
//		}
//		if (value instanceof Handle) {
//			Handle handle = (Handle) value;
//			boolean isFieldHandle = handle.getTag() <= Opcodes.H_PUTSTATIC;
//
//			return new Handle(
//					handle.getTag(),
//					mapType(handle.getOwner()),
//					isFieldHandle
//							? mapFieldName(handle.getOwner(), handle.getName(), handle.getDesc())
//							: mapMethodName(handle.getOwner(), handle.getName(), handle.getDesc()),
//					isFieldHandle ? mapDesc(handle.getDesc()) : mapMethodDesc(handle.getDesc()),
//					handle.isInterface());
//		}
//		if (value instanceof ConstantDynamic) {
//			ConstantDynamic constantDynamic = (ConstantDynamic) value;
//			int bootstrapMethodArgumentCount = constantDynamic.getBootstrapMethodArgumentCount();
//			Object[] remappedBootstrapMethodArguments = new Object[bootstrapMethodArgumentCount];
//			for (int i = 0; i < bootstrapMethodArgumentCount; ++i) {
//				remappedBootstrapMethodArguments[i] =
//						mapValue(constantDynamic.getBootstrapMethodArgument(i));
//			}
//			String descriptor = constantDynamic.getDescriptor();
//			return new ConstantDynamic(
//					mapInvokeDynamicMethodName(constantDynamic.getName(), descriptor),
//					mapDesc(descriptor),
//					(Handle) mapValue(constantDynamic.getBootstrapMethod()),
//					remappedBootstrapMethodArguments);
//		}
//		return value;
//	}

//	public String mapSignature(final String signature, final boolean typeSignature) {
//		if (signature == null) {
//			return null;
//		}
//		SignatureReader signatureReader = new SignatureReader(signature);
//		SignatureWriter signatureWriter = new SignatureWriter();
//		SignatureVisitor signatureRemapper = createSignatureRemapper(signatureWriter);
//		if (typeSignature) {
//			signatureReader.acceptType(signatureRemapper);
//		} else {
//			signatureReader.accept(signatureRemapper);
//		}
//		return signatureWriter.toString();
//	}
//
//	@Deprecated
//	protected SignatureVisitor createRemappingSignatureAdapter(
//			final SignatureVisitor signatureVisitor) {
//		return createSignatureRemapper(signatureVisitor);
//	}
//
//	protected SignatureVisitor createSignatureRemapper(final SignatureVisitor signatureVisitor) {
//		return new SignatureRemapper(signatureVisitor, this);
//	}
//
//	public String mapAnnotationAttributeName(final String descriptor, final String name) {
//		return name;
//	}
//
//	public String mapInnerClassName(
//			final String name, final String ownerName, final String innerName) {
//		final String remappedInnerName = this.mapType(name);
//		if (remappedInnerName.contains("$")) {
//			int index = remappedInnerName.lastIndexOf('$') + 1;
//			while (index < remappedInnerName.length()
//					&& Character.isDigit(remappedInnerName.charAt(index))) {
//				index++;
//			}
//			return remappedInnerName.substring(index);
//		} else {
//			return innerName;
//		}
//	}
	
	public String mapMethodName(final String owner, final String name, final String descriptor) {
		return name;
	}
	
//	public String mapInvokeDynamicMethodName(final String name, final String descriptor) {
//		return name;
//	}

//	public String mapRecordComponentName(
//			final String owner, final String name, final String descriptor) {
//		return name;
//	}
	
	public String mapFieldName(final String owner, final String name, final String descriptor) {
		return name;
	}
	
//	public String mapPackageName(final String name) {
//		return name;
//	}
//
//	public String mapModuleName(final String name) {
//		return name;
//	}
	
	public String map(final String internalName) {
		return internalName;
	}
	
	public String mapMethodDesc(final String methodDescriptor) {
		return methodDescriptor;
	}
}
