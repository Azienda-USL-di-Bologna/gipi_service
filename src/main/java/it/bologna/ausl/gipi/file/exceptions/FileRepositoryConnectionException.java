package it.bologna.ausl.gipi.file.exceptions;

/**
 * Created by f.longhitano on 27/08/2017.
 */
public class FileRepositoryConnectionException extends Exception {

    public FileRepositoryConnectionException(String message) {
        super(message);
    }

    public FileRepositoryConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
