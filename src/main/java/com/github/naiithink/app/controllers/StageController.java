package com.github.naiithink.app.controllers;

import java.io.BufferedReader;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.helpers.ResourcePrefix;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @todo Apply Optional fields
 * @todo Scene tree for navigation
 * @todo Multi-stage management
 */
public final class StageController {

    private static final String GET_INSTANCE_METHOD_NAME;

    private static final String INDEX_PROPERTY_TOKEN;

    private static final double DEFAULT_STAGE_WIDTH;

    private static final double DEFAULT_STAGE_HEIGHT;

    private static StageController instance;

    private Logger logger;

    private Path sceneResourcePath;

    private Map<String, SceneMap> controllerTable;

    private Properties resourceIndexProperties;

    private Object mainApp;

    private Stage primaryStage;

    private String primaryStageTitle;

    private double primaryStageWidth;

    private double primaryStageHeight;

    private String homeSceneEntry;

    public interface FXMLController {}

    @Retention (RetentionPolicy.RUNTIME)
    @Target (ElementType.TYPE)
    public @interface NaiiThinkStageController {}

    @Retention (RetentionPolicy.RUNTIME)
    @Target (ElementType.METHOD)
    public @interface NaiiThinkGetStageControllerInstance {}

    private record SceneMap(Scene scene,
                            Class<?> controllerClass) {}

    static {
        GET_INSTANCE_METHOD_NAME = "getInstance";
        INDEX_PROPERTY_TOKEN = "\\=>";
        DEFAULT_STAGE_WIDTH = 800.0;
        DEFAULT_STAGE_HEIGHT = 600.0;
    }

    private StageController() {
        logger = Logger.getLogger(getClass().getName());

        controllerTable = new ConcurrentHashMap<>();
        resourceIndexProperties = new Properties();

        primaryStage = null;
    }

    public static StageController getInstance() {
        if (instance == null) {
            synchronized (StageController.class) {
                if (instance == null) {
                    instance = new StageController();
                }
            }
        }

        return instance;
    }

