package lostembers.fluf.gradle.tasks.compile;

import lostembers.fluf.gradle.settings.Loader;
import lostembers.fluf.gradle.tasks.generic.FlufTask;

import java.util.HashMap;

public class BuildTask extends FlufTask {
	public String loader = null;
	
	public void run() {
		Loader.target = loader;
		for (String s : tasks.keySet())
			tasks.get(s).setup(s);
	}
	
	private final HashMap<String, ASMTask> tasks = new HashMap<>();
	
	public void addTask(String intermediary, ASMTask remapTaskInter) {
		tasks.put(intermediary, remapTaskInter);
	}
}
