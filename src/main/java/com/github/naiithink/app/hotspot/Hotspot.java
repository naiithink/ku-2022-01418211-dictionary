package com.github.naiithink.app.hotspot;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.helpers.ResourcePrefix;
import com.github.naiithink.app.models.WordClass;

public final class Hotspot {
    private Hotspot() {}

    public static final String DEFAULT_WORD_ENTRY;

    public static boolean isAppInitialized;

    static {
        DEFAULT_WORD_ENTRY = "naiithink";
        isAppInitialized = false;
    }

    public static class DefaultWordInfo {

        public static String WORD_LITERAL;

        public static WordClass WORD_CLASS;

        public static String WORD_DEFINITION;

        public static List<String> EXAMPLE_IN_SENTENCES;

        static {
            WORD_LITERAL = "naiithink";
            WORD_CLASS = WordClass.NOUN;
            WORD_DEFINITION = "An instance of type Homo sapiens who wrote this app.\nThis type of creature is frequently called 'human'.";
            EXAMPLE_IN_SENTENCES = new CopyOnWriteArrayList<>();

            EXAMPLE_IN_SENTENCES.add("Look! naiithink is working on his project.");
            EXAMPLE_IN_SENTENCES.add("I've been to the moon with naiithink long time ago.");
            EXAMPLE_IN_SENTENCES.add("naiithink is a user on GitHub, eiei.");
        }
    }

    public static class Universal {

        public enum IECBinary {

            KIBI        (2 << 9),
            MEBI        (KIBI.value * KIBI.value),
            GIBI        (KIBI.value * MEBI.value),
            TEBI        (KIBI.value * GIBI.value);

            private long value;

            private IECBinary(long value) {
                this.value = value;
            }

            public long value() {
                return value;
            }
        }

        public enum CSV {

            CSV_DELIMITER                       (","),
            CSV_WITH_QOUTE_ESCAPE               ("(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))"),
            END_OF_SENTENCE_ESCAPE              ("\\\\\\\\033\\\\\\[1E"),
            END_OF_SENTENCE_READ                ("\\\\033\\[1E"),
            END_OF_SENTENCE_WRITE               ("\\033[1E"),
            END_OF_SENTENCE_WRITE_REPLACE       ("\\\\033[1E"),
            END_OF_SENTENCE_SEARCH              ("(?:\\\\033\\[1E)");

            private String token;

            private CSV(String token) {
                this.token = token;
            }

            public String getToken() {
                return token;
            }
        }
    }

    public static class UI {

        public static final String APP_TITLE;

        public static final double STAGE_WIDTH;

        public static final double STAGE_HEIGHT;

        static {
            APP_TITLE = "naiithink's Dictionary Free";
            STAGE_WIDTH = 800.0;
            STAGE_HEIGHT = 600.0;
        }

        public enum Color {

            RED         ("ee6060"),
            GREEN       ("16e454"),
            BLUE        ("5788fa"),
            YELLOW      ("fae13e");

            private String value;

            private Color(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }
        }

        public enum AnimationDuration {

            STATUS_BANNER_DURATION      (5.0);

            private double value;

            private AnimationDuration(double value) {
                this.value = value;
            }

            public double getValue() {
                return value;
            }
        }
    }

    public static class Resource {

        public static class DataSourceReference {

            public static final String PREFIX;

            public static final String DATA_SOURCE_DIRECTORY;

            public static final String DATA_SOURCE_FILE;

            static {
                PREFIX = System.getProperty("user.dir");
                DATA_SOURCE_DIRECTORY = PREFIX + System.getProperty("file.separator") + "data";
                DATA_SOURCE_FILE = DATA_SOURCE_DIRECTORY + System.getProperty("file.separator") + "dictionary.csv";
            }
        }

        public static class ResourceIndex {

            private static Logger logger;

            private static Properties resourceIndex;

            static {
                logger = Logger.getLogger(ResourceIndex.class.getName());
                resourceIndex = new Properties();
            }

            public static void readResourceIndex() {
                try (InputStream in = Files.newInputStream(ResourcePrefix.getPrefix().resolve("index.properties"), StandardOpenOption.READ)) {

                    Objects.nonNull(in);

                    resourceIndex.load(in);
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Cannot load resource index");
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            public static String getProperty(String key) {
                return resourceIndex.getProperty(key);
            }
        }
    }

    public enum EventType {

        RECORD_CHANGED
    }
}
