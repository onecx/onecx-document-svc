package org.tkit.onecx.document.rs.internal.exceptions;

public class DocumentException extends RuntimeException {

    private final Enum<?> key;

    public DocumentException(Enum<?> key, String message) {
        super(message);
        this.key = key;
    }

    public DocumentException(Enum<?> key, String message, Throwable t) {
        super(message, t);
        this.key = key;
    }

    @SuppressWarnings("java:S1452")
    public Enum<?> getKey() {
        return key;
    }

}
