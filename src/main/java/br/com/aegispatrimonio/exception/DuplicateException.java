package br.com.aegispatrimonio.exception;

public class DuplicateException extends RuntimeException {
    private static final long serialVersionUID = 2L; // ← ADICIONE ESTA LINHA
    
    public DuplicateException(String message) {
        super(message);
    }
    
    public DuplicateException(String message, Throwable cause) {
        super(message, cause);
    }
}