package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;

public class Contrasenia {

    protected String id;
    private Date fecha_expiracion;
    private String contrasenia_actual;
    private List<String> contrasenia_usadas;
    private Usuario usuario;

    public Contrasenia(String id, Date fecha_expiracion, String contrasenia_actual, List<String> contrasenia_usadas, Usuario usuario) {
        this.id = id;
        this.fecha_expiracion = fecha_expiracion;
        this.contrasenia_actual = contrasenia_actual;
        this.contrasenia_usadas = contrasenia_usadas;
        this.usuario = usuario;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public Date getFecha_expiracion() {
        return fecha_expiracion;
    }

    public void setFecha_expiracion(Date fecha_expiracion) {
        this.fecha_expiracion = fecha_expiracion;
    }

    public String getContrasenia_actual() {
        return contrasenia_actual;
    }

    public void setContrasenia_actual(String contrasenia_actual) {
        this.contrasenia_actual = contrasenia_actual;
    }

    public List<String> getContrasenia_usadas() {
        return contrasenia_usadas;
    }

    public void setContrasenia_usadas(List<String> contrasenia_usadas) {
        this.contrasenia_usadas = contrasenia_usadas;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
