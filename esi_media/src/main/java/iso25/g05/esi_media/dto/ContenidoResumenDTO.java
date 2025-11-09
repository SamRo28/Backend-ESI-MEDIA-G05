package iso25.g05.esi_media.dto;

/**
 * DTO para listar contenidos accesibles para un visualizador.
 * Campos mínimos para pintar tarjetas/listas en el frontend.
 */
public class ContenidoResumenDTO {
    private String id;
    private String titulo;
    /** Tipo de contenido: "AUDIO" o "VIDEO" */
    private String tipo;
    /** Carátula asociada al contenido (mismo tipo que en el modelo) */
    private Object caratula;
    /** Indica si el contenido es VIP (requiere usuario VIP) */
    private boolean vip;

    public ContenidoResumenDTO() {}

    public ContenidoResumenDTO(String id, String titulo, String tipo, Object caratula, boolean vip) {
        this.id = id;
        this.titulo = titulo;
        this.tipo = tipo;
        this.caratula = caratula;
        this.vip = vip;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Object getCaratula() { return caratula; }
    public void setCaratula(Object caratula) { this.caratula = caratula; }

    public boolean isVip() { return vip; }
    public void setVip(boolean vip) { this.vip = vip; }
}
