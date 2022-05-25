package lostembers.fluf.gradle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lostembers.fluf.gradle.settings.Loader;
import lostembers.fluf.gradle.settings.Settings;
import lostembers.fluf.gradle.tasks.WarningTask;
import lostembers.fluf.gradle.tasks.compile.ASMTask;
import lostembers.fluf.gradle.tasks.compile.BuildTask;
import lostembers.fluf.gradle.tasks.compile.ModJsonHandler;
import lostembers.fluf.gradle.tasks.devenv.AMTask;
import lostembers.fluf.gradle.tasks.devenv.RemapMCJarTask;
import lostembers.fluf.gradle.tasks.generic.ProxyTask;
import lostembers.fluf.gradle.tasks.run.RunFabric;
import lostembers.fluf.gradle.util.DependencySpec;
import lostembers.fluf.gradle.util.mappings.Mojmap;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class FlufProject {
	public final Project project;
	public final ASMTask remapTask;
	public final ProxyTask doRemap;
	public final ModJsonHandler handleJson;
	public final WarningTask warnNormalBuild;
	public final RemapMCJarTask remapMCTask;
	public final AMTask amTask;
	private final BuildTask buildFabric;
	private final ArrayList<BuildTask> buildTasks = new ArrayList<>();
	public final Settings settings;
	public final Configuration cfg;
	public DependencySpec[] dependencies;
	
	public FlufProject(Project project) {
		this.project = project;

//		SoftwareComponent component = project.getComponents().getByName("fluf");
//		System.out.println(component);
		settings = project.getExtensions().create("fluf", Settings.class);
		settings.project = this;
		cfg = project.getConfigurations().getByName("implementation");

//		Settings extension = project.getExtensions().create("fluf", Settings.class, project);
//		System.out.println(extension.getGreeter().get());
		
		/* compilation tasks */
		ProxyTask dummyTask = createTask("dummy", ProxyTask.class);
		dummyTask.dependsOn(getTask("classes"), getTask("build"));
		{
			BuildTask task = createTask("buildForge", BuildTask.class);
			task.loader = "forge";
			task.setGroup("build");
			task.setFinalizedBy(Collections.singletonList(getTask("build")));
			buildTasks.add(task);
		}
		{
			BuildTask task = buildFabric = createTask("buildFabric", BuildTask.class);
			task.loader = "fabric";
			task.setGroup("build");
			task.setFinalizedBy(Collections.singletonList(getTask("build")));
			buildTasks.add(task);
		}
		{
			BuildTask task = createTask("buildVanilla", BuildTask.class);
			task.loader = "vanilla";
			task.setGroup("build");
			task.setFinalizedBy(Collections.singletonList(getTask("build")));
			buildTasks.add(task);
		}
		/* setup tasks */
		{
			RemapMCJarTask task = createTask("remapMc", RemapMCJarTask.class);
			(remapMCTask = task).buildDir = project.getBuildDir();
			remapMCTask.project = this;
		}
		{
			AMTask task = createTask("amHandler", AMTask.class);
			(amTask = task).project = this;
			amTask.project = this;
			remapMCTask.finalizedBy(amTask);
		}
		/* runs */
		{
			RunFabric fabric = createTask("runFabric", RunFabric.class);
			fabric.project = this;
			fabric.setGroup("fluf_runs");
			fabric.dependsOn(buildFabric);
		}
		/* internal tasks */
		{
			ASMTask task = createTask("remapMod", ASMTask.class);
			(remapTask = task).buildDir = project.getBuildDir();
			task.settings = settings;
		}
		{
			doRemap = createTask("doRemap", ProxyTask.class);
			getTask("jar").dependsOn(doRemap);
		}
		{
			handleJson = createTask("handleModJson", ModJsonHandler.class);
			handleJson.buildDir = project.getBuildDir();
			getTask("jar").dependsOn(handleJson);
		}
		{
			WarningTask task = createTask("warnNormalBuild", WarningTask.class);
			(warnNormalBuild = task).action = () -> {
				if (Loader.target == null) {
					System.err.println("[Warning]: Build/Jar task was run normally");
					System.err.println("Please use any of the following:");
					// TODO: automate this
					System.err.println("- buildForge");
					System.err.println("- buildFabric");
					System.err.println("- buildVanilla");
				}
			};
			getTask("jar").finalizedBy(task);
		}
		
		project.afterEvaluate(p -> {
			setup(settings);
		});
	}
	
	public void setMappings(String mappings) {
		switch (mappings) {
			case "tsrg2", "intermediary", "obsfucation", "mojmap" -> doRemap.task = remapTask;
		}
		for (BuildTask buildTask : buildTasks) buildTask.addTask(mappings, (ASMTask) doRemap.task);
	}
	
	private void setup(Settings settings) {
		if (settings == null) throw new RuntimeException("A \"fluf\" block must be specified");
		if (settings.mappings == null && settings.version == null)
			throw new RuntimeException("A \"fluf\" block must be specified");
		if (settings.mappings == null)
			throw new RuntimeException("Your \"fluf\" block must specify the name of the mappings you want to use");
		if (settings.version == null)
			throw new RuntimeException("Your \"fluf\" block must specify the name of the game version you want to use");
		
		String targetDir = getGameJarPath();
		File fl = new File(targetDir + ".jar").getAbsoluteFile();
		if (!fl.getAbsoluteFile().exists()) remapMCTask.run();
		{
			File fl1 = new File("fluf_gradle/cache/" + settings.version + ".json").getAbsoluteFile();
			String url;
			try {
				if (!fl1.exists()) {
					String txt = Mojmap.readUrl(Mojmap.url);
					JsonObject object = Mojmap.gson.fromJson(txt, JsonObject.class);
					JsonArray array = object.getAsJsonArray("versions");
					
					url = null;
					for (JsonElement element : array) {
						if (element.getAsJsonObject().getAsJsonPrimitive("id").getAsString().equals(settings.version)) {
							url = element.getAsJsonObject().getAsJsonPrimitive("url").getAsString();
							break;
						}
					}
					if (url == null) throw new RuntimeException("Could not find an entry for " + settings.version);
					txt = Mojmap.readUrl(url);
					
					{
						FileOutputStream outputStream = new FileOutputStream(fl1);
						outputStream.write(txt.getBytes());
						outputStream.close();
						outputStream.flush();
					}
				} else {
					url = fl1.toURL().toString();
				}
				JsonObject obj = Mojmap.gson.fromJson(Mojmap.readUrl(url), JsonObject.class);
				ArrayList<DependencySpec> specs = new ArrayList<>();
				handleGameJson(obj, specs);
				for (DependencySpec spec : specs) {
					boolean foundRepo = false;
					for (ArtifactRepository repository : project.getRepositories()) {
						if (repository instanceof MavenArtifactRepository) {
							URI url2 = ((MavenArtifactRepository) repository).getUrl();
							if (url2.toString().equals(spec.maven.toString())) {
								foundRepo = true;
								break;
							}
						}
					}
					if (!foundRepo) {
						URL url2 = spec.maven;
						URI uri = url2.toURI();
						ArtifactRepository repo = project.getRepositories().maven(o -> o.setUrl(uri));
						project.getRepositories().add(repo);
					}
					
					Dependency dep = project.getDependencies().create(spec.name);
					cfg.getDependencies().add(dep);
				}
				dependencies = specs.toArray(new DependencySpec[0]);
			} catch (Throwable err) {
				err.printStackTrace();
				throw new RuntimeException("e");
			}
		}
		File amJar = new File(project.getBuildDir() + "/fluf_gradle/post_am.jar");
		if (amJar.exists()) fl = amJar;
		Dependency dep = project.getDependencies().create(project.files(fl.toString()));
		cfg.getDependencies().add(dep);
	}
	
	public void handleGameJson(JsonObject obj, ArrayList<DependencySpec> urls) {
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("windows")) os = "windows";
		// TODO: check..?
		// from: https://stackoverflow.com/a/31547504
		if ((os.contains("mac")) || (os.contains("darwin"))) os = "osx";
		// TODO: optimize this
		loopElements:
		for (JsonElement element : obj.getAsJsonArray("libraries")) {
			String name = element.getAsJsonObject().get("name").getAsString();
			if (((JsonObject) element).has("rules")) {
				JsonArray rules = element.getAsJsonObject().getAsJsonArray("rules");
				for (JsonElement rule : rules) {
					if (rule.getAsJsonObject().get("action").getAsString().equals("allow")) {
						if (rule.getAsJsonObject().has("os")) {
							String str = rule.getAsJsonObject().get("os").getAsJsonObject().get("name").getAsJsonPrimitive().getAsString();
							if (!str.equals(os)) continue loopElements;
						}
					} else if (rule.getAsJsonObject().get("action").getAsString().equals("disallow")) {
						if (rule.getAsJsonObject().has("os")) {
							String str = rule.getAsJsonObject().get("os").getAsJsonObject().get("name").getAsJsonPrimitive().getAsString();
							if (str.equals(os)) continue loopElements;
						} else continue loopElements;
					}
					// TODO?
				}
			}
			element = element.getAsJsonObject().get("downloads").getAsJsonObject().get("artifact");
			String url1 = element.getAsJsonObject().get("url").getAsJsonPrimitive().getAsString();
			String path = element.getAsJsonObject().get("path").getAsJsonPrimitive().getAsString();
			String fullUrl = url1;
			url1 = url1.substring(0, url1.length() - path.length());
			
			final String theURL = url1;
			try {
				new URL(theURL);
				urls.add(new DependencySpec(fullUrl, theURL, name));
			} catch (Throwable err) {
				err.printStackTrace();
			}
		}
	}
	
	private <T extends Task> T createTask(String name, Class<T> clazz) {
		T task = project.getTasks().create(name, clazz);
		task.setGroup("fluf");
		return task;
	}
	
	private Task getTask(String name) {
		return project.getTasksByName(name, false).toArray(new Task[0])[0];
	}
	
	public String getGameJarPath() {
		return "fluf_gradle/" + settings.getVersion() + "/" + settings.getMappings() + "/" + settings.version + "-mapped-" + settings.getMappings();
	}
}
