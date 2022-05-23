package lostembers.fluf.gradle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lostembers.fluf.gradle.settings.Loader;
import lostembers.fluf.gradle.settings.Settings;
import lostembers.fluf.gradle.tasks.WarningTask;
import lostembers.fluf.gradle.tasks.compile.BuildTask;
import lostembers.fluf.gradle.tasks.compile.ModJsonHandler;
import lostembers.fluf.gradle.tasks.compile.RemapTask;
import lostembers.fluf.gradle.tasks.devenv.RemapMCJarTask;
import lostembers.fluf.gradle.tasks.generic.ProxyTask;
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
import java.util.Collections;

public class FlufProject {
	public final Project project;
	public final RemapTask remapTaskInter;
	public final RemapTask remapTaskMoj;
	public final RemapTask remapTaskObsf;
	public final RemapTask remapTaskSrg;
	public final ProxyTask doRemap;
	public final ModJsonHandler handleJson;
	public final WarningTask warnNormalBuild;
	public final RemapMCJarTask remapMCTask;
	private final ArrayList<BuildTask> buildTasks = new ArrayList<>();
	public final Settings settings;
	public final Configuration cfg;
	
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
		{
			BuildTask task = createTask("buildForge", BuildTask.class);
			task.loader = "forge";
			task.setGroup("build");
			task.setFinalizedBy(Collections.singletonList(getTask("build")));
			buildTasks.add(task);
		}
		{
			BuildTask task = createTask("buildFabric", BuildTask.class);
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
		/* internal tasks */
		// TODO: why do I have 4 of these?
		// they're all identical
		// if I have only one of these, I don't need the proxy task
		{
			RemapTask task = createTask("remapIntermediary", RemapTask.class);
			(remapTaskInter = task).buildDir = project.getBuildDir();
			task.settings = settings;
		}
		{
			RemapTask task = createTask("remapMoj", RemapTask.class);
			(remapTaskMoj = task).buildDir = project.getBuildDir();
			task.settings = settings;
		}
		{
			RemapTask task = createTask("remapObsf", RemapTask.class);
			(remapTaskObsf = task).buildDir = project.getBuildDir();
			task.settings = settings;
		}
		{
			RemapTask task = createTask("remapSrg", RemapTask.class);
			(remapTaskSrg = task).buildDir = project.getBuildDir();
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
			case "tsrg2" -> doRemap.task = remapTaskSrg;
			case "intermediary" -> doRemap.task = remapTaskInter;
			case "mojmap" -> doRemap.task = remapTaskMoj;
			case "obsfucation" -> doRemap.task = remapTaskObsf;
		}
		for (BuildTask buildTask : buildTasks) buildTask.addTask(mappings, (RemapTask) doRemap.task);
	}
	
	private void setup(Settings settings) {
		String targetDir = "fluf_gradle/" + settings.getVersion() + "/" + settings.getMappings() + "/mapped-" + settings.getMappings();
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
					url1 = url1.substring(0, url1.length() - path.length());
					
					final String theURL = url1;
					try {
						new URL(theURL);
						boolean foundRepo = false;
						for (ArtifactRepository repository : project.getRepositories()) {
							if (repository instanceof MavenArtifactRepository) {
								URI url2 = ((MavenArtifactRepository) repository).getUrl();
								String repoUrl = url2.toString();
								if (repoUrl.equals(theURL)) {
									foundRepo = true;
									break;
								}
							}
						}
						if (!foundRepo) {
							URL url2 = new URL(theURL);
							URI uri = url2.toURI();
							ArtifactRepository repo = project.getRepositories().maven(o -> o.setUrl(uri));
							project.getRepositories().add(repo);
						}
						
						Dependency dep = project.getDependencies().create(name);
						cfg.getDependencies().add(dep);
					} catch (Throwable err) {
						err.printStackTrace();
					}
				}
			} catch (Throwable err) {
				err.printStackTrace();
				throw new RuntimeException("e");
			}
		}
		Dependency dep = project.getDependencies().create(project.files(fl.toString()));
		cfg.getDependencies().add(dep);
	}
	
	private <T extends Task> T createTask(String name, Class<T> clazz) {
		T task = project.getTasks().create(name, clazz);
		task.setGroup("fluf");
		return task;
	}
	
	private Task getTask(String name) {
		return project.getTasksByName(name, false).toArray(new Task[0])[0];
	}
}
