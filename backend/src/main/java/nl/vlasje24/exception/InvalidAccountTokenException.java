package nl.vlasje24.exception;

public class InvalidAccountTokenException extends RuntimeException {

    private final boolean alreadyUsed;

    private InvalidAccountTokenException(String message, boolean alreadyUsed) {
        super(message);
        this.alreadyUsed = alreadyUsed;
    }

    public static InvalidAccountTokenException invalidOrExpired() {
        return new InvalidAccountTokenException("De link is ongeldig of verlopen", false);
    }

    public static InvalidAccountTokenException alreadyUsed() {
        return new InvalidAccountTokenException("De link is al gebruikt", true);
    }

    public boolean isAlreadyUsed() {
        return alreadyUsed;
    }
}
