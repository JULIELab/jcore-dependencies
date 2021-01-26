package de.julielab.xml.util;

public class MissingBinaryMappingException extends XMISplitterException {
    private String missingItem;
    public MissingBinaryMappingException(String missingItem) {
        this.missingItem = missingItem;
    }

    public MissingBinaryMappingException(String missingItem, String message) {
        super(message);
        this.missingItem = missingItem;
    }

    public MissingBinaryMappingException(String missingItem, String message, Throwable cause) {
        super(message, cause);
        this.missingItem = missingItem;
    }

    public MissingBinaryMappingException(String missingItem, Throwable cause) {
        super(cause);
        this.missingItem = missingItem;
    }

    public String getMissingItem() {
        return missingItem;
    }

    public MissingBinaryMappingException(String missingItem, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.missingItem = missingItem;

    }
}
