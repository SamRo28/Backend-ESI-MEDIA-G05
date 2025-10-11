package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "users")
public class Usuario {
    @Id
    private String id;

    protected String _nombre;
    protected String _apellidos;
    protected String _email;
    protected Object _foto;
    protected boolean _bloqueado;
    public List<Codigo_recuperacion> _codigos_recuperacion_ = new ArrayList<>();
    public List<Token> sesions_token_ = new ArrayList<>();
    public Contrasenia _contrasenia;
    private String secretkey;
    private boolean _2FactorAutenticationEnabled;
    private boolean _3FactorAutenticationEnabled;

    // Constructor vacío requerido por MongoDB
    public Usuario() {
        this._codigos_recuperacion_ = new ArrayList<>();
        this.sesions_token_ = new ArrayList<>();
    }

    public Usuario(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto, String _nombre) {
        this();
        this._apellidos = _apellidos;
        this._bloqueado = _bloqueado;
        this._contrasenia = _contrasenia;
        this._email = _email;
        this._foto = _foto;
        this._nombre = _nombre;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return _nombre;
    }

    public void setNombre(String n) {
        _nombre = n;
    }

    public String getApellidos() {
        return _apellidos;
    }

    public void setApellidos(String a) {
        _apellidos = a;
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String e) {
        _email = e;
    }

    public boolean isBloqueado() {
        return _bloqueado;
    }

    public void setBloqueado(boolean b) {
        _bloqueado = b;
    }
}
