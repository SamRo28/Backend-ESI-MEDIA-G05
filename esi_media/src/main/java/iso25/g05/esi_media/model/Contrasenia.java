package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "contrasenias")
public class Contrasenia {

    @Id
    protected String id;
    
    @Field("fecha_expiracion")
    private Date fechaexpiracion;
    
    @Field("contrasenia_actual")
    private String contraseniaactual;
    
    @Field("contrasenia_usadas")
    private List<String> contraseniausadas;

    // Constructor vacío requerido por MongoDB
    public Contrasenia() {
    }

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
