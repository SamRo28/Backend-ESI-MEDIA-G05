package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;

public class Contrasenia {

    protected String id;
    private Date fechaexpiracion;
    private String contraseniaactual;
    private List<String> contraseniausadas;

    public Contrasenia(String id, Date fechaexpiracion, String contraseniaactual, List<String> contraseniausadas) {
        this.id = id;
        this.fechaexpiracion = fechaexpiracion;
        this.contraseniaactual = contraseniaactual;
        this.contraseniausadas = contraseniausadas;

    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public Date getFechaExpiracion() {
        return fechaexpiracion;
    }

    public void setFechaExpiracion(Date d) {
        fechaexpiracion = d;
    }

    public String getContraseniaActual() {
        return contraseniaactual;
    }

    public void setContraseniaActual(String c) {
        contraseniaactual = c;
    }

    public List<String> getContraseniasUsadas() {
        return contraseniausadas;
    }

    public void setContraseniasUsadas(List<String> l) {
        contraseniausadas = l;
    }
}
