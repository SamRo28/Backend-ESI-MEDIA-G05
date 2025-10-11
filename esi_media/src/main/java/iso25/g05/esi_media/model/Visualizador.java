package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class Visualizador extends Usuario {

    private String _alias;
    private Date _fecha_nac;
    private boolean _vip;
    public List<Lista> listas_privadas = new ArrayList<>();
    public List<Contenido> contenido_fav = new ArrayList<>();

    // Constructor vacío requerido por MongoDB
    public Visualizador() {
        super();
        this.listas_privadas = new ArrayList<>();
        this.contenido_fav = new ArrayList<>();
    }

    public Visualizador(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto,
            String _nombre, String alias, Date fechaNac, boolean vip) {
        super(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre);
        this._alias = alias;
        this._fecha_nac = fechaNac;
        this._vip = vip;
        this.listas_privadas = new ArrayList<>();
        this.contenido_fav = new ArrayList<>();
    }

    public void Visualizar(Contenido aC) {
        throw new UnsupportedOperationException();
    }

    public String getAlias() {
        return _alias;
    }

    public void setAlias(String a) {
        _alias = a;
    }

    public Date getFechaNac() {
        return _fecha_nac;
    }

    public void setFechaNac(Date d) {
        _fecha_nac = d;
    }

    public boolean isVip() {
        return _vip;
    }

    public void setVip(boolean v) {
        _vip = v;
    }
}
