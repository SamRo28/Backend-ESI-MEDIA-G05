package iso25.g05.esi_media.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * DTO para representar una Lista/Playlist de contenidos
 * Usado para transferir información entre el servicio y el controlador
 */
public class PlaylistDto {
    
    private String id;
    private String nombre;
    private String descripcion;
    private boolean visible;
    private String creadorId;
    private Set<String> tags;
    private String especializacionGestor;
    private List<String> contenidosIds;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    
    // Constructor vacío
    public PlaylistDto() {}
    
    // Constructor completo
    public PlaylistDto(String id, String nombre, String descripcion, boolean visible, 
                       String creadorId, Set<String> tags, String especializacionGestor,
                       List<String> contenidosIds, LocalDateTime fechaCreacion, 
                       LocalDateTime fechaActualizacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.visible = visible;
        this.creadorId = creadorId;
        this.tags = tags;
        this.especializacionGestor = especializacionGestor;
        this.contenidosIds = contenidosIds;
        this.fechaCreacion = fechaCreacion;
        this.fechaActualizacion = fechaActualizacion;
    }
    
    // Getters y Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public String getCreadorId() {
        return creadorId;
    }
    
    public void setCreadorId(String creadorId) {
        this.creadorId = creadorId;
    }
    
    public Set<String> getTags() {
        return tags;
    }
    
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    
    public String getEspecializacionGestor() {
        return especializacionGestor;
    }
    
    public void setEspecializacionGestor(String especializacionGestor) {
        this.especializacionGestor = especializacionGestor;
    }
    
    public List<String> getContenidosIds() {
        return contenidosIds;
    }
    
    public void setContenidosIds(List<String> contenidosIds) {
        this.contenidosIds = contenidosIds;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
