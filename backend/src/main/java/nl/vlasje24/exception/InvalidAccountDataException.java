package nl.vlasje24.exception;

public class InvalidAccountDataException extends RuntimeException {
    public InvalidAccountDataException(String message) {
        super(message);
    }
}
