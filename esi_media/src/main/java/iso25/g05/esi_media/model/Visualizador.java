package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Visualizador extends Usuario {

    private String alias;
    private Date fechanac;
    private boolean vip;
    public List<Lista> listasprivadas = new ArrayList<>();
    public List<Contenido> contenidofav = new ArrayList<>();

    // Constructor vacío requerido por MongoDB
    public Visualizador() {
        super();
        this.listasprivadas = new ArrayList<>();
        this.contenidofav = new ArrayList<>();
    }

    public Visualizador(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto,
            String nombre, String alias, Date fechaNac, boolean vip) {
        super(apellidos, bloqueado, contrasenia, email, foto, nombre);
        this.alias = alias;
        this.fechanac = fechaNac;
        this.vip = vip;
        this.listasprivadas = new ArrayList<>();
        this.contenidofav = new ArrayList<>();
    }

    public void Visualizar(Contenido aC) {
        throw new UnsupportedOperationException();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String a) {
        alias = a;
    }

    public Date getFechaNac() {
        return fechanac;
    }

    public void setFechaNac(Date d) {
        fechanac = d;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean v) {
        vip = v;
    }
}
