package it.bologna.ausl.gipi.exceptions;

public class GipiPubblicazioneException extends Exception {

    public GipiPubblicazioneException(String message) {
        super(message);
    }

    public GipiPubblicazioneException(String message, Throwable cause) {
        super(message, cause);
    }

    public GipiPubblicazioneException(Throwable cause) {
        super(cause);
    }
}
