package com.github.naiithink.app.controllers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.github.naiithink.app.controllers.StageController.NaiiThinkGetStageControllerInstance;
import com.github.naiithink.app.controllers.StageController.NaiiThinkStageController;
import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.models.Word;
import com.github.naiithink.app.models.WordClass;
import com.github.naiithink.app.models.WordDictionary;

import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

@NaiiThinkStageController
public final class WordEditorController {
    private WordEditorController() {}

    private static Logger logger;

    private static WordEditorController instance;

    private static StageController stageController;

    private static WordDictionary wordDictionary;

    private static WordDictionaryDataSourceController wordDictionaryDataSource;

    private String exampleSentenceToRemove;

    private ChangeListener<String> selectedExampleListener;

    @FXML
    private Button homeButton;

    @FXML
    private Rectangle newWordLiteralAccent;

    @FXML
    private TextField newWordLiteralTextField;

    @FXML
    private ChoiceBox<String> newWordClassChoiceBox;

    @FXML
    private TextArea newWordDefinitionTextArea;

    @FXML
    private ListView<String> exampleSentencesListView;

    @FXML
    private Button removeSelectedExampleSentenceButton;

    @FXML
    private TextField newExampleSentenceTextField;

    @FXML
    private Label exampleSentenceInfoLabel;

    @FXML
    private Button addExampleSentenceButton;

    @FXML
    private Label addNewWordResultLabel;

    @FXML
    private Group addNewWordResultBanner;

    @FXML
    private Rectangle addNewWordResultBannerFill;

    static {
        logger = Logger.getLogger(WordEditorController.class.getName());
        stageController = StageController.getInstance();
        wordDictionary = WordDictionary.getDictionary();
        wordDictionaryDataSource = WordDictionaryDataSourceController.getInstance();
    }

    @NaiiThinkGetStageControllerInstance
    public static WordEditorController getInstance() {
        if (instance == null) {
            synchronized (WordEditorController.class) {
                if (instance == null) {
                    instance = new WordEditorController();
                }
            }
        }

        return instance;
    }

