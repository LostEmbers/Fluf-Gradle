package lostembers.fluf.gradle.util;

import java.net.URL;

public class DependencySpec {
	public final URL url;
	public final URL maven;
	public final String name;
	
	public DependencySpec(URL url, URL maven, String name) {
		this.url = url;
		this.maven = maven;
		this.name = name;
	}
	
	public DependencySpec(String url, String maven, String name) {
		try {
			this.url = new URL(url);
			this.maven = new URL(maven);
			this.name = name;
		} catch (Throwable ignored) {
			throw new RuntimeException("e");
		}
	}
}
