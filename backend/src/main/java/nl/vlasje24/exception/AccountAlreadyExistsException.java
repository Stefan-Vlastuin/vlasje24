package nl.vlasje24.exception;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException() {
        super("De gebruikersnaam of het e-mailadres is al in gebruik");
    }
}
