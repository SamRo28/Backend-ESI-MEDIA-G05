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
    /** Duración en segundos */
    private double duracion;
    /** Indica si el contenido está visible (estado) */
    private boolean estado;

    // Información adicional de detalle que debe mostrarse al visualizador/gestor
    private java.util.Date fechadisponiblehasta;
    private int edadvisualizacion;
    private int nvisualizaciones;
    private String resolucion;
    private java.util.List<String> tags;

    /**
     * Para VIDEO: será la URL externa (YouTube, etc.).
     * Para AUDIO: será el endpoint del backend, por ejemplo: "/multimedia/audio/{id}".
     */
    private String referenciaReproduccion;

    private String creadorNombre;
    private String creadorApellidos;

    private java.util.Date fechaCreacion;

    public ContenidoDetalleDTO() {
    }

    /**
     * Constructor de compatibilidad (sin duración). Mantiene compatibilidad con tests existentes.
     * Asigna duracion = -1 para indicar "no indicada".
     */
    public ContenidoDetalleDTO(String id, String titulo, String descripcion, String tipo,
                               Object caratula, boolean vip,
                               java.util.Date fechadisponiblehasta, int edadvisualizacion,
                               int nvisualizaciones, java.util.List<String> tags,
                               String referenciaReproduccion, String resolucion) {
        this(id, titulo, descripcion, tipo, caratula, vip, -1d,
                fechadisponiblehasta, edadvisualizacion, nvisualizaciones, tags, referenciaReproduccion, resolucion);
    }

    public ContenidoDetalleDTO(String id, String titulo, String descripcion, String tipo,
                               Object caratula, boolean vip, double duracion,
                               java.util.Date fechadisponiblehasta, int edadvisualizacion,
                               int nvisualizaciones, java.util.List<String> tags,
                               String referenciaReproduccion, String resolucion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.caratula = caratula;
        this.vip = vip;
        this.duracion = duracion;
        this.fechadisponiblehasta = fechadisponiblehasta;
        this.edadvisualizacion = edadvisualizacion;
        this.nvisualizaciones = nvisualizaciones;
        this.tags = tags;
        this.referenciaReproduccion = referenciaReproduccion;
        this.resolucion = resolucion;
        // por defecto, estado visible; el mapper lo sobreescribirá
        this.estado = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Object getCaratula() {
        return caratula;
    }

    public void setCaratula(Object caratula) {
        this.caratula = caratula;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public double getDuracion() {
        return duracion;
    }

    public void setDuracion(double duracion) {
        this.duracion = duracion;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public java.util.Date getFechaDisponibleHasta() {
        return fechadisponiblehasta;
    }

    public void setFechaDisponibleHasta(java.util.Date fechadisponiblehasta) {
        this.fechadisponiblehasta = fechadisponiblehasta;
    }

    public int getEdadVisualizacion() {
        return edadvisualizacion;
    }

    public void setEdadVisualizacion(int edadvisualizacion) {
        this.edadvisualizacion = edadvisualizacion;
    }

    public int getNvisualizaciones() {
        return nvisualizaciones;
    }

    public void setNvisualizaciones(int nvisualizaciones) {
        this.nvisualizaciones = nvisualizaciones;
    }

    public java.util.List<String> getTags() {
        return tags;
    }

    public void setTags(java.util.List<String> tags) {
        this.tags = tags;
    }

    public String getReferenciaReproduccion() {
        return referenciaReproduccion;
    }

    public void setReferenciaReproduccion(String referenciaReproduccion) {
        this.referenciaReproduccion = referenciaReproduccion;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }

    public String getCreadorNombre() {
        return creadorNombre;
    }

    public void setCreadorNombre(String creadorNombre) {
        this.creadorNombre = creadorNombre;
    }

    public String getCreadorApellidos() {
        return creadorApellidos;
    }

    public void setCreadorApellidos(String creadorApellidos) {
        this.creadorApellidos = creadorApellidos;
    }

    public java.util.Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(java.util.Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
