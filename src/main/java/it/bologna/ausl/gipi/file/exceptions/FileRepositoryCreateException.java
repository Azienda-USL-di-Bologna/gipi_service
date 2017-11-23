package it.bologna.ausl.gipi.file.exceptions;

/**
 * Created by user on 27/06/2017.
 */
public class FileRepositoryCreateException extends Exception {

    public FileRepositoryCreateException(String message) {
        super(message);
    }

    public FileRepositoryCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
