package iso25.g05.esi_media.model;

import java.util.Date;

public class Token {

    private String _token;
    private Date _fecha_expiracion;
    private boolean _expirado;
    public Usuario _usuario;


    public Token() {
        this._token = java.util.UUID.randomUUID().toString();
        this._fecha_expiracion = new Date(System.currentTimeMillis() + 3600 * 1000);
        this._expirado = false;
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
