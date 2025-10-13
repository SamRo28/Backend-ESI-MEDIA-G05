package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
    protected Date _fecha_registro;
    private String secretkey;
    private boolean _2FactorAutenticationEnabled;
    private boolean _3FactorAutenticationEnabled;


    public Usuario(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto, String _nombre, Date _fecha_registro) {
        this._apellidos = _apellidos;
        this._bloqueado = _bloqueado;
        this._contrasenia = _contrasenia;
        this._email = _email;
        this._foto = _foto;
        this._nombre = _nombre;
        this._fecha_registro = _fecha_registro;
    }

    // Constructor sin fecha de registro (se asigna automáticamente)
    public Usuario(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto, String _nombre) {
        this(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre, new Date());
    }

    public Contrasenia getContrasenia() {
        return _contrasenia;
    }
    public void setContrasenia(Contrasenia c) {
        _contrasenia = c;
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

    public Date getFechaRegistro() {
        return _fecha_registro;
    }

    public void setFechaRegistro(Date fecha) {
        this._fecha_registro = fecha;
    }

    public boolean is2FactorAutenticationEnabled() {
        return _2FactorAutenticationEnabled;
    }

    public void set2FactorAutenticationEnabled(boolean _2FactorAutenticationEnabled) {
        this._2FactorAutenticationEnabled = _2FactorAutenticationEnabled;
    }

    public boolean is3FactorAutenticationEnabled() {
        return _3FactorAutenticationEnabled;
    }

    public void set3FactorAutenticationEnabled(boolean _3FactorAutenticationEnabled) {
        this._3FactorAutenticationEnabled = _3FactorAutenticationEnabled;
    }
}
