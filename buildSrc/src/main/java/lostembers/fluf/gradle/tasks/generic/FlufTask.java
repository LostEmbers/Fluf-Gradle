package lostembers.fluf.gradle.tasks.generic;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public abstract class FlufTask extends DefaultTask {
	@TaskAction
	public final void call() {
		run();
	}
	
	public abstract void run();
}
