package iso25.g05.esi_media.dto;

import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualización de contenidos por parte del Gestor.
 * Incluye solo los campos editables desde la vista de gestión.
 */
public class ContenidoUpdateDTO {

    @NotBlank(message = "El título es obligatorio")
    @Size(min = 2, max = 100, message = "El título debe tener entre 2 y 100 caracteres")
    private String titulo;

    @Size(min = 1, max = 500, message = "La descripción debe tener entre 1 y 500 caracteres")
    private String descripcion;

    @NotEmpty(message = "Debe indicar al menos un tag")
    private List<@Size(max = 30, message = "El tag no puede exceder 30 caracteres") String> tags;

    @NotNull(message = "Debe indicar si es contenido VIP")
    private Boolean vip;

    @NotNull(message = "Debe indicar si el contenido es visible")
    private Boolean estado; // true = visible, false = no visible

    @Min(value = 0, message = "La edad mínima no puede ser negativa")
    @Max(value = 18, message = "La edad máxima es 18")
    private Integer edadVisualizacion;

    private Date fechaDisponibleHasta;

    private Object caratula;

    public ContenidoUpdateDTO() {
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Boolean getVip() {
        return vip;
    }

    public void setVip(Boolean vip) {
        this.vip = vip;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Integer getEdadVisualizacion() {
        return edadVisualizacion;
    }

    public void setEdadVisualizacion(Integer edadVisualizacion) {
        this.edadVisualizacion = edadVisualizacion;
    }

    public Date getFechaDisponibleHasta() {
        return fechaDisponibleHasta;
    }

    public void setFechaDisponibleHasta(Date fechaDisponibleHasta) {
        this.fechaDisponibleHasta = fechaDisponibleHasta;
    }

    public Object getCaratula() {
        return caratula;
    }

    public void setCaratula(Object caratula) {
        this.caratula = caratula;
    }
}

