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
    
    /**
     * NOTA IMPORTANTE SOBRE EL DISEÑO:
     * 
     * DECISIÓN DEL EQUIPO - MODELO UNIDIRECCIONAL:
     * - Contrasenia NO conoce a su usuario (sin usuarioId)
     * - Usuario SÍ conoce su contraseña (relación uno a uno)
     * - Eliminación controlada: primero contraseña, luego usuario
     * 
     * VENTAJAS DE ESTE DISEÑO:
     * - Máxima simplicidad: Contrasenia es solo un valor/objeto
     * - Sin referencias circulares en absoluto
     * - Modelo conceptualmente más claro
     * - Lógica de eliminación controlada por el servicio
     * 
     * RESPONSABILIDADES:
     * - Usuario: Conoce y gestiona su contraseña
     * - Contrasenia: Solo almacena datos de autenticación
     * - Servicio: Gestiona la relación y el ciclo de vida
     */
}
