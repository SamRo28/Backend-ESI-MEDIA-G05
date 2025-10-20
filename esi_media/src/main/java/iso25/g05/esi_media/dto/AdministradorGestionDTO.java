package iso25.g05.esi_media.dto;

import jakarta.validation.constraints.*;
import java.util.Date;

/**
 * DTO para gestión de administradores por otros administradores
 * Incluye solo los campos que un administrador puede ver y modificar de otros administradores
 */
public class AdministradorGestionDTO {
    
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
    
    @NotBlank(message = "El departamento es obligatorio")
    @Size(min = 2, max = 100, message = "El departamento debe tener entre 2 y 100 caracteres")
    private String departamento;
    
    // Constructor vacío
    public AdministradorGestionDTO() {
        // Constructor requerido para Jackson
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
    
    public String getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }
}