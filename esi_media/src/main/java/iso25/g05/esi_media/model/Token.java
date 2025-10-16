package iso25.g05.esi_media.model;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tokens")
public class Token {

    private String token;
    private Date fechaexpiracion;
    private boolean expirado;
    public Usuario usuario;


    public Token() {
        this.token = java.util.UUID.randomUUID().toString();
        this.fechaexpiracion = new Date(System.currentTimeMillis() + 3600 * 1000);
        this.expirado = false;
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
	

    
}