    public void dispatch(Path sceneResourcePath,
                         Path sceneResourcePrefix,
                         Object mainApp,
                         Stage primaryStage,
                         String primaryStageTitle,
                         double primaryStageWidth,
                         double primaryStageHeight) {

        Objects.nonNull(sceneResourcePath);
        Objects.nonNull(mainApp);
        Objects.nonNull(primaryStage);

        if (Application.class.isAssignableFrom(mainApp.getClass()) == false) {
            logger.log(Level.SEVERE, "Unable to take control over main JavaFX entry, invalid argument type for parameter 'mainApp'");

            System.exit(1);
        }

        this.sceneResourcePath = sceneResourcePath;
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

        try (InputStream in = Files.newInputStream(sceneResourcePrefix.resolve(this.sceneResourcePath))) {

            Objects.nonNull(in);

            this.resourceIndexProperties.load(in);
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
            Set<String> sceneEntries = this.resourceIndexProperties.stringPropertyNames();

            for (String sceneEntry : sceneEntries) {
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

                    controllerTable.put(sceneEntry, new SceneMap(new Scene(FXMLLoader.load(sceneResourcePrefix.resolve(sceneEntryResourceName).toUri().toURL()), primaryStageWidth, primaryStageHeight), null));
                    logger.log(Level.INFO, "Scene added: " + sceneEntry + ": **/" + sceneEntryResourceName);
                    
                    continue;
                }

                if (sceneIndexPropertyFields.length == 2) {
                    controllerClassName = new String(sceneIndexPropertyFields[1]);
                    controllerClass = Class.forName(controllerClassName);

                    if (controllerClass.isAnnotationPresent(StageController.NaiiThinkStageController.class) == false) {
                        /**
                         * @danger DO NOT pass any message read from file to any string formatter
                         */
                        String cannotValidateDeclaredControllerMessage = "Singleton controller: cannot check if the controller class of file '" + sceneEntryResourceName + "' with declared controller name '" + controllerClassName + "' is valid.\n"
                                                                         + "FXML controller MUST be annotated with '@" + StageController.NaiiThinkStageController.class.getCanonicalName() + "' interface and define a static method '" + GET_INSTANCE_METHOD_NAME + "' with an annotation '@" + StageController.NaiiThinkGetStageControllerInstance.class.getCanonicalName() + "' which returns the singleton instance of the controller class itself";

                        logger.log(Level.SEVERE, cannotValidateDeclaredControllerMessage);

                        System.exit(1);
                    }

                    getInstanceMethod = controllerClass.getMethod(GET_INSTANCE_METHOD_NAME);

                    if (getInstanceMethod.isAnnotationPresent(StageController.NaiiThinkGetStageControllerInstance.class) == false) {
                        logger.log(Level.SEVERE, "Cannot find a valid static annotated method '" + GET_INSTANCE_METHOD_NAME + "'");

                        System.exit(1);
                    }

                    controllerInstance = getInstanceMethod.invoke(null);

                    loader = new FXMLLoader(sceneResourcePrefix.resolve(sceneEntryResourceName).toUri().toURL());
                    loader.setController(controllerInstance);
                    scene = new Scene(loader.load(), primaryStageWidth, primaryStageHeight);

                    controllerTable.put(sceneEntry, new SceneMap(scene, controllerClass));
                    logger.log(Level.INFO, "Scene added: " + sceneEntry + ": **/" + sceneEntryResourceName + " => " + controllerClassName);

                    sceneEntryResourceName = null;
                    controllerClassName = null;
                    controllerClass = null;
                    getInstanceMethod = null;
                    controllerInstance = null;

                    loader = null;
                    scene = null;
                }
            }
        } catch (IOException e) {
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

    public void dispatch(String pathStringToResourceIndexFile,
                         String sceneResourcePrefixPathString,
                         Object mainApp,
                         Stage primaryStage,
                         String primaryStageTitle,
                         double primaryStageWidth,
                         double primaryStageHeight) {

        Objects.nonNull(pathStringToResourceIndexFile);

        if (Files.exists(Paths.get(pathStringToResourceIndexFile)) == false) {
            logger.log(Level.SEVERE, "FXML index file not found");

            System.exit(1);
        }

        dispatch(Paths.get(pathStringToResourceIndexFile),
                 Paths.get(sceneResourcePrefixPathString),
                 mainApp,
                 primaryStage,
                 primaryStageTitle,
                 primaryStageWidth,
                 primaryStageHeight);
    }

    private boolean defineHomeSceneFromIndexFile() {
        String defaultSceneProperty = new String();

        try (BufferedReader in = Files.newBufferedReader(sceneResourcePath, StandardCharsets.UTF_8)) {

            if (in.ready()) {
                defaultSceneProperty = in.readLine();
            }

            homeSceneEntry = Optional.ofNullable(defaultSceneProperty.split("=")[0])
                                     .filter(s -> controllerTable.containsKey(s))
                                     .get();

            logger.log(Level.INFO, "Defined home scene to '" + this.homeSceneEntry + "'");

            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Got IOException while trying to define home scene from FXML index file");

            System.exit(1);
        }

        logger.log(Level.SEVERE, "Trying to define unrecognizable home scene entry");

        return false;
    }

    public boolean defineHomeSceneTo(String sceneEntry) {
        if (controllerTable.containsKey(sceneEntry)) {
            this.homeSceneEntry = sceneEntry;

            logger.log(Level.INFO, "Define home scene to '" + this.homeSceneEntry + "'");
            return true;
        }

        logger.log(Level.SEVERE, "Trying to define unrecognizable home scene entry");

        return false;
    }

    public void activate() {
        if (homeSceneEntry == null) {
            if (defineHomeSceneFromIndexFile() == false) {

                logger.log(Level.SEVERE, "Cannot set initial scene");

                System.exit(1);
            }
        }

        this.primaryStage.setTitle(primaryStageTitle);
        this.primaryStage.setScene(controllerTable.get(homeSceneEntry).scene);

        this.primaryStage.show();

        logger.log(Level.INFO, "Activated stage");
    }

    public void addScene(String sceneEntry,
                         String scenePathString) {

        Path scenePath = ResourcePrefix.getPrefix().resolve("fxml").resolve(scenePathString);

        if (Files.exists(scenePath)
            && Files.isRegularFile(scenePath)) {

            // DocumentBuilderFactory builders = DocumentBuilderFactory.newInstance();
            // DocumentBuilder builder;
            // Document doc;
            // Element fxmlRoot;

            String controllerClassName = new String();
            Class<?> controllerClass;
            Method getInstanceMethod;
            Object controllerInstance;

            FXMLLoader loader;
            Scene scene;

            try (InputStream in = Files.newInputStream(scenePath)) {

                // builder = builders.newDocumentBuilder();
                // doc = builder.parse(in);
                // fxmlRoot = doc.getDocumentElement();

                // String controllerClassName = fxmlRoot.getAttribute("fx:controller");

                controllerClass = Class.forName(controllerClassName);

                if (controllerClass.isAnnotationPresent(StageController.NaiiThinkStageController.class) == false) {
                    /**
                     * @danger DO NOT pass any message read from file to any string formatter
                     */
                    String cannotValidateDeclaredControllerMessage = "Singleton controller: cannot check if the controller class of file '" + scenePathString + "' with declared controller name '" + controllerClassName + "' is valid.\n"
                                                                     + "FXML controller MUST be annotated with '@" + StageController.NaiiThinkStageController.class.getCanonicalName() + "' interface and define a static method '" + GET_INSTANCE_METHOD_NAME + "' with an annotation '@" + StageController.NaiiThinkGetStageControllerInstance.class.getCanonicalName() + "' which returns the singleton instance of the controller class itself";

                    logger.log(Level.SEVERE, cannotValidateDeclaredControllerMessage);

                    System.exit(1);
                }

                getInstanceMethod = controllerClass.getMethod(GET_INSTANCE_METHOD_NAME);

                if (getInstanceMethod.isAnnotationPresent(StageController.NaiiThinkGetStageControllerInstance.class) == false) {
                    logger.log(Level.SEVERE, "Cannot find a valid static annotated method '" + GET_INSTANCE_METHOD_NAME + "'");

                    System.exit(1);
                }

                controllerInstance = getInstanceMethod.invoke(null);

                loader = new FXMLLoader(scenePath.toUri().toURL());
                loader.setController(controllerInstance);

                scene = new Scene(loader.load(), primaryStageWidth, primaryStageHeight);
                controllerTable.put(sceneEntry, new SceneMap(scene, controllerClass));

                logger.log(Level.INFO, "Scene added: " + sceneEntry + ": */" + scenePathString);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Got 'IOException' while loading FXML resource: " + e.getMessage());
            // }  catch (ParserConfigurationException e) {
            //     e.printStackTrace();
            // } catch (SAXException e) {
            //     e.printStackTrace();
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
    }

    public void removeScene(String sceneEntry) {
        controllerTable.remove(sceneEntry);
    }

    public void setScene(String sceneEntry) {
        if (homeSceneEntry == null) {
            defineHomeSceneFromIndexFile();
        }

        if (controllerTable.containsKey(sceneEntry)) {
            this.primaryStage.setScene(controllerTable.get(sceneEntry).scene);
            logger.log(Level.INFO, "Current scene has been set to '" + sceneEntry + "'");
        } else {
            logger.log(Level.SEVERE, "Requesting to set unrecognized scene entry '" + sceneEntry + "', current scene will not be changed");
        }
    }
}
