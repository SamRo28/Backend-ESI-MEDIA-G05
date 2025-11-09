package iso25.g05.esi_media.dto;

import java.util.Date;

/**
 * DTO estándar para respuestas de error (400/403/404, etc.).
 * Se utilizará desde un @ControllerAdvice para respuestas consistentes.
 */
public class ErrorRespuestaDTO {
    private Date timestamp = new Date();
    private int status;       // Código HTTP, p. ej. 404
    private String error;     // Texto de error, p. ej. "Not Found"
    private String mensaje;   // Mensaje claro para el cliente
    private String ruta;      // Path del endpoint que falló

    public ErrorRespuestaDTO() {}

    public ErrorRespuestaDTO(int status, String error, String mensaje, String ruta) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.ruta = ruta;
    }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
}
