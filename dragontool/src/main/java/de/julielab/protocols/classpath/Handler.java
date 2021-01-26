package de.julielab.protocols.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A {@link URLStreamHandler} that handles resources on the classpath. Taken
 * from
 * https://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
 */
public class Handler extends URLStreamHandler {
	/** The classloader to find resources from. */
	private final ClassLoader classLoader;

	public Handler() {
		this.classLoader = getClass().getClassLoader();
	}

	public Handler(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		URL resource = classLoader.getResource(u.getPath());
		// Actually, I don't really know exactly why this line above sometimes
		// returns null when the line below gets the correct resource. I think
		// there might be different class loaders involved. We just try both...
		if (resource == null)
			resource = getClass().getResource(u.getPath());
		return resource.openConnection();
	}
}