    @FXML
    public void initialize() {
        List<String> newWordClassChoiceBoxOptionList = new CopyOnWriteArrayList<>();
        String[] wordClassArray = WordClass.getAllPrettyPrintedWordClass();

        newWordClassChoiceBoxOptionList.add(Hotspot.UI.PlaceHolder.WORD_CLASS_CHOICE_BOX_GUIDE);
        for (String wordClass : wordClassArray) {
            newWordClassChoiceBoxOptionList.add(wordClass);
        }

        newWordClassChoiceBox.setItems(FXCollections.observableArrayList(newWordClassChoiceBoxOptionList));
        removeSelectedExampleSentence();
        initialView();
        addNewWordResultBanner.setVisible(false);

        newExampleSentenceTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent kEvent) {
                if (kEvent.getCode().equals(KeyCode.ENTER)) {
                    performAddExampleSentence();
                }
            }
        });
    }

    private void initialView() {
        newWordClassChoiceBox.valueProperty().set(Hotspot.UI.PlaceHolder.WORD_CLASS_CHOICE_BOX_GUIDE);
        addNewWordResultBanner.setVisible(false);
        newWordLiteralTextField.clear();
        newWordLiteralAccent.setStroke(Color.TRANSPARENT);
        newWordDefinitionTextArea.clear();
        exampleSentencesListView.getItems().clear();
        newExampleSentenceTextField.clear();
        removeSelectedExampleSentenceButton.setVisible(false);
        exampleSentenceInfoLabel.setVisible(false);
        exampleSentenceInfoLabel.setText("exampleSentenceInfoLabel");
        newWordLiteralTextField.requestFocus();
    }

    @FXML
    public void handleHomeButton(ActionEvent event) {
        stageController.setScene("home");
        initialView();
    }

    @FXML
    public void handleAddExampleSentenceButton(ActionEvent event) {
        performAddExampleSentence();
    }

    @FXML
    public void handleRemoveSelectedExampleSentenceButton() {
        performRemoveSelectedExampleSentence();
    }

    @FXML
    public void handleAddNewWordButton(ActionEvent event) {
        performAddNewWord();
    }

    private void performAddNewWord() {
        String wordLiteralString = newWordLiteralTextField.getText().toLowerCase();

        if (newWordClassChoiceBox.getValue() == Hotspot.UI.PlaceHolder.WORD_CLASS_CHOICE_BOX_GUIDE) {
            addNewWordResultLabel.setText("Word class must be selected");
            addNewWordResultBannerFill.setFill(Color.valueOf(Hotspot.UI.Color.YELLOW.getValue()));
            newWordLiteralAccent.setStroke(Color.valueOf(Hotspot.UI.Color.YELLOW.getValue()));
            addNewWordResultBanner.setVisible(true);
            return;
        }

        WordClass wordClass = Enum.valueOf(WordClass.class, newWordClassChoiceBox.getValue().toUpperCase());
        String wordDefinitionString = newWordDefinitionTextArea.getText();

        List<String> wordExampleSentences = new CopyOnWriteArrayList<>();
        
        for (String s : exampleSentencesListView.getItems()) {
            wordExampleSentences.add(s);
        }

        Word word = new Word(wordLiteralString,
                             wordClass,
                             wordDefinitionString,
                             wordExampleSentences);

        wordDictionary.addWord(wordLiteralString, word);
        wordDictionaryDataSource.writeRecord(word);

        initialView();

        addNewWordResultLabel.setText("Word '" + wordLiteralString + "' added!");
        addNewWordResultBannerFill.setFill(Color.valueOf(Hotspot.UI.Color.GREEN.getValue()));

        addNewWordResultBanner.setVisible(true);

        PauseTransition addedWord = new PauseTransition(Duration.seconds(Hotspot.UI.AnimationDuration.STATUS_BANNER_DURATION.getValue()));

        addedWord.setOnFinished(e -> addNewWordResultBanner.setVisible(false));
        addedWord.play();
    }

    private void performAddExampleSentence() {
        String newExampleSentence = newExampleSentenceTextField.getText();
        String newExampleSentenceCaps = newExampleSentenceTextField.getText().toUpperCase();

        newWordLiteralAccent.setStroke(Color.TRANSPARENT);

        if (newWordLiteralTextField.getText().isEmpty()) {
            exampleSentenceInfoLabel.setText("Please enter some word before adding examples");
            exampleSentenceInfoLabel.setVisible(true);
            newWordLiteralAccent.setStroke(Color.valueOf(Hotspot.UI.Color.RED.getValue()));
            return;
        } else if (newExampleSentenceCaps.contains(newWordLiteralTextField.getText().toUpperCase()) == false) {
            exampleSentenceInfoLabel.setText("Sentence must contain the adding word");
            exampleSentenceInfoLabel.setVisible(true);
            newWordLiteralAccent.setStroke(Color.valueOf(Hotspot.UI.Color.YELLOW.getValue()));
            return;
        } else if (exampleSentencesListView.getItems().contains(newExampleSentence)) {
            exampleSentenceInfoLabel.setText("Sentence already in the list");
            exampleSentenceInfoLabel.setVisible(true);
            return;
        }

        exampleSentenceInfoLabel.setVisible(false);
        addToExampleSentencesListView(newExampleSentence);
        newExampleSentenceTextField.clear();
    }

    private void performRemoveSelectedExampleSentence() {
        exampleSentencesListView.refresh();
        removeSelectedExampleSentence();

        if (exampleSentencesListView.getItems().size() == 0
            && exampleSentenceToRemove == null) {

            exampleSentenceInfoLabel.setText("Select a sentence in the list to remove");
            exampleSentenceInfoLabel.setVisible(true);
            return;
        }

        exampleSentencesListView.getItems().remove(exampleSentenceToRemove);
        exampleSentencesListView.refresh();
        exampleSentenceInfoLabel.setVisible(false);
        exampleSentenceToRemove = null;

        if (exampleSentencesListView.getItems().isEmpty()) {
            removeSelectedExampleSentenceButton.setVisible(false);
            exampleSentencesListView.getSelectionModel().selectedItemProperty().removeListener(selectedExampleListener);
        }
    }

    private void addToExampleSentencesListView(String newExampleSentence) {
        exampleSentencesListView.getItems().add(newExampleSentence);
        exampleSentencesListView.refresh();
    }

    private void removeSelectedExampleSentence() {
        selectedExampleListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue,
                                String oldSentenceEntry,
                                String newSentenceEntry) {

                removeSelectedExampleSentenceButton.setVisible(true);
                exampleSentenceToRemove = newSentenceEntry;
            }
        };

        exampleSentencesListView.getSelectionModel().selectedItemProperty().addListener(selectedExampleListener);

        exampleSentencesListView.refresh();
    }
}
