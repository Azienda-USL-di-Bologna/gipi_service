package it.bologna.ausl.gipi.file.exceptions;

/**
 * Created by user on 27/06/2017.
 */
public class FileRepositoryException extends Exception {

    public FileRepositoryException(String message) {
        super(message);
    }

    public FileRepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileRepositoryException(Throwable cause) {
        super(cause);
    }
}
