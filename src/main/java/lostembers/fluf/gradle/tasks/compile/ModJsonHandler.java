package lostembers.fluf.gradle.tasks.compile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lostembers.fluf.gradle.settings.Loader;
import lostembers.fluf.gradle.tasks.compile.fabric.FabricModJsonProcessor;
import lostembers.fluf.gradle.tasks.compile.forge.ForgeModJsonProcessor;
import lostembers.fluf.gradle.tasks.generic.FlufTask;
import lostembers.fluf.gradle.util.mappings.Mojmap;

import java.io.File;

public class ModJsonHandler extends FlufTask {
	public File buildDir = null;
	
	@Override
	public void run() {
		try {
			Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
			JsonObject modJson = gson.fromJson(Mojmap.readUrl(new File(buildDir + "/resources/main/fluf_mod.json").toURL().toString().replace("\\", "/")), JsonObject.class);
			System.out.println(modJson);
			// TODO: delete forge main when compiling to fabric
			File fabricModJson = new File(buildDir + "/resources/main/fabric.mod.json").getAbsoluteFile();
			File forgeModToml = new File(buildDir + "/resources/main/META-INF/mods.toml").getAbsoluteFile();
			if (fabricModJson.exists()) fabricModJson.delete();
			if (forgeModToml.exists()) {
				forgeModToml.delete();
				if (forgeModToml.getParentFile().listFiles().length == 0)
					forgeModToml.getParentFile().delete();
			}
			if (Loader.target.equals("fabric")) FabricModJsonProcessor.process(buildDir, gson, modJson, fabricModJson);
			else if (Loader.target.equals("forge"))
				ForgeModJsonProcessor.process(buildDir, gson, modJson, forgeModToml);
			File flufModJson = new File(buildDir + "/resources/main/fluf_mod.json").getAbsoluteFile();
			// TODO: somehow exclude the fluf_mod.json from the jar without deleting it
//			if (flufModJson.exists()) flufModJson.delete();
		} catch (Throwable err) {
			System.err.println("Failed to read fluf_mod.json.");
			System.err.println("Does your mod json exist and follow conventions?");
			System.err.println(err.getMessage());
		}
	}
}
