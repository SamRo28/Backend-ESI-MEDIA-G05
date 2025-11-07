package iso25.g05.esi_media.controller;

import iso25.g05.esi_media.dto.ErrorRespuestaDTO;
import iso25.g05.esi_media.exception.AccesoNoAutorizadoException;
import iso25.g05.esi_media.exception.PeticionInvalidaException;
import iso25.g05.esi_media.exception.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Manejador global de excepciones para respuestas de error consistentes.
 *
 * Objetivo:
 * - Convertir excepciones de negocio a respuestas HTTP claras (400/403/404),
 *   con un cuerpo estándar (ErrorRespuestaDTO) y un pequeño log.
 * - Mantener los controladores limpios y sin repetición de try/catch.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PeticionInvalidaException.class)
    public ResponseEntity<ErrorRespuestaDTO> handlePeticionInvalida(PeticionInvalidaException ex,
                                                                    HttpServletRequest request) {
        ErrorRespuestaDTO body = new ErrorRespuestaDTO(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        // Nivel bajo: no llena el log con ruido
        log.debug("400 Bad Request en {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(AccesoNoAutorizadoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleAccesoNoAutorizado(AccesoNoAutorizadoException ex,
                                                                      HttpServletRequest request) {
        ErrorRespuestaDTO body = new ErrorRespuestaDTO(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        // 403 suele ser esperado (p. ej., edad/VIP). Log como warning.
        log.warn("403 Forbidden en {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorRespuestaDTO> handleNoEncontrado(RecursoNoEncontradoException ex,
                                                                HttpServletRequest request) {
        ErrorRespuestaDTO body = new ErrorRespuestaDTO(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        // 404 es informativo; nivel info
        log.info("404 Not Found en {}: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Fallback para cualquier error no contemplado.
     * No forma parte estricta del paso 7, pero aporta robustez mínima.
     */
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<ErrorRespuestaDTO> handleMaxUpload(MaxUploadSizeExceededException ex,
                                                                                                                         HttpServletRequest request) {
                ErrorRespuestaDTO body = new ErrorRespuestaDTO(
                                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                                HttpStatus.PAYLOAD_TOO_LARGE.getReasonPhrase(),
                                "El archivo supera el tamaño máximo permitido",
                                request.getRequestURI()
                );
                log.warn("413 Payload Too Large en {}: {}", request.getRequestURI(), ex.getMessage());
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
        }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuestaDTO> handleGenerico(Exception ex, HttpServletRequest request) {
        ErrorRespuestaDTO body = new ErrorRespuestaDTO(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Error interno del servidor",
                request.getRequestURI()
        );
        log.error("500 Internal Server Error en {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
