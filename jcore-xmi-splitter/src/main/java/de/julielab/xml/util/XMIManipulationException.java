package de.julielab.xml.util;

public class XMIManipulationException extends Exception {
    public XMIManipulationException() {
    }

    public XMIManipulationException(String message) {
        super(message);
    }

    public XMIManipulationException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMIManipulationException(Throwable cause) {
        super(cause);
    }

    public XMIManipulationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
