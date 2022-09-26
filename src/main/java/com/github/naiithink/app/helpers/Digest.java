package com.github.naiithink.app.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MessageDigest of a String message or content of a file.
 */
public final class Digest {
    private Digest() {}

    protected static final String DEFAULT_ALGORITHM;

    protected static final Charset DEFAULT_CHARSET;

    static {
        DEFAULT_ALGORITHM = "SHA-1";
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
    }

    /**
     * Gets the default hashing algorithm.
     * 
     * @return      the default hashing algorithm
     */
    public static String getDefaultDigestAlgorithm() {
        return DEFAULT_ALGORITHM;
    }

    /**
     * Gets the default charset.
     * 
     * @return      the default charset encoding
     */
    public static Charset getDefaultCharset() {
        return DEFAULT_CHARSET;
    }

    /**
     * Converts an array of byte to a String.
     * 
     * @param       bytes   array of byte
     * 
     * @return      the converted String
     * 
     * @note INTERNAL
     */
    protected static String byteArrayToHexString(byte[] bytes) {
        String result = new String();

        for (int i = 0, len = bytes.length; i < len; i++) {
            result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }

    /**
     * Performs the hash computation on a String message.
     * 
     * @param       algorithm   Hasing algorithm to be used
     * @param       charset     Decodes the message into a sequence of bytes using the given @link java.nio.charset.Charset charset @endlink
     * @param       message     message to be digest
     * 
     * @return      the message digest
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     */
    public static String getDigestStringOfMessage(String algorithm,
                                                  Charset charset,
                                                  String message) throws NoSuchAlgorithmException {
        String result = new String();

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
            byte[] digestResultByte = messageDigest.digest(message.getBytes(charset));
            result = byteArrayToHexString(digestResultByte);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    /**
     * Performs the hash computation on a String message using the default algorithm.
     * 
     * @param       charset     Decodes the message into a sequence of bytes using the given @link java.nio.charset.Charset charset @endlink
     * @param       message     message to be digest
     * 
     * @return      the message digest
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * 
     * @see         Digest.cs.oakcoding.app.helpers.security.OakDigest#getDefaultDigestAlgorithm()
     */
    public static String getDigestStringOfMessage(Charset charset,
                                                  String message) throws NoSuchAlgorithmException {
        return getDigestStringOfMessage(DEFAULT_ALGORITHM,
                                        charset,
                                        message);
    }

    /**
     * Performs the hash computation on a String message using the default charset.
     * 
     * @param       algorithm   Hasing algorithm to be used
     * @param       message     message to be digest
     * 
     * @return      the message digest
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * 
     * @see         Digest.cs.oakcoding.app.helpers.security.OakDigest#getDefaultCharset()
     */
    public static String getDigestStringOfMessage(String algorithm,
                                                  String message) throws NoSuchAlgorithmException {
        return getDigestStringOfMessage(algorithm,
                                        DEFAULT_CHARSET,
                                        message);
    }

    /**
     * Performs the hash computation on a String message using the default algorithm and default charset.
     * 
     * @param       message     message to be digest
     * 
     * @return      the message digest
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * 
     * @see         Digest.cs.oakcoding.app.helpers.security.OakDigest#getDefaultDigestAlgorithm()
     * @see         Digest.cs.oakcoding.app.helpers.security.OakDigest#getDefaultCharset()
     */
    public static String getDigestStringOfMessage(String message) throws NoSuchAlgorithmException {
        return getDigestStringOfMessage(DEFAULT_ALGORITHM,
                                        DEFAULT_CHARSET,
                                        message);
    }

    /**
     * Performs the hash computation on content of the given Path.
     * 
     * @param       filePath    Path to a file on the current file system
     * 
     * @return      the message digest of the given Path
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * @throws      FileNotFoundException       If the given Path points to file that does not exist
     */
    public static String getDigestStringOf(String algorithm,
                                           Path filePath) throws NoSuchAlgorithmException,
                                                                 FileNotFoundException {
        if (Files.exists(filePath) == false) {
            throw new FileNotFoundException(filePath.toString());
        }

        String fileDigestString = new String();

        try (InputStream fis = Files.newInputStream(filePath, StandardOpenOption.READ)) {
            MessageDigest fileDigest = MessageDigest.getInstance(algorithm);
            DigestInputStream dis = new DigestInputStream(fis, fileDigest);

            while (dis.read() != -1);

            byte[] digestBytes = fileDigest.digest();

            fileDigestString = byteArrayToHexString(digestBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return fileDigestString;
    }

    /**
     * Performs the hash computation on content of the given Path using the default algorithm.
     * 
     * @param       filePath    Path to a file on the current file system
     * 
     * @return      the message digest of the given Path
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * @throws      FileNotFoundException       If the given Path points to file that does not exist
     * 
     * @see         Digest.cs.oakcoding.app.helpers.security.OakDigest#getDefaultDigestAlgorithm()
     */
    public static String getDigestStringOf(Path filePath) throws NoSuchAlgorithmException,
                                                                 FileNotFoundException {
        if (Files.exists(filePath) == false) {
            throw new FileNotFoundException(filePath.toString());
        }

        return getDigestStringOf(DEFAULT_ALGORITHM,
                                 filePath);
    }

    /**
     * Performs the hash computation on content of the given file.
     * 
     * @param       algorithm       Hasing algorithm to be used
     * @param       filePathString  path String pointing to a file on the current file system
     * 
     * @return      the message digest of a file pointed to by the given path String
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * @throws      FileNotFoundException       If the given file pointed to by the given path String does not exist
     * 
     */
    public static String getDigestStringOf(String algorithm,
                                           String filePathString) throws NoSuchAlgorithmException,
                                                                         FileNotFoundException {
        return getDigestStringOf(FileSystems.getDefault().getPath(filePathString));
    }

    /**
     * Performs the hash computation on content of the given file using the default algorithm.
     * 
     * @param       filePathString  path String pointing to a file on the current file system
     * 
     * @return      the message digest of a file pointed to by the given path String
     * 
     * @throws      NoSuchAlgorithmException    If the specified algorithm does not exist
     * @throws      FileNotFoundException       If the given file pointed to by the given path String does not exist
     * 
     * @see         Digest.cs.oakcoding.app.helpers.security.OakDigest#getDefaultDigestAlgorithm()
     */
    public static String getDigestStringOf(String filePathString) throws NoSuchAlgorithmException,
                                                                         FileNotFoundException {
        return getDigestStringOf(DEFAULT_ALGORITHM,
                                 filePathString);
    }
}
