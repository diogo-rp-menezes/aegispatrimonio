package br.com.aegispatrimonio.exception;

/**
 * Exceção de domínio lançada quando a geração de QR Code falha
 * (ex.: texto inválido para o formato, dimensões ilegais, erro de I/O
 * na serialização PNG). Substitui o uso genérico de RuntimeException,
 * permitindo tratamento específico pelo ApplicationControllerAdvice.
 */
public class QRCodeGenerationException extends RuntimeException {

    public QRCodeGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
