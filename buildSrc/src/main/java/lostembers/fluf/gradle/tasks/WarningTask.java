package lostembers.fluf.gradle.tasks;

import lostembers.fluf.gradle.tasks.generic.FlufTask;

public class WarningTask extends FlufTask {
	public Runnable action;
	
	public void run() {
		action.run();
	}
}
