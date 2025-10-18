package iso25.g05.esi_media.dto;

import java.util.Date;

/**
 * DTO para mostrar la información del perfil de un usuario
 * según su tipo (Administrador, Gestor o Visualizador).
 * Solo incluye información visible para el administrador en modo lectura.
 */
public class PerfilDTO {
    
    // Campos comunes a todos los usuarios
    private String id;
    private String nombre;
    private String apellidos;
    private String email;
    private Object foto;
    private boolean bloqueado;
    private String rol; // "Administrador", "Gestor", "Visualizador"
    private Date fechaRegistro;
    
    // Campos específicos de Administrador
    private String departamento;
    
    // Campos específicos de Gestor
    private String alias;
    private String descripcion;
    private String especialidad;
    private String tipoContenido; // "audio" o "video"
    
    // Campos específicos de Visualizador
    private Date fechaNacimiento;
    private boolean vip;
    private Integer edad; // Calculada a partir de fechaNacimiento
    
    public PerfilDTO() {
    }
    
    // Constructor para Administrador
    public PerfilDTO(String id, String nombre, String apellidos, String email, Object foto, 
                     boolean bloqueado, String departamento, Date fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.foto = foto;
        this.bloqueado = bloqueado;
        this.rol = "Administrador";
        this.departamento = departamento;
        this.fechaRegistro = fechaRegistro;
    }
    
    // Constructor para Gestor
    public PerfilDTO(String id, String nombre, String apellidos, String email, Object foto, 
                     boolean bloqueado, String alias, String descripcion, String especialidad, 
                     String tipoContenido, Date fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.foto = foto;
        this.bloqueado = bloqueado;
        this.rol = "Gestor";
        this.alias = alias;
        this.descripcion = descripcion;
        this.especialidad = especialidad;
        this.tipoContenido = tipoContenido;
        this.fechaRegistro = fechaRegistro;
    }
    
    // Constructor para Visualizador
    public PerfilDTO(String id, String nombre, String apellidos, String email, Object foto, 
                     boolean bloqueado, String alias, Date fechaNacimiento, boolean vip, 
                     Integer edad, Date fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.foto = foto;
        this.bloqueado = bloqueado;
        this.rol = "Visualizador";
        this.alias = alias;
        this.fechaNacimiento = fechaNacimiento;
        this.vip = vip;
        this.edad = edad;
        this.fechaRegistro = fechaRegistro;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public void setTipoContenido(String tipoContenido) {
        this.tipoContenido = tipoContenido;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }
}
