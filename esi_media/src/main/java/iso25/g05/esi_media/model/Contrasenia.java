package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;

public class Contrasenia {

    private Date _fecha_expiracion;
    private String _contrasenia_actual;
    private List<String> _contrasenia_usadas;
    public Usuario _unnamed_Usuario_;

    public Date getFechaExpiracion() {
        return _fecha_expiracion;
    }

    public void setFechaExpiracion(Date d) {
        _fecha_expiracion = d;
    }

    public String getContraseniaActual() {
        return _contrasenia_actual;
    }

    public void setContraseniaActual(String c) {
        _contrasenia_actual = c;
    }

    public List<String> getContraseniasUsadas() {
        return _contrasenia_usadas;
    }

    public void setContraseniasUsadas(List<String> l) {
        _contrasenia_usadas = l;
    }
}
