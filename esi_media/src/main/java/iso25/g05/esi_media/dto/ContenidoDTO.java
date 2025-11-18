package iso25.g05.esi_media.dto;

import java.util.List;

/**
 * DTO para transferir información de contenidos en el filtrado avanzado
 */
public class ContenidoDTO {
    
    private String id;
    private String titulo;
    private String descripcion;
    private String tipo;
    private int nvisualizaciones;
    private String thumbnailUrl;
    private int edadvisualizacion;
    private boolean estado;
    private List<String> tags;
    private String resolucion; // Solo para videos
    
    // Constructor vacío
    public ContenidoDTO() {}
    
    // Constructor completo
    public ContenidoDTO(String id, String titulo, String descripcion, String tipo, 
                        int nvisualizaciones, String thumbnailUrl, int edadvisualizacion, 
                        boolean estado, List<String> tags, String resolucion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.nvisualizaciones = nvisualizaciones;
        this.thumbnailUrl = thumbnailUrl;
        this.edadvisualizacion = edadvisualizacion;
        this.estado = estado;
        this.tags = tags;
        this.resolucion = resolucion;
    }
    
    // Getters y Setters
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
    
    public int getNvisualizaciones() {
        return nvisualizaciones;
    }
    
    public void setNvisualizaciones(int nvisualizaciones) {
        this.nvisualizaciones = nvisualizaciones;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public int getEdadvisualizacion() {
        return edadvisualizacion;
    }
    
    public void setEdadvisualizacion(int edadvisualizacion) {
        this.edadvisualizacion = edadvisualizacion;
    }
    
    public boolean isEstado() {
        return estado;
    }
    
    public void setEstado(boolean estado) {
        this.estado = estado;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public String getResolucion() {
        return resolucion;
    }
    
    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }
}