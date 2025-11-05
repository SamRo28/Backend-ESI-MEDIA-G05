package iso25.g05.esi_media.exception;

/**
 * Excepción para recursos no encontrados (404)
 */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
