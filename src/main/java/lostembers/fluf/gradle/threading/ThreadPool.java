package lostembers.fluf.gradle.threading;

public class ThreadPool {
	private final ReusableThread[] threads;
	private int highestNumberUsed = 0;
	
	public ThreadPool(int count) {
		threads = new ReusableThread[count];
		for (int i = 0; i < threads.length; i++)
			threads[i] = new ReusableThread(null);
	}
	
	private final Object lock = new Object();
	
	int countStart = 0;
	
	public ReusableThread startNext(Runnable r) {
		synchronized (lock) {
			while (true) {
				for (int i = 0; i < threads.length; i++) {
					if (!threads[i].isRunning()) {
						threads[i].setAction(r);
						threads[i].start();
						highestNumberUsed = Math.max(highestNumberUsed, i + 1);
						return threads[i];
					}
				}
				if (threads.length == 0) r.run();
				try {
					// helps with CPU usage, at least on older JREs/JDKs
					Thread.sleep(1);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
	
	public void kill() {
		System.out.println("Used: " + highestNumberUsed + "/" + threads.length + " threads");
		for (ReusableThread thread : threads)
			thread.kill();
	}
	
	public void completeAsync(Runnable r) {
		r.run();
		System.out.println("Awaiting threads to finish");
		await();
	}
	
	public void await() {
		for (int i = 0; i < threads.length; i++) {
			if (threads[i].isRunning())
				System.out.println("Waiting on thread " + i);
			threads[i].await();
		}
	}
}
