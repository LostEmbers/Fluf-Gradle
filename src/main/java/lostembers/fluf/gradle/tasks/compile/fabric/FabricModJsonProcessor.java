package lostembers.fluf.gradle.tasks.compile.fabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class FabricModJsonProcessor {
	public static void process(File buildDir, Gson gson, JsonObject modJson, File fabricModJson) throws IOException {
		JsonObject object = new JsonObject();
		object.addProperty("schemaVersion", 1);
		object.addProperty("id", modJson.get("id").getAsString());
		object.addProperty("name", modJson.get("name").getAsString());
		// TODO: support variable versions
		object.addProperty("version", modJson.get("version").getAsString());
		object.addProperty("license", modJson.get("license").getAsString());
		if (modJson.has("dependencies")) {
			JsonObject dependencies = new JsonObject();
			JsonObject obj = modJson.getAsJsonObject("dependencies");
			for (String type : obj.keySet()) {
				if (!(type.equals("fabric") || type.equals("common"))) continue;
				JsonObject deps = obj.getAsJsonObject(type);
				for (String s : deps.keySet()) {
					String depVersion = deps.get(s).getAsString();
					if (depVersion.equals("x")) {
						depVersion = "*";
					} else if (depVersion.endsWith(".x")) {
						depVersion = "~" + depVersion.replace(".x", "");
					} else if (depVersion.contains("-")) {
						if (depVersion.startsWith("(")) {
							// TODO
							throw new RuntimeException("NYI");
						} else {
							// TODO
							throw new RuntimeException("NYI");
						}
					}
					dependencies.addProperty(s, depVersion);
				}
			}
			object.add("dependencies", dependencies);
		}
		
		if (modJson.has("side")) {
			if (modJson.get("side").getAsString().equals("any")) {
				object.addProperty("environment", "*");
			} else if (modJson.get("side").getAsString().equals("client") || modJson.get("side").getAsString().equals("server")) {
				throw new RuntimeException("NYI");
			} else {
				throw new RuntimeException("fluf_mod.json specifies an invalid side.");
			}
			// TODO
		} else {
			object.addProperty("environment", "*");
		}
		
		JsonObject entriesJson = modJson.getAsJsonObject("entries");
		JsonObject entryPoints = new JsonObject();
		for (String entryType : entriesJson.keySet()) {
			if (entryType.equals("fabric")) {
				JsonObject obj = entryPoints.getAsJsonObject(entryType);
				for (String s : obj.keySet()) {
					JsonArray entryList = obj.getAsJsonArray(s);
					JsonArray list = new JsonArray();
					for (JsonElement element : entryList) {
						String clazz = element.getAsString();
						list.add(clazz.replace("/", "."));
						// TODO: check interface..?
					}
					entryPoints.add(s, list);
				}
			} else {
				String name = entryType;
				if (name.equals("common")) name = "main";
				JsonArray entryList = entriesJson.getAsJsonArray(entryType);
				JsonArray list = new JsonArray();
				for (JsonElement element : entryList) {
					String clazz = element.getAsString();
					list.add(clazz.replace("/", "."));
					processFile(buildDir, clazz, entryType, true);
				}
				entryPoints.add(name, list);
			}
		}
		object.add("entrypoints", entryPoints);
		
		String txt = gson.toJson(object);
		FileOutputStream outputStream = new FileOutputStream(fabricModJson);
		outputStream.write(txt.getBytes());
		outputStream.close();
		outputStream.flush();
	}
	
	public static void processFile(File buildDir, String className, String entrypointType, boolean builtIn) throws IOException {
		if (!builtIn) return; // TODO
		String clazz;
		String method;
		switch (entrypointType) {
			case "common" -> {
				clazz = "net.fabricmc.api.ModInitializer";
				method = "onInitialize";
			}
			case "client" -> {
				clazz = "net.fabricmc.api.ClientModInitializer";
				method = "onInitializeClient";
			}
			case "server" -> throw new RuntimeException("NYI"); // TODO
			default -> throw new RuntimeException("Non-standard (likely loader specific) entry point type: " + entrypointType);
		}
		clazz = clazz.replace(".", "/");
		
		String dir = buildDir.toString().replace("\\", "/") + "/";
		if (dir.endsWith("//")) dir = dir.replace("//", "/");
		
		ClassNode node = new ClassNode();
		for (File file : new File(dir + "classes").listFiles()) {
			File fl = new File(file + "/main/" + className + ".class");
			if (fl.exists()) {
				InputStream stream = new FileInputStream(fl);
				byte[] bytes = stream.readAllBytes();
				stream.close();
				ClassReader reader = new ClassReader(bytes);
				reader.accept(node, ClassReader.EXPAND_FRAMES);
				
				node.interfaces.add(clazz);
				InsnList list = new InsnList();
				ArrayList<LocalVariableNode> nodes = new ArrayList<>();
				for (MethodNode methodNode : node.methods) {
					if (methodNode.name.equals("<init>")) {
						if (methodNode.desc.equals("()V")) {
							ArrayList<AbstractInsnNode> objInitNodes = new ArrayList<>();
							ArrayList<AbstractInsnNode> modInitNodes = new ArrayList<>();
							boolean hitSInit = false;
							for (AbstractInsnNode instruction : methodNode.instructions) {
								if (hitSInit) {
									modInitNodes.add(instruction);
									continue;
								} else objInitNodes.add(instruction);
								if (instruction instanceof MethodInsnNode) {
									if (((MethodInsnNode) instruction).name.equals("<init>")) {
										hitSInit = true;
									}
								}
							}
							for (AbstractInsnNode nd : objInitNodes) methodNode.instructions.remove(nd);
							for (AbstractInsnNode nd : modInitNodes) methodNode.instructions.remove(nd);
							InsnList initList = new InsnList();
							for (AbstractInsnNode nd : objInitNodes) initList.add(nd);
							for (AbstractInsnNode nd : modInitNodes) list.add(nd);
							initList.add(new InsnNode(Opcodes.RETURN));
							methodNode.instructions = initList;
							// move locals
							for (int i = 1; i < methodNode.localVariables.size(); i++) nodes.add(methodNode.localVariables.get(i));
							for (LocalVariableNode localVariableNode : nodes) methodNode.localVariables.remove(localVariableNode);
						}
					} else if (methodNode.name.equals(method) && methodNode.desc.equals("()V")) {
						System.err.println("Entrypoint: " + className + " contains a method which conflicts with the initializer's method: " + methodNode.name + methodNode.desc);
					}
				}
				MethodNode node1 = new MethodNode(
						Opcodes.ASM9,
						method, "()V",
						null, null
				);
				node1.instructions = list;
				node1.access = Modifier.PUBLIC;
				node1.invisibleAnnotations = new ArrayList<>();
				node1.invisibleAnnotations.add(new AnnotationNode("Ljava/lang/Override;"));
				node1.localVariables = nodes;
				node.methods.add(node1);
				
				ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
				node.accept(writer);
				FileOutputStream outputStream = new FileOutputStream(fl);
				outputStream.write(writer.toByteArray());
				outputStream.close();
				outputStream.flush();
			}
		}
	}
}
