package lostembers.fluf.gradle.threading;

import java.util.ArrayList;
import java.util.function.Supplier;

public class ThreadObjectPool<T> {
	private final ArrayList<T> values = new ArrayList<>();
	private final ArrayList<Long> ids = new ArrayList<>();
	private final Supplier<T> defaultVal;
	
	private final Object lock = new Object();
	private final Object lock1 = new Object();
	
	public ThreadObjectPool(Supplier<T> defaultVal) {
		this.defaultVal = defaultVal;
	}
	
	public T get() {
		Thread td = Thread.currentThread();
		long id = td.getId();
		synchronized (lock1) {
			synchronized (lock) {
				if (!ids.contains(id)) {
					ids.add(id);
					values.add(defaultVal.get());
				}
			}
			return values.get(ids.indexOf(id));
		}
	}
	
	public ArrayList<T> getValues() {
//	public T[] getValues() {
		synchronized (lock1) {
//			T[] vals = null;
//			while (true) {
//				vals = (T[]) new Object[values.size()];
//				try {
//					for (int i = 0; i < vals.length; i++) {
//						vals[i] = values.get(i);
//					}
//					return vals;
//				} catch (Throwable ignored) {
//				}
//			}
			while (true) {
				try {
					return new ArrayList<>(values);
				} catch (Throwable ignored) {
				}
			}
		}
	}
}
