package de.julielab.xml.util;

public class XMIBuilderException extends XMIManipulationException{
    public XMIBuilderException() {
    }

    public XMIBuilderException(String message) {
        super(message);
    }

    public XMIBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMIBuilderException(Throwable cause) {
        super(cause);
    }

    public XMIBuilderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
