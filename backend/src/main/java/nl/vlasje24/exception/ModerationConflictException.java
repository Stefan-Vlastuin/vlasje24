package nl.vlasje24.exception;

public class ModerationConflictException extends RuntimeException {
    public ModerationConflictException(String message) {
        super(message);
    }
}
