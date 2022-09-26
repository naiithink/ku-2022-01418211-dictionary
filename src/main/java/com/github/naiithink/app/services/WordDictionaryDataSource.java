/**
 * UserDefinedFileAttributeView
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/attribute/UserDefinedFileAttributeView.html
 * 
 * FileStore#supportsFileAttributeView()
 * https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/FileStore.html#supportsFileAttributeView(java.lang.Class)
 */

package com.github.naiithink.app.services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.models.Word;
import com.github.naiithink.app.models.WordClass;
import com.github.naiithink.app.util.resources.DataSource;
import com.github.naiithink.app.util.resources.MalformedDataSourceException;

public class WordDictionaryDataSource
        implements DataSource<Word> {

    private static Logger logger;

    private Path path;

    static {
        logger = Logger.getLogger(WordDictionaryDataSource.class.getName());
    }

    public WordDictionaryDataSource(Path path) {
        this.path = path;

        if (Files.exists(path)) {
            return;
        } else if (Files.exists(path.getParent())
                   && Files.isDirectory(path.getParent()) == false) {

            logger.log(Level.SEVERE, "File '" + path.getParent() + "' is not a directory");
            System.exit(1);
        } else if (Files.exists(path.getParent()) == false) {
            try {
                Files.createDirectory(this.path.getParent());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot create directory '" + this.path.getParent().toAbsolutePath().toString() + "'");
                e.printStackTrace();
                System.exit(1);
            }
        }

        try (BufferedWriter out = Files.newBufferedWriter(this.path, StandardOpenOption.CREATE_NEW)) {

            Objects.nonNull(out);

            Word defaultWord = new Word(Hotspot.DefaultWordInfo.WORD_LITERAL,
                                        Hotspot.DefaultWordInfo.WORD_CLASS,
                                        Hotspot.DefaultWordInfo.WORD_DEFINITION,
                                        Hotspot.DefaultWordInfo.EXAMPLE_IN_SENTENCES);

            writeRecord(defaultWord);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // private String getEscapeNLSequenceToken() throws UnresolvableEscapeSequenceException {

    //     Optional<String> escapeSequence = Optional.empty();

    //     try {
    //         for (FileStore fileStore : FileSystems.getDefault().getFileStores()) {
    //             if (fileStore.supportsFileAttributeView("user")) {
    //                 UserDefinedFileAttributeView view = Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);
    //                 ByteBuffer attr = ByteBuffer.allocate(Integer.MAX_VALUE);

    //                 view.read("user.nlreplacement", attr);

    //                 escapeSequence = Optional.ofNullable((String) Files.getAttribute(path, "user:nlreplacement"));

    //                 escapeSequence = Optional.ofNullable(new String("{" + Instant.now().toEpochMilli() + "}"));

    //                 Files.setAttribute(path,
    //                                    "user:nlreplacement",
    //                                    escapeSequence.orElseThrow(UnresolvableEscapeSequenceException::new));

    //                 break;
    //             }
    //         }
    //     } catch (IOException e) {
    //         logger.log(Level.SEVERE, "Got 'IOException' while resolving special token sequence, stop");

    //         System.exit(1);
    //     }

    //     return new String(escapeSequence.orElseThrow(UnresolvableEscapeSequenceException::new));
    // }

    @Override
    public List<Word> readData() throws SecurityException,
                                        MalformedDataSourceException {
        Objects.nonNull(path);

        Function<String, String[]> splitToSentences = s -> s.split(Hotspot.Universal.CSV.END_OF_SENTENCE_SEARCH.getToken());

        Function<String, String> trimQuotes = s -> {
            return s.charAt(0) == '"' && s.charAt(0) == s.charAt(s.length() - 1) ? s.substring(1, s.length() - 1)
                                                                                 : s;
        };

        List<Word> wordList = new CopyOnWriteArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path)) {

            while (reader.ready()) {
                List<String> exampleInSentences = new CopyOnWriteArrayList<>();

                String line = reader.readLine();
                String[] data = line.split(Hotspot.Universal.CSV.CSV_DELIMITER.getToken() + Hotspot.Universal.CSV.CSV_WITH_QOUTE_ESCAPE.getToken());

                String wordLiteralString = data[0];
                String wordClassName = data[1];
                String wordDefinition = String.valueOf(data[2]);
                String sentenceSequence = data[3];

                wordLiteralString = Optional.ofNullable(wordLiteralString)
                                            .map(trimQuotes)
                                            .get();

                String[] sentences = Optional.ofNullable(sentenceSequence)
                                             .map(trimQuotes)
                                             .map(splitToSentences)
                                             .get();

                for (String s : sentences) {
                    s = s.replaceAll(Hotspot.Universal.CSV.END_OF_SENTENCE_ESCAPE.getToken(), Hotspot.Universal.CSV.END_OF_SENTENCE_READ.getToken());
                    exampleInSentences.add(s);
                }

                wordDefinition = Optional.ofNullable(wordDefinition)
                                         .map(trimQuotes)
                                         .get();

                // if (wordDefinition.contains(Hotspot.Universal.CSV.END_OF_SENTENCE_ESCAPE.getToken())) {
                //     Optional<String> optEscapeSequence = Optional.empty();
                //     try {
                //         optEscapeSequence = Optional.ofNullable(getEscapeNLSequenceToken());
                //     } catch (UnresolvableEscapeSequenceException e) {
                //         throw new SecurityException("Cannot resolve special token sequence");
                //     }

                //     wordDefinition = wordDefinition.replaceAll(Hotspot.Universal.CSV.END_OF_SENTENCE_ESCAPE.getToken(), optEscapeSequence.orElseThrow(SecurityException::new));
                // }

                wordDefinition = wordDefinition.replaceAll(Hotspot.Universal.CSV.END_OF_SENTENCE_READ.getToken(), "\n");
                wordDefinition = wordDefinition.replaceAll(Hotspot.Universal.CSV.END_OF_SENTENCE_ESCAPE.getToken(), Hotspot.Universal.CSV.END_OF_SENTENCE_READ.getToken());

                wordList.add(
                    new Word(wordLiteralString,
                             Enum.valueOf(WordClass.class, wordClassName),
                             wordDefinition,
                             exampleInSentences)
                );
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (PatternSyntaxException e) {
            logger.log(Level.SEVERE, "Malformed data source file");
            throw new MalformedDataSourceException(path.toString(), e);
        }

        return wordList;
    }

    @Override
    public void writeRecord(Word word) {
        Objects.nonNull(path);

        String wordDefinition = word.getWordDefinition();

        wordDefinition = wordDefinition.replaceAll(Hotspot.Universal.CSV.END_OF_SENTENCE_READ.getToken(), Hotspot.Universal.CSV.END_OF_SENTENCE_ESCAPE.getToken());
        wordDefinition = wordDefinition.replaceAll("\n", Hotspot.Universal.CSV.END_OF_SENTENCE_WRITE_REPLACE.getToken());

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            writer.write("\"" + word.getWordLiteral()  + "\""
                    + Hotspot.Universal.CSV.CSV_DELIMITER.getToken() + word.getWordClass().name()
                    + Hotspot.Universal.CSV.CSV_DELIMITER.getToken() + "\"" + wordDefinition + "\""
                    + Hotspot.Universal.CSV.CSV_DELIMITER.getToken() + "\""
            );

            for (String s : word.getExampleInSentences()) {
                s = s.replaceAll(Hotspot.Universal.CSV.END_OF_SENTENCE_READ.getToken(), Hotspot.Universal.CSV.END_OF_SENTENCE_ESCAPE.getToken());
                writer.write(s + Hotspot.Universal.CSV.END_OF_SENTENCE_WRITE.getToken());
            }

            writer.write("\"");
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
