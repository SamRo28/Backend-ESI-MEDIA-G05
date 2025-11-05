package iso25.g05.esi_media.exception;

/**
 * Excepción para peticiones inválidas (400)
 */
public class PeticionInvalidaException extends RuntimeException {
    public PeticionInvalidaException(String message) {
        super(message);
    }
}
