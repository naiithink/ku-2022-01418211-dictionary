package com.github.naiithink.app.controllers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.controllers.StageController.NaiiThinkGetStageControllerInstance;
import com.github.naiithink.app.controllers.StageController.NaiiThinkStageController;
import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.models.Word;
import com.github.naiithink.app.models.WordClass;
import com.github.naiithink.app.models.WordDictionary;
import com.github.naiithink.app.util.Filterer;
import com.github.naiithink.app.util.events.listeners.EventListener;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
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

    private static ExecutorService eService;

    private static StageController stageController;

    private static WordDictionary wordDictionary;

    private static Set<String> allWordEntries;

    private static Set<Word> allWordSet;

    private static Set<String> filteredAllWordEntries;

    private static Set<Word> filteredAllWordSet;

    private static Set<String> searchResultList;

    private static Optional<WordClass> wordClassFilter;

    @FXML
    private Label allWordsLabel;

    @FXML
    private ListView<String> allWordsListView;

    @FXML
    private TextField wordSearchTokenTextField;

    @FXML
    private Button wordSearchButton;

    @FXML
    private ChoiceBox<String> wordClassFilterChoiceBox;

    @FXML
    private CheckBox showExactSearchMatchCheckBox;

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

    private enum SkimOption {

        WORD_LITERAL,
        DEFINITION,
        SENTENCES;
    }

    private class SkimWordsThread
            implements Callable<Set<String>> {

        private volatile SkimOption skimScope;
        
        private volatile Collection<? extends Word> wordsToSkim;

        private volatile String searchSequence;

        public SkimWordsThread(SkimOption scope,
                                          Collection<? extends Word> words,
                                          String sequence) {

            skimScope = scope;
            wordsToSkim = words;
            searchSequence = new String(sequence);
        }

        @Override
        public Set<String> call() {
            Set<String> result = new CopyOnWriteArraySet<>();

            switch (skimScope) {
                case WORD_LITERAL:
                    for (Word word : wordsToSkim) {
                        if (word.getWordLiteral().contains(searchSequence)) {
                            result.add(word.getWordLiteral());
                        }
                    }

                    break;
                case DEFINITION:
                    for (Word word : wordsToSkim) {
                        if (word.getExampleInSentences().contains(searchSequence)) {
                            result.add(word.getWordLiteral());
                        }
                    }

                    break;
                case SENTENCES:
                    for (Word word : wordsToSkim) {
                        for (String sentence : word.getExampleInSentences()) {
                            if (sentence.contains(searchSequence)) {
                                result.add(word.getWordLiteral());
                            }
                        }
                    }

                    break;
            };

            return result;
        }
    }

    static {
        logger = Logger.getLogger(HomeController.class.getName());

        eService = Executors.newFixedThreadPool(3);

        stageController = StageController.getInstance();

        wordDictionary = WordDictionary.getDictionary();

        allWordEntries = new ConcurrentSkipListSet<>();
        filteredAllWordEntries = new ConcurrentSkipListSet<>();
        searchResultList = new ConcurrentSkipListSet<>();
        allWordSet = new ConcurrentSkipListSet<>();
        filteredAllWordSet = new ConcurrentSkipListSet<>();

        wordClassFilter = Optional.empty();
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
        System.out.println("yes");
        List<String> wordClassChoiceBoxOptionsList = new CopyOnWriteArrayList<>();
        String[] wordClassArray = WordClass.getAllPrettyPrintedWordClass();

        wordClassChoiceBoxOptionsList.add(Hotspot.UI.PlaceHolder.WORD_CLASS_CHOICE_BOX_GUIDE);

        for (String wordClass : wordClassArray) {
            wordClassChoiceBoxOptionsList.add(wordClass);
        }

        wordClassFilterChoiceBox.setItems(FXCollections.observableArrayList(wordClassChoiceBoxOptionsList));

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

        allWordEntries.addAll(wordDictionary.getAllWordEntries());
        filteredAllWordEntries.addAll(allWordEntries);
        allWordSet.addAll(wordDictionary.getAllWords());
        filteredAllWordSet.addAll(allWordSet);

        handleSelectedWord();
        showAllWordsListView();
        initialView();
    }

    private void initialView() {
        wordClassFilterChoiceBox.valueProperty().set(Hotspot.UI.PlaceHolder.WORD_CLASS_CHOICE_BOX_GUIDE);
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
        Filterer<List<Word>, Optional<WordClass>> wordClassFilterer = (data, filter) -> {
            if (filter.isPresent() == false) {
                return data;
            }

            List<Word> result = new CopyOnWriteArrayList<>();

            for (Word word : data) {
                if (filter.get().equals(word.getWordClass())) {
                    result.add(word);
                }
            }

            return result;
        };

        filteredAllWordEntries.clear();
        filteredAllWordSet.clear();

        Optional<String> searchToken = Optional.ofNullable(wordSearchTokenTextField.getText())
                                               .map(s -> s.toLowerCase().trim());

        String wordClassSelectedFilter = wordClassFilterChoiceBox.getValue().toUpperCase();

        if (wordClassSelectedFilter.equals(Hotspot.UI.PlaceHolder.WORD_CLASS_CHOICE_BOX_GUIDE.toUpperCase())) {
            wordClassFilter = Optional.empty();
        } else {
            wordClassFilter = Optional.ofNullable(Enum.valueOf(WordClass.class, wordClassSelectedFilter));
        }

        Set<Word> filteredWordSet = new CopyOnWriteArraySet<>(wordClassFilterer.filter(wordDictionary.getAllWords(), wordClassFilter));

        for (Word word : filteredWordSet) {
            filteredAllWordEntries.add(word.getWordLiteral());
            filteredAllWordSet.add(word);
        }

        if (searchToken.get().isBlank()) {
            showAllWordsListView();
            return;
        }

        if (showExactSearchMatchCheckBox.isSelected()) {
            Set<String> searchTokenSet = new CopyOnWriteArraySet<>();
            searchTokenSet.add(searchToken.get());

            filteredAllWordEntries.retainAll(searchTokenSet);
        } else {
            // public SkimWordsThread(SkimOption scope,
            // Collection<? extends Word> words,
            // String sequence) { 

            // List<Callable<Set<String>>> skimTasks = new CopyOnWriteArrayList<>();

            // skimTasks.add(new SkimWordsThread(SkimOption.WORD_LITERAL,
            //                                              filteredAllWordSet,
            //                                              searchToken.get()));
            // skimTasks.add(new SkimWordsThread(SkimOption.DEFINITION,
            //                                              filteredAllWordSet,
            //                                              searchToken.get()));
            // skimTasks.add(new SkimWordsThread(SkimOption.SENTENCES,
            //                                              filteredAllWordSet,
            //                                              searchToken.get()));

            try {
                Future<Set<String>> skimLiteralFuture = eService.submit(this.new SkimWordsThread(SkimOption.WORD_LITERAL,
                                                                                            filteredAllWordSet,
                                                                                            searchToken.get()));
                Future<Set<String>> skimDefinitionFuture = eService.submit(this.new SkimWordsThread(SkimOption.DEFINITION,
                                                                                            filteredAllWordSet,
                                                                                            searchToken.get()));
                Future<Set<String>> skimSentencesFuture = eService.submit(this.new SkimWordsThread(SkimOption.SENTENCES,
                                                                                            filteredAllWordSet,
                                                                                            searchToken.get()));

                while ((skimLiteralFuture.isDone()
                        && skimDefinitionFuture.isDone()
                        && skimSentencesFuture.isDone()) == false) {

                    if (skimLiteralFuture.isDone()) {
                        filteredAllWordEntries.addAll(skimLiteralFuture.get());
                    }

                    if (skimDefinitionFuture.isDone()) {
                        filteredAllWordEntries.addAll(skimDefinitionFuture.get());
                    }

                    if (skimSentencesFuture.isDone()) {
                        filteredAllWordEntries.addAll(skimSentencesFuture.get());
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        showAllWordsListView();
    }

    private void showAllWordsListView() {
        allWordsListView.getItems().setAll(filteredAllWordEntries);
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
