package io.jhudson.gogetssm;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import picocli.CommandLine.IVersionProvider;

public class ManifestVersionProvider implements IVersionProvider {

    /**
     * Returns the version of the application from the manifest.
     *
     * @return the version of the application
     */
    @Override
    public String[] getVersion() throws Exception {
        return new String[] {"0.1.0-SNAPSHOT"};

        // TODO: This approach is likely to cause issues in native image.. also doesn't
        // work without addition of maven-archiver-plugin and manifest file.. look into a
        // better approach

        // return new String[]{ getVersionFromManifest() };
    }

    private String getVersionFromManifest() throws MalformedURLException, IOException {
        Class clazz = ManifestVersionProvider.class;
        String className = clazz.getSimpleName() + ".class";
        URL resource = clazz.getResource(className);
        if (resource == null) {
            throw new RuntimeException("Resource not found");
        }
        String classPath = resource.toString();
        if (!classPath.startsWith("jar")) {
            // Class not from JAR
            throw new RuntimeException("Class not from JAR");
        }
        String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        Manifest manifest = new Manifest(new URL(manifestPath).openStream());
        // If the line above leads to <null> manifest Attributes try instead from
        // JarInputStream:
        // Manifest manifest = new URL(manifestPath).openStream().getManifest();
        Attributes attr = manifest.getMainAttributes();
        return attr.getValue("Manifest-Version");
    }
}
