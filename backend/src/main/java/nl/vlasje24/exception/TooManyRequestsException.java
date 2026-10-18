package nl.vlasje24.exception;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException() {
        super("Te veel verzoeken; probeer het later opnieuw");
    }
}
