package nl.vlasje24.exception;

public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException() {
        super("Verifieer eerst je e-mailadres om te kunnen reageren");
    }
}
