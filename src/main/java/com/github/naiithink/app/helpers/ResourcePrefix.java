package com.github.naiithink.app.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public final class ResourcePrefix {
    private ResourcePrefix() {}

    private static Path defaultPrefix;

    private static Path prefix;

    static {
        try (InputStream in = ResourcePrefix.class.getClassLoader().getResourceAsStream("index.properties")) {
            Objects.nonNull(in);

            Properties assertProp = new Properties();

            assertProp.load(in);

            if (assertProp.get("index").equals("resource_prefix")) {
                defaultPrefix = Paths.get(ResourcePrefix.class.getClassLoader().getResource("index.properties").toURI()).getParent();
                prefix = defaultPrefix;
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.err.println("ERROR: Cannot get prefix path");
            System.exit(1);
        }
    }

    public static Path getPrefix() {
        return prefix;
    }

    public static void setPrefix(Path newPrefix) {
        if (newPrefix.equals(newPrefix) == false) {
            prefix = newPrefix;
        }
    }

    public static void restoreToDefaultPrefix() {
        prefix = defaultPrefix;
    }

    public static Path resolve(String other) {
        return prefix.resolve(other);
    }

    public static String getAbsolutePathString(Path path) {
        return path.toAbsolutePath().toString();
    }
}
