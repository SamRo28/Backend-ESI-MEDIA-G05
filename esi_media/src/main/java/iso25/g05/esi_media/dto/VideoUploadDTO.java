package iso25.g05.esi_media.dto;

import jakarta.validation.constraints.*;
import java.util.Date;
import java.util.List;

/**
 * DTO para subida de videos por URL
 * Contiene validaciones de negocio y formato de URL
 */
public class VideoUploadDTO {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 2, max = 100, message = "El título debe tener entre 2 y 100 caracteres")
    private String titulo;
    
    @Size(min = 1, max = 500, message = "La descripción debe tener entre 1 y 500 caracteres")
    private String descripcion;
    
    @NotBlank(message = "Los tags son obligatorios (al menos uno)")
    private List<String> tags;
    
    @DecimalMin(value = "0.1", message = "La duración debe ser mayor a 0.1 segundos")
    @DecimalMax(value = "14400.0", message = "La duración no puede exceder 4 horas")
    private double duracion;
    
    @NotNull(message = "Debe especificar si es contenido VIP")
    private Boolean vip;
    
    @NotNull(message = "La edad de visualización es obligatoria")
    @Min(value = 0, message = "La edad mínima no puede ser negativa")
    @Max(value = 18, message = "La edad máxima es 18")
    private int edadVisualizacion;
    
    private Date fechaDisponibleHasta;
    
    @NotNull(message = "Debe especificar si el contenido es visible")
    private Boolean visible;
    
    @NotBlank(message = "La URL del video es obligatoria")
    @Pattern(regexp = "^(https?)://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*$", 
             message = "La URL debe tener un formato válido (http/https)")
    private String url;
    
    @NotBlank(message = "La resolución es obligatoria")
    @Size(max = 50, message = "La resolución no puede exceder 50 caracteres")
    private String resolucion;
    
    private Object caratula;
    
    // Constructor por defecto
    public VideoUploadDTO() {}
    
    // Getters y Setters
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
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public double getDuracion() {
        return duracion;
    }
    
    public void setDuracion(double duracion) {
        this.duracion = duracion;
    }
    
    public Boolean getVip() {
        return vip;
    }
    
    public void setVip(Boolean vip) {
        this.vip = vip;
    }
    
    public int getEdadVisualizacion() {
        return edadVisualizacion;
    }
    
    public void setEdadVisualizacion(int edadVisualizacion) {
        this.edadVisualizacion = edadVisualizacion;
    }
    
    public Date getFechaDisponibleHasta() {
        return fechaDisponibleHasta;
    }
    
    public void setFechaDisponibleHasta(Date fechaDisponibleHasta) {
        this.fechaDisponibleHasta = fechaDisponibleHasta;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getResolucion() {
        return resolucion;
    }
    
    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }
    
    public Boolean getVisible() {
        return visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    public Object getCaratula() {
        return caratula;
    }
    
    public void setCaratula(Object caratula) {
        this.caratula = caratula;
    }
}