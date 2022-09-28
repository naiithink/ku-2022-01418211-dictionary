/**
 * @file StageManager.java
 * @version 0.1.0-dev
 * 
 * @attention This version of the StageManager was written to fully support JavaFX version 17.0.2.
 * 
 * @todo Apply Optional fields
 * @todo Scene tree for navigation
 * @todo Multi-stage management
 */

package com.github.naiithink.app.services;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Manager for the primary application Stage
 * 
 * @attention This version of the StageManager was written to fully support JavaFX version 17.0.2.
 * @note This version of the StageManager only supports single-stage application
 * 
 * This class is following the singleton pattern since there must only be one instance
 * of the StageManager that takes control over the primary @link javafx.stage.Stage @unlink
 * provided by the platform.
 */
public final class StageManager {

    /**
     * If required, controller class to be used must be written in singleton pattern
     * 
     * Name of the method for getting controller type instance
     */
    private static final String GET_INSTANCE_METHOD_NAME;

    /**
     * Delimiter used to delimit the .fxml resource and its controller class name field in a record
     * read from FXML index property file.
     */
    private static final String INDEX_PROPERTY_TOKEN;

    /**
     * Default width of the Stage
     */
    private static final double DEFAULT_STAGE_WIDTH;

    /**
     * Default height of the Stage
     */
    private static final double DEFAULT_STAGE_HEIGHT;

    private static StageManager instance;

    /**
     * Configuration file for this StageManager
     */
    private static Optional<Properties> stageManagerConfigProperties;

    /**
     * Font resource table
     */
    private static Map<String, Font> fontTable;

    /**
     * Logger instance for this StageManager
     */
    private Logger logger;

    /**
     * Path to the FXML index property file
     */
    private Path sceneResourceIndexPath;

    /**
     * Prefix Path of FXML files to be loaded
     */
    private Path sceneResourcePrefixPath;

    /**
     * Table storing all of the Scene entries each maps to its corresponding SceneMap
     * 
     * @see SceneMap
     */
    private Map<String, SceneMap> sceneTable;

    /**
     * Properties type storing all the records loaded from the FXML index property file
     */
    private Properties resourceIndexProperties;

    /**
     * The instance of the main entry point of the JavaFX application
     */
    private Object mainApp;

    /**
     * The primary Stage of the application
     */
    private Stage primaryStage;

    /**
     * Title of the primary Stage
     */
    private String primaryStageTitle;

    /**
     * Width of the primary Stage
     * 
     * @see DEFAULT_STAGE_WIDTH
     */
    private double primaryStageWidth;

    /**
     * Height of the primary Stage
     * 
     * @see DEFAULT_STAGE_HEIGHT
     */
    private double primaryStageHeight;

    /**
     * Scene entry of the primary Scene
     */
    private String homeSceneEntry;

    /**
     * The annotation used to annotate types, states, or behaviours required by this StageManager
     */
    @Retention (RetentionPolicy.RUNTIME)
    @Target ({ ElementType.TYPE, ElementType.METHOD })
    public @interface NaiiThinkStageManager {}

    /**
     * The record type for storing a pair of Scene and, if required, its corresponding controller Class
     */
    private record SceneMap(Scene scene,
                            Class<?> controllerClass) {}

