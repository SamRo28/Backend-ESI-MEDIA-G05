package iso25.g05.esi_media.model;

import java.util.Date;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tokens")
public class Token {

    private String token;
    private Date fechaexpiracion;
    private boolean expirado;
<<<<<<< HEAD



=======
    public Usuario usuario;


>>>>>>> alvaro
    public Token() {
        this.token = java.util.UUID.randomUUID().toString();
        this.fechaexpiracion = new Date(System.currentTimeMillis() + 3600 * 1000);
        this.expirado = false;
    }
<<<<<<< HEAD



    public String getToken() {
        return token;
        return token;
    }



    public void setToken(String token) {
        this.token = token;
    }



    public Date getFechaexpiracion() {
        return fechaexpiracion;
    }



    public void setFechaexpiracion(Date fechaexpiracion) {
        this.fechaexpiracion = fechaexpiracion;
=======
	

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
>>>>>>> alvaro
    }



    public boolean isExpirado() {
        return expirado;
<<<<<<< HEAD
        return expirado;
    }



    public void setExpirado(boolean expirado) {
        this.expirado = expirado;
=======
    }

    public void setExpirado(boolean e) {
        expirado = e;
>>>>>>> alvaro
    }
	

    
}
