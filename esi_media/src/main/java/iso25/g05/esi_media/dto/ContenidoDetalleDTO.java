package iso25.g05.esi_media.dto;

/**
 * DTO para devolver el detalle de un contenido seleccionado.
 * Incluye la referencia de reproducción (URL de vídeo o endpoint de audio).
 */
public class ContenidoDetalleDTO {
    private String id;
    private String titulo;
    private String descripcion;
    /** Tipo de contenido: "AUDIO" o "VIDEO" */
    private String tipo;
    private Object caratula;
    /** Indica si el contenido es VIP (requiere usuario VIP) */
    private boolean vip;
    /**
     * Para VIDEO: será la URL externa (YouTube, etc.).
     * Para AUDIO: será el endpoint del backend, por ejemplo: "/multimedia/audio/{id}".
     */
    private String referenciaReproduccion;

    public ContenidoDetalleDTO() {}

    public ContenidoDetalleDTO(String id, String titulo, String descripcion, String tipo,
                               Object caratula, boolean vip, String referenciaReproduccion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.caratula = caratula;
        this.vip = vip;
        this.referenciaReproduccion = referenciaReproduccion;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Object getCaratula() { return caratula; }
    public void setCaratula(Object caratula) { this.caratula = caratula; }

    public boolean isVip() { return vip; }
    public void setVip(boolean vip) { this.vip = vip; }

    public String getReferenciaReproduccion() { return referenciaReproduccion; }
    public void setReferenciaReproduccion(String referenciaReproduccion) { this.referenciaReproduccion = referenciaReproduccion; }
}