    /**
     * Thrown to indicate that the code has attempted to manipulate a Scene that is not in the Scene table
     * 
     * @see sceneTable
     * @see addScene
     * @see removeScene
     */
    public final class SceneNotFoundException
            extends Exception {

        public SceneNotFoundException() {}

        public SceneNotFoundException(String sceneEntry) {
            super(sceneEntry);
        }

        public SceneNotFoundException(Throwable cause) {
            super(cause);
        }

        public SceneNotFoundException(String sceneEntry,
                                      Throwable cause) {
            super(sceneEntry, cause);
        }

        public SceneNotFoundException(String message,
                                      Throwable cause,
                                      boolean enableSuppression,
                                      boolean writableStackTrace) {

            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    static {
        GET_INSTANCE_METHOD_NAME = "getInstance";
        INDEX_PROPERTY_TOKEN = "\\=>";
        DEFAULT_STAGE_WIDTH = 800.0;
        DEFAULT_STAGE_HEIGHT = 600.0;
    }

    /**
     * Contructor is kept private, singleton
     */
    private StageManager() {
        logger = Logger.getLogger(getClass().getName());

        fontTable = new ConcurrentHashMap<>();
        sceneTable = new ConcurrentHashMap<>();
        resourceIndexProperties = new Properties();

        primaryStage = null;
    }

    /**
     * Gets the instance of StageManager
     */
    public static StageManager getStageManager() {
        if (instance == null) {
            synchronized (StageManager.class) {
                if (instance == null) {
                    instance = new StageManager();
                }
            }
        }

        return instance;
    }

    /**
     * Dispatch controls over Stages of the application to this StageManager
     * 
     * @param       sceneResourceIndexPath      Path to the FXML index property file
     * @param       sceneResourcePrefixPath     Prefix Path of FXML files to be loaded
     * @param       mainApp                     The instance of the main entry point of the JavaFX application
     * @param       primaryStage                The primary Stage of the application
     * @param       primaryStageTitle           Title of the primary Stage
     * @param       primaryStageWidth           Width of the primary Stage
     * @param       primaryStageHeight          Height of the primary Stage
     */
    public void dispatch(Path sceneResourceIndexPath,
                         Path sceneResourcePrefixPath,
                         Object mainApp,
                         Stage primaryStage,
                         String primaryStageTitle,
                         double primaryStageWidth,
                         double primaryStageHeight) {

        Objects.nonNull(sceneResourceIndexPath);
        Objects.nonNull(mainApp);
        Objects.nonNull(primaryStage);

        if (Application.class.isAssignableFrom(mainApp.getClass()) == false) {
            logger.log(Level.SEVERE, "Unable to take control over main JavaFX entry, invalid argument type for parameter 'mainApp'");

            System.exit(1);
        }

        this.sceneResourceIndexPath = sceneResourceIndexPath;
        this.sceneResourcePrefixPath = sceneResourcePrefixPath;
        this.mainApp = mainApp;
        this.primaryStage = primaryStage;
        this.primaryStageTitle = primaryStageTitle;
        this.primaryStageWidth = primaryStageWidth;
        this.primaryStageHeight = primaryStageHeight;

        // try {
        //     checkIfMainAppObject(mainApp);
        // } catch (NotMainAppObjectException e) {
        //     logger.log(Level.SEVERE, "Trying to bind with non-main JavaFX application entry");
        //     e.printStackTrace();
        // }

        try (InputStream in = Files.newInputStream(sceneResourceIndexPath)) {

            Objects.nonNull(in);

            resourceIndexProperties.load(in);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot get to FXML index file");
        }

        String sceneEntryResourceName;
        String controllerClassName;
        Class<?> controllerClass;
        Method getInstanceMethod;
        Object controllerInstance;

        FXMLLoader loader;
        Scene scene;

        try {
            Set<String> sceneEntries = resourceIndexProperties.stringPropertyNames();

            for (String sceneEntry : sceneEntries) {

                loader = new FXMLLoader();

                String[] sceneIndexPropertyFields = resourceIndexProperties.getProperty(sceneEntry).split(INDEX_PROPERTY_TOKEN);

                if (sceneIndexPropertyFields.length == 0
                    || sceneIndexPropertyFields.length > 2) {

                    logger.log(Level.SEVERE, "FXML index property '" + sceneEntry + "' has no property value");
                    continue;
                } else if (sceneIndexPropertyFields.length > 2) {
                    logger.log(Level.SEVERE, "Too many property field for '" + sceneEntry + "'");
                    continue;
                }

                sceneEntryResourceName = new String(sceneIndexPropertyFields[0]);

                if (sceneIndexPropertyFields.length == 1) {
                    logger.log(Level.WARNING, "FXML index '" + sceneEntry + "' does not have declared controller");

                    loader.setLocation(sceneResourcePrefixPath.resolve(sceneEntryResourceName).toUri().toURL());

                    sceneTable.put(sceneEntry, new SceneMap(new Scene(loader.load(), primaryStageWidth, primaryStageHeight), null));
                    logger.log(Level.INFO, "Scene added: " + sceneEntry + ": **/" + sceneEntryResourceName);

                    continue;
                }

                if (sceneIndexPropertyFields.length == 2) {
                    controllerClassName = new String(sceneIndexPropertyFields[1]);
                    controllerClass = Class.forName(controllerClassName);

                    if (controllerClass.isAnnotationPresent(StageManager.NaiiThinkStageManager.class) == false) {
                        /**
                         * @danger DO NOT pass any message read from file to any string formatter
                         */
                        String cannotValidateDeclaredControllerMessage = "Singleton controller: cannot check if the controller class of file '" + sceneEntryResourceName + "' with declared controller name '" + controllerClassName + "' is valid.\n"
                                                                         + "FXML controller MUST be annotated with '@" + StageManager.NaiiThinkStageManager.class.getCanonicalName() + "' interface and define a static method '" + GET_INSTANCE_METHOD_NAME + "' with an annotation '@" + StageManager.NaiiThinkStageManager.class.getCanonicalName() + "' which returns the singleton instance of the controller class itself";

                        logger.log(Level.SEVERE, cannotValidateDeclaredControllerMessage);

                        System.exit(1);
                    }

                    getInstanceMethod = controllerClass.getMethod(GET_INSTANCE_METHOD_NAME);

                    if (getInstanceMethod.isAnnotationPresent(StageManager.NaiiThinkStageManager.class) == false) {
                        logger.log(Level.SEVERE, "Cannot find a valid static annotated method '" + GET_INSTANCE_METHOD_NAME + "'");

                        System.exit(1);
                    }

                    controllerInstance = getInstanceMethod.invoke(null);

                    loader.setLocation(sceneResourcePrefixPath.resolve(sceneEntryResourceName).toUri().toURL());
                    loader.setController(controllerInstance);
                    scene = new Scene(loader.load(), primaryStageWidth, primaryStageHeight);

                    sceneTable.put(sceneEntry, new SceneMap(scene, controllerClass));
                    logger.log(Level.INFO, "Scene added: " + sceneEntry + ": **/" + sceneEntryResourceName + " => " + controllerClassName);

                    // sceneEntryResourceName = null;
                    // controllerClassName = null;
                    // controllerClass = null;
                    // getInstanceMethod = null;
                    // controllerInstance = null;

                    // loader = null;
                    // scene = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, "Got 'IOException' while loading FXML resource: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Controller class found: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            logger.log(Level.SEVERE, "Method '" + GET_INSTANCE_METHOD_NAME + "' not found");
        } catch (IllegalAccessException e) {
            /**
             * An IllegalAccessException is thrown when an application tries to reflectively create an instance (other than an array),
             * set or get a field, or invoke a method, but the currently executing method does not have access to the definition of
             * the specified class, field, method or constructor.
             */
            logger.log(Level.SEVERE, "Got illegal access: " + e.getMessage());
        } catch (InvocationTargetException e) {
            /**
             * InvocationTargetException is a checked exception that wraps an exception thrown by an invoked method or constructor.
             */
            logger.log(Level.SEVERE, "Got an exception while getting instance of controller class");
        }
    }

    /**
     * Dispatch controls over Stages of the application to this StageManager
     * 
     * @param       sceneResourceIndexPathString        String representation of the Path to the FXML index property file
     * @param       sceneResourcePrefixPathString       String representation of the prefix Path of FXML files to be loaded
     * @param       mainApp                             The instance of the main entry point of the JavaFX application
     * @param       primaryStage                        The primary Stage of the application
     * @param       primaryStageTitle                   Title of the primary Stage
     * @param       primaryStageWidth                   Width of the primary Stage
     * @param       primaryStageHeight                  Height of the primary Stage
     */
    public void dispatch(String sceneResourceIndexPathString,
                         String sceneResourcePrefixPathString,
                         Object mainApp,
                         Stage primaryStage,
                         String primaryStageTitle,
                         double primaryStageWidth,
                         double primaryStageHeight) {

        Objects.nonNull(sceneResourceIndexPathString);

        if (Files.exists(Paths.get(sceneResourceIndexPathString)) == false) {
            logger.log(Level.SEVERE, "FXML index file not found");

            System.exit(1);
        }

        dispatch(Paths.get(sceneResourceIndexPathString),
                 Paths.get(sceneResourcePrefixPathString),
                 mainApp,
                 primaryStage,
                 primaryStageTitle,
                 primaryStageWidth,
                 primaryStageHeight);
    }

    /**
     * Defines Scene entry of the primary Scene to the first record in FXML index property file
     * 
     * @see homeSceneEntry
     * 
     * @throws      SceneNotFoundException       when the first record in FXML index property file define a Scene that is not in the Scene table
     */
    public void defineHomeSceneFromIndexFile() throws SceneNotFoundException {
        String defaultSceneProperty = new String();

        try (BufferedReader in = Files.newBufferedReader(sceneResourceIndexPath, StandardCharsets.UTF_8)) {

            if (in.ready()) {
                defaultSceneProperty = in.readLine();
            }

                homeSceneEntry = Optional.ofNullable(defaultSceneProperty.split("\\=")[0])
                                         .filter(s -> sceneTable.containsKey(s))
                                         .get();

                logger.log(Level.INFO, "Defined home scene to '" + homeSceneEntry + "'");

                return;
        } catch (NoSuchElementException e) {
            logger.log(Level.SEVERE, "Trying to define unrecognizable home scene entry");
            throw new SceneNotFoundException(defaultSceneProperty);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Got IOException while trying to define home scene from FXML index file");
            throw new SceneNotFoundException(defaultSceneProperty, e);
        }
    }

    /**
     * Defines Scene entry of the primary Scene to the first record in FXML index property file.
     * 
     * @param       sceneEntry          Entry of a Scene to be defined to
     * 
     * @throws      SceneNotFoundException       when sceneEntry is not in the Scene table
     * 
     * @see homeSceneEntry
     */
    public boolean defineHomeSceneTo(String sceneEntry) throws SceneNotFoundException {
        if (sceneTable.containsKey(sceneEntry)) {
            this.homeSceneEntry = sceneEntry;

            logger.log(Level.INFO, "Defined home scene to '" + this.homeSceneEntry + "'");
            return true;
        }

        logger.log(Level.SEVERE, "Trying to define unrecognizable home scene entry");
        throw new SceneNotFoundException(sceneEntry);
    }

    /**
     * Adds a Scene to the Scene table
     * 
     * @param       sceneEntry          Entry for the Scene to be added
     * @param       scenePathString     String representation of the Path to FXML resource
     * 
     * @throws      SceneNotFoundException       when sceneEntry is not in the Scene table
     * 
     * @see sceneTable
     */
    public void addScene(String sceneEntry,
                         String scenePathString) throws FileNotFoundException {

        Path scenePath = sceneResourcePrefixPath.resolve(scenePathString);

        if (Files.exists(scenePath)
            && Files.isRegularFile(scenePath)) {

            DocumentBuilderFactory builders = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            Document doc;
            Element fxmlRoot;

            String controllerClassName = new String();
            Class<?> controllerClass;
            Method getInstanceMethod;
            Object controllerInstance;

            FXMLLoader loader;
            Scene scene;

            try (InputStream in = Files.newInputStream(scenePath)) {

                loader = new FXMLLoader();

                builder = builders.newDocumentBuilder();
                doc = builder.parse(in);
                fxmlRoot = doc.getDocumentElement();

                controllerClassName = fxmlRoot.getAttribute(FXMLLoader.FX_CONTROLLER_ATTRIBUTE);

                controllerClass = Class.forName(controllerClassName);

                if (controllerClass.isAnnotationPresent(StageManager.NaiiThinkStageManager.class) == false) {
                    /**
                     * @danger DO NOT pass any message read from file to any string formatter
                     */
                    String cannotValidateDeclaredControllerMessage = "Singleton controller: cannot check if the controller class of file '" + scenePathString + "' with declared controller name '" + controllerClassName + "' is valid.\n"
                                                                     + "FXML controller MUST be annotated with '@" + StageManager.NaiiThinkStageManager.class.getCanonicalName() + "' interface and define a static method '" + GET_INSTANCE_METHOD_NAME + "' with an annotation '@" + StageManager.NaiiThinkStageManager.class.getCanonicalName() + "' which returns the singleton instance of the controller class itself";

                    logger.log(Level.SEVERE, cannotValidateDeclaredControllerMessage);

                    System.exit(1);
                }

                getInstanceMethod = controllerClass.getMethod(GET_INSTANCE_METHOD_NAME);

                if (getInstanceMethod.isAnnotationPresent(StageManager.NaiiThinkStageManager.class) == false) {
                    logger.log(Level.SEVERE, "Cannot find a valid static annotated method '" + GET_INSTANCE_METHOD_NAME + "'");

                    System.exit(1);
                }

                controllerInstance = getInstanceMethod.invoke(null);

                loader.setLocation(scenePath.toUri().toURL());
                loader.setController(controllerInstance);

                scene = new Scene(loader.load(), primaryStageWidth, primaryStageHeight);
                sceneTable.put(sceneEntry, new SceneMap(scene, controllerClass));

                logger.log(Level.INFO, "Scene added: " + sceneEntry + ": **/" + scenePathString);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Got 'IOException' while loading FXML resource: " + e.getMessage());
            }  catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                logger.log(Level.SEVERE, "Controller class found: " + e.getMessage());
            } catch (NoSuchMethodException e) {
                logger.log(Level.SEVERE, "Method '" + GET_INSTANCE_METHOD_NAME + "' not found");
            } catch (IllegalAccessException e) {
                /**
                 * An IllegalAccessException is thrown when an application tries to reflectively create an instance (other than an array),
                 * set or get a field, or invoke a method, but the currently executing method does not have access to the definition of
                 * the specified class, field, method or constructor.
                 */
                logger.log(Level.SEVERE, "Got illegal access: " + e.getMessage());
            } catch (InvocationTargetException e) {
                /**
                 * InvocationTargetException is a checked exception that wraps an exception thrown by an invoked method or constructor.
                 */
                logger.log(Level.SEVERE, "Got an exception while getting instance of controller class");
            }
        } else {
            throw new FileNotFoundException(scenePathString);
        }
    }

    /**
     * Removes a Scene from the Scene table
     * 
     * @param       sceneEntry          Entry for the Scene to be removed
     * 
     * @throws      SceneNotFoundException       when sceneEntry is not in the Scene table
     * 
     * @see sceneTable
     */
    public void removeScene(String sceneEntry) throws SceneNotFoundException {
        if (sceneTable.containsKey(sceneEntry) == false) {
            logger.log(Level.SEVERE, "Attempting to remove Scene that is not in the Scene table");
            throw new SceneNotFoundException(sceneEntry);
        }

        sceneTable.remove(sceneEntry);
    }

    /**
     * Sets the current Scene
     * 
     * @param       sceneEntry          Entry for a Scene in the Scene table to be used
     * 
     * @throws      SceneNotFoundException       when sceneEntry is not in the Scene table
     * 
     * @see sceneTable
     * @see addScene
     * @see removeScene
     */
    public void setScene(String sceneEntry) throws SceneNotFoundException {
        if (homeSceneEntry == null) {
            // defineHomeSceneFromIndexFile();
            logger.log(Level.SEVERE, "Home Scene has not been set");
            throw new SceneNotFoundException();
        }

        if (sceneTable.containsKey(sceneEntry)) {
            this.primaryStage.setScene(sceneTable.get(sceneEntry).scene);
            logger.log(Level.INFO, "Current scene has been set to '" + sceneEntry + "'");
        } else {
            logger.log(Level.SEVERE, "Requesting to set unrecognized scene entry '" + sceneEntry + "', current scene will not be changed");
        }
    }

    /**
     * Shows the primary Stage containing the home Scene
     * 
     * @throws      SceneNotFoundException       when the home Scene has not been set
     * 
     * @see sceneEntry
     * @see defineHomeSceneTo
     */
    public void activate() throws SceneNotFoundException {
        if (homeSceneEntry == null) {
            // defineHomeSceneFromIndexFile();
            logger.log(Level.SEVERE, "Cannot set initial Scene, home Scene has not been set");
            throw new SceneNotFoundException();
        }

        this.primaryStage.setTitle(primaryStageTitle);
        this.primaryStage.setScene(sceneTable.get(homeSceneEntry).scene);

        this.primaryStage.show();

        logger.log(Level.INFO, "Activated stage");
    }
}
