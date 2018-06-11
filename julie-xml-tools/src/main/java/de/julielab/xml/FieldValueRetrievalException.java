package de.julielab.xml;

public class FieldValueRetrievalException extends Exception {
    public FieldValueRetrievalException() {
    }

    public FieldValueRetrievalException(String message) {
        super(message);
    }

    public FieldValueRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

    public FieldValueRetrievalException(Throwable cause) {
        super(cause);
    }

    public FieldValueRetrievalException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
