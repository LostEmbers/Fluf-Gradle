package lostembers.fluf.gradle.tasks.generic;

// so, extensions get initialized too late
// and thus I need this so I can make it so that the jar task depends on the correct remap task
public class ProxyTask extends FlufTask {
	public FlufTask task = null;
	
	public void run() {
		if (task != null) task.run();
	}
}
