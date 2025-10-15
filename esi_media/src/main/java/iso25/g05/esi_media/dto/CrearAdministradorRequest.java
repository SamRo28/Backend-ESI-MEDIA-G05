package iso25.g05.esi_media.dto;

public class CrearAdministradorRequest {
    
    private String nombre;
    private String apellidos;
    private String email;
    private String departamento;
    private String contrasenia;
    private String rol;
    private String foto;
    
    // Constructors
    public CrearAdministradorRequest() {}
    
    public CrearAdministradorRequest(String nombre, String apellidos, String email, String departamento, String contrasenia, String rol, String foto) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.departamento = departamento;
        this.contrasenia = contrasenia;
        this.rol = rol;
        this.foto = foto;
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
    
    public String getDepartamento() {
        return departamento;
    }
    
    public void setDepartamento(String departamento) {
        this.departamento = departamento;
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
    
    public String getFoto() {
        return foto;
    }
    
    public void setFoto(String foto) {
        this.foto = foto;
    }
}