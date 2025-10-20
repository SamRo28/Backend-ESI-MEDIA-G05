package iso25.g05.esi_media.dto;

import jakarta.validation.constraints.*;
import java.util.Date;

/**
 * DTO para gestión de gestores de contenido por administradores
 * Incluye solo los campos que el administrador puede ver y modificar
 */
public class GestorGestionDTO {
    
    private String id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    private String nombre;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;
    
    // Solo lectura - no se puede modificar
    @Email(message = "El email debe tener un formato válido")
    private String email;
    
    private Object foto;
    
    // Solo lectura - controlado por otra funcionalidad
    private boolean bloqueado;
    
    // Solo lectura
    private Date fecharegistro;
    
    @NotBlank(message = "El alias es obligatorio")
    @Size(min = 3, max = 30, message = "El alias debe tener entre 3 y 30 caracteres")
    private String alias;
    
    @Size(max = 100, message = "La especialidad no puede exceder 100 caracteres")
    private String campoespecializacion;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
    
    // Solo lectura - no se puede modificar
    private String tipocontenidovideooaudio;
    
    // Constructor vacío
    public GestorGestionDTO() {}
    
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
    
    public String getApellidos() {
        return apellidos;
    }
    
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Object getFoto() {
        return foto;
    }
    
    public void setFoto(Object foto) {
        this.foto = foto;
    }
    
    public boolean isBloqueado() {
        return bloqueado;
    }
    
    public void setBloqueado(boolean bloqueado) {
        this.bloqueado = bloqueado;
    }
    
    public Date getFecharegistro() {
        return fecharegistro;
    }
    
    public void setFecharegistro(Date fecharegistro) {
        this.fecharegistro = fecharegistro;
    }
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public String getCampoespecializacion() {
        return campoespecializacion;
    }
    
    public void setCampoespecializacion(String campoespecializacion) {
        this.campoespecializacion = campoespecializacion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getTipocontenidovideooaudio() {
        return tipocontenidovideooaudio;
    }
    
    public void setTipocontenidovideooaudio(String tipocontenidovideooaudio) {
        this.tipocontenidovideooaudio = tipocontenidovideooaudio;
    }
}