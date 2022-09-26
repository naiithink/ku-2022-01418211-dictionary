package com.github.naiithink.app.controllers;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.controllers.StageController.NaiiThinkGetStageControllerInstance;
import com.github.naiithink.app.controllers.StageController.NaiiThinkStageController;
import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.models.Word;
import com.github.naiithink.app.models.WordDictionary;
import com.github.naiithink.app.util.events.listeners.EventListener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

@NaiiThinkStageController
public final class HomeController
        implements EventListener {

    private static HomeController instance;

    private static Logger logger;

    private static StageController stageController;

    private static WordDictionary wordDictionary;

    private static Set<String> allWordEntries;

    private static Set<String> searchResultList;

    @FXML
    private Label allWordsLabel;

    @FXML
    private ListView<String> allWordsListView;

    @FXML
    private TextField wordSearchTokenTextField;

    @FXML
    private Button wordSearchButton;

    @FXML
    private Label theWordLiteralLabel;

    @FXML
    private Label theWordClassLabel;

    @FXML
    private Button addNewWordButton;

    @FXML
    private Label theWordDefinitionLabelLabel;

    @FXML
    private TextFlow theWordDefinitionTextFlow;

    @FXML
    private Label theWordExampleSentencesLabelLabel;

    @FXML
    private ListView<String> theExampleSentencesListView;

    static {
        logger = Logger.getLogger(HomeController.class.getName());

        stageController = StageController.getInstance();

        wordDictionary = WordDictionary.getDictionary();
        allWordEntries = new ConcurrentSkipListSet<>();
        searchResultList = new ConcurrentSkipListSet<>();
    }

    @NaiiThinkGetStageControllerInstance
    public static HomeController getInstance() {
        if (instance == null) {
            synchronized (HomeController.class) {
                if (instance == null) {
                    instance = new HomeController();
                }
            }
        }

        return instance;
    }

    @FXML
    public void initialize() {
        theWordDefinitionTextFlow.setLineSpacing(2.0);
        theWordDefinitionTextFlow.setPadding(new Insets(1.0, 1.0, 1.0, 1.0));
        theWordDefinitionTextFlow.setTextAlignment(TextAlignment.LEFT);

        wordSearchTokenTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent kEvent) {
                if (kEvent.getCode().equals(KeyCode.ENTER)) {
                    performWordSearch();
                }
            }
        });

        handleSelectedWord();
        showAllWordsListView();
        initialView();
    }

    private void initialView() {
        showAllWordsListView();
        allWordsListView.getSelectionModel().select(Hotspot.DEFAULT_WORD_ENTRY);
        allWordsListView.scrollTo(Hotspot.DEFAULT_WORD_ENTRY);
    }

    @FXML
    public void handleSpecialOfferLabel() {
        ButtonType subscribeButton = new ButtonType("Subscribe", ButtonData.YES);
        ButtonType subscribeRightNowButton = new ButtonType("Subscribe Right Now", ButtonData.YES);
        Dialog<String> specialOfferInfoDialog = new Dialog<>();

        specialOfferInfoDialog.setTitle("Special Offer!");
        specialOfferInfoDialog.setContentText("Earn premium benefit now!" + "\n"
                                              + "- Store unlimited words*" + "\n"
                                              + "\n"
                                              + "* The maximum number of words is limited to your device's storage. "
                                              + "But we will remove the in-app limit so what is limiting you from saving your brilliant words is your device, not our app ;D ");
        specialOfferInfoDialog.getDialogPane().getButtonTypes().add(subscribeButton);
        specialOfferInfoDialog.getDialogPane().getButtonTypes().add(subscribeRightNowButton);

        Optional<String> userResponse = specialOfferInfoDialog.showAndWait();

        if (userResponse.isPresent()) {
            switch (userResponse.get()) {
                case "Subscribe":
                case "Subscribe Right Now":
                    System.out.println("Woo hoo! user considered subscribing.");
            }
        }

    }

    @FXML
    public void handleWordSearchButton(ActionEvent event) {
        performWordSearch();
    }

    @FXML
    public void handleAddNewWordButton(ActionEvent event) {
        stageController.setScene("word_editor");
    }

    private void performWordSearch() {
        searchResultList.clear();
        Optional<String> searchToken = Optional.ofNullable(wordSearchTokenTextField.getText())
                                               .map(s -> s.toLowerCase().trim())
                                               .or(() -> Optional.of(new String()));

        if (searchToken.get().isBlank()) {
            showAllWordsListView();
        } else if (wordDictionary.containsWord(searchToken.get())) {
            searchResultList.add(wordDictionary.getWord(searchToken.get()).getWordLiteral());
            allWordsListView.getItems().setAll(searchResultList);
        } else if (wordDictionary.containsWord(searchToken.get()) == false) {
            allWordsListView.getItems().clear();
        }
    }

    private void showAllWordsListView() {
        allWordEntries.addAll(wordDictionary.getAllWordEntries());
        allWordsListView.getItems().setAll(allWordEntries);
        allWordsListView.refresh();
    }

    private void handleSelectedWord() {
        allWordsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue,
                                String oldWordEntry,
                                String newWordEntry) {

                if (newWordEntry != null) {
                    showSelectedWord(wordDictionary.getWord(newWordEntry));
                }
            }
        });
    }

    private void showSelectedWord(Word word) {
        theWordLiteralLabel.setText(word.getWordLiteral());
        theWordClassLabel.setText(word.getWordClass().getPrettyPrinted());

        Text theWordDefinitionText = new Text(word.getWordDefinition());
        theWordDefinitionText.setFont(Font.font("Noto Sans Display Condensed Medium", 14));

        theWordDefinitionTextFlow.getChildren().setAll(theWordDefinitionText);
        theWordDefinitionTextFlow.setPadding(new Insets(2.0, 2.0, 2.0, 2.0));
        updateTheExampleSentencesListView(word);
    }

    private void updateTheExampleSentencesListView(Word word) {
        theExampleSentencesListView.getItems().setAll(word.getExampleInSentences());
    }

    @Override
    public void update(Object context) {
        showAllWordsListView();
        logger.log(Level.INFO, "Got an event!");
    }
}
