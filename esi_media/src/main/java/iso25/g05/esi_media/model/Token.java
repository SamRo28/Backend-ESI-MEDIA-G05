package iso25.g05.esi_media.model;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tokens")
public class Token {

    @Id
    private String id;
    private String token;
    private Date fechaexpiracion;
    private boolean expirado;
    
    // DECISIÓN DEL EQUIPO: Sin referencia al usuario (consistente con Contrasenia)
    // - Token NO conoce a su usuario
    // - Usuario SÍ conoce sus tokens
    // - Eliminación: primero tokens, luego usuario

    // Constructor vacío para MongoDB
    public Token() {
    }

    // Constructor sin referencia a usuario - Consistente con decisión del equipo
    public Token(boolean _expirado, Date _fecha_expiracion, String _token) {
        this.expirado = _expirado;
        this.fechaexpiracion = _fecha_expiracion;
        this.token = _token;
    }

	

    public String getToken() {
        return token;
    }

    public void setToken(String t) {
        token = t;
    }

    public Date getFechaExpiracion() {
        return fechaexpiracion;
    }

    public void setFechaExpiracion(Date d) {
        fechaexpiracion = d;
    }

    public boolean isExpirado() {
        return expirado;
    }

    public void setExpirado(boolean e) {
        expirado = e;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // NOTA: Sin getters/setters de usuarioId - Decisión del equipo
    // Token es independiente y no necesita conocer a su usuario
}
