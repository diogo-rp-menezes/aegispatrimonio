package br.com.aegispatrimonio.exception;

public class NotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L; // ← ADICIONE ESTA LINHA
    
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}