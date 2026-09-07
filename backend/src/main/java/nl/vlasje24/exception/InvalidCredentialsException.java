package nl.vlasje24.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Ongeldige inloggegevens");
    }
}
