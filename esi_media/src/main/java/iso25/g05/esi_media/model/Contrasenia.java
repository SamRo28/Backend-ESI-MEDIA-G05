package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "contrasenias")
public class Contrasenia {

    @Id
    protected String id;
    private Date fechaexpiracion;
    private String contraseniaactual;
    private List<String> contraseniausadas;
    
    // DECISIÓN DEL EQUIPO: Sin referencia al usuario
    // - Contrasenia NO conoce a su usuario (unidireccional)
    // - Usuario SÍ conoce su contraseña 
    // - Eliminación: primero contraseña, luego usuario

    // Constructor vacío para MongoDB
    public Contrasenia() {
        this.contraseniausadas = new ArrayList<>();
    }

    // Constructor sin referencia a usuario - Decisión del equipo
    public Contrasenia(Date fecha_expiracion, String contrasenia_actual, List<String> contrasenias_usadas) {
        this.fechaexpiracion = fecha_expiracion;
        this.contraseniaactual = contrasenia_actual;
        this.contraseniausadas = contrasenias_usadas != null ? contrasenias_usadas : new ArrayList<>();
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
