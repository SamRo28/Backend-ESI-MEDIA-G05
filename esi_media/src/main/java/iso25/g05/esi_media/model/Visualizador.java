package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Visualizador extends Usuario {

    private String alias;
    private Date fecha_nac;
    private boolean vip;
    private List<Lista> listas_privadas = new ArrayList<>();
    private List<Contenido> contenido_fav = new ArrayList<>();

    public Visualizador(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto,
            String nombre, String alias, Date fecha_nac, boolean vip) {
        super(apellidos, bloqueado, contrasenia, email, foto, nombre);
        this.alias = alias;
        this.fecha_nac = fecha_nac;
        this.vip = vip;
        this.listas_privadas = new ArrayList<>();
        this.contenido_fav = new ArrayList<>();
    }

    public void Visualizar(Contenido aC) {
        throw new UnsupportedOperationException();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Date getFecha_nac() {
        return fecha_nac;
    }

    public void setFecha_nac(Date fecha_nac) {
        this.fecha_nac = fecha_nac;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean vip) {
        this.vip = vip;
    }

    public List<Lista> getListas_privadas() {
        return listas_privadas;
    }

    public void setListas_privadas(List<Lista> listas_privadas) {
        this.listas_privadas = listas_privadas;
    }

    public List<Contenido> getContenido_fav() {
        return contenido_fav;
    }

    public void setContenido_fav(List<Contenido> contenido_fav) {
        this.contenido_fav = contenido_fav;
    }
}
