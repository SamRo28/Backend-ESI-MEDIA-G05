package iso25.g05.esi_media.exception;

/**
 * Excepción para accesos no autorizados (403)
 */
public class AccesoNoAutorizadoException extends RuntimeException {
    public AccesoNoAutorizadoException(String message) {
        super(message);
    }
}
