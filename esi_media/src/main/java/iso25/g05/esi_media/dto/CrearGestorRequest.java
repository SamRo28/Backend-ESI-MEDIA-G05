package iso25.g05.esi_media.dto;

public class CrearGestorRequest {
    
    private String nombre;
    private String apellidos;
    private String email;
    private String contrasenia;
    private String rol;
    private String alias;
    private String descripcion;
    private String especialidad;
    private String tipoContenido;
    
    // Constructors
    public CrearGestorRequest() {}
    
    public CrearGestorRequest(String nombre, String apellidos, String email, String contrasenia, 
                            String rol, String alias, String descripcion, String especialidad, String tipoContenido) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.alias = alias;
        this.descripcion = descripcion;
        this.especialidad = especialidad;
        this.tipoContenido = tipoContenido;
    }
    
    // Getters and Setters
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
    
    public String getContrasenia() {
        return contrasenia;
    }
    
    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
    
    public String getRol() {
        return rol;
    }
    
    public void setRol(String rol) {
        this.rol = rol;
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
    
    @Override
    public String toString() {
        return "CrearGestorRequest{" +
                "nombre='" + nombre + '\'' +
                ", apellidos='" + apellidos + '\'' +
                ", email='" + email + '\'' +
                ", rol='" + rol + '\'' +
                ", alias='" + alias + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", tipoContenido='" + tipoContenido + '\'' +
                '}';
    }
}