package iso25.g05.esi_media.model;

import java.util.Date;

public class Token {

    private String _token;
    private Date _fecha_expiracion;
    private boolean _expirado;
    public Usuario _usuario;

    public Token(boolean _expirado, Date _fecha_expiracion, String _token, Usuario _usuario) {
        this._expirado = _expirado;
        this._fecha_expiracion = _fecha_expiracion;
        this._token = _token;
        this._usuario = _usuario;
    }

	

    public String getToken() {
        return _token;
    }

    public void setToken(String t) {
        _token = t;
    }

    public Date getFechaExpiracion() {
        return _fecha_expiracion;
    }

    public void setFechaExpiracion(Date d) {
        _fecha_expiracion = d;
    }

    public boolean isExpirado() {
        return _expirado;
    }

    public void setExpirado(boolean e) {
        _expirado = e;
    }
}
