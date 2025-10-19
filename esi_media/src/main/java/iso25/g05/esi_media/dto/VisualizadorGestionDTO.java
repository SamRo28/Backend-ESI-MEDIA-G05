package iso25.g05.esi_media.dto;

import jakarta.validation.constraints.*;
import java.util.Date;

/**
 * DTO para gestión de visualizadores por administradores
 * Incluye solo los campos que el administrador puede ver y modificar
 */
public class VisualizadorGestionDTO {
    
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
    
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    private Date fechanac;
    
    // Solo lectura - controlado por otra funcionalidad
    private boolean vip;
    
    // Constructor vacío
    public VisualizadorGestionDTO() {}
    
    // Constructor completo
    public VisualizadorGestionDTO(String id, String nombre, String apellidos, String email, 
                                 Object foto, boolean bloqueado, Date fecharegistro, 
                                 String alias, Date fechanac, boolean vip) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.foto = foto;
        this.bloqueado = bloqueado;
        this.fecharegistro = fecharegistro;
        this.alias = alias;
        this.fechanac = fechanac;
        this.vip = vip;
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
    
    public String getAlias() {
        return alias;
    }
    
    public void setAlias(String alias) {
        this.alias = alias;
    }
    
    public Date getFechanac() {
        return fechanac;
    }
    
    public void setFechanac(Date fechanac) {
        this.fechanac = fechanac;
    }
    
    public boolean isVip() {
        return vip;
    }
    
    public void setVip(boolean vip) {
        this.vip = vip;
    }
}