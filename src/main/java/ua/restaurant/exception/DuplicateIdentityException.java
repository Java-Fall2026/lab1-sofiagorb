package ua.restaurant.exception;

public class DuplicateIdentityException extends IllegalStateException {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}