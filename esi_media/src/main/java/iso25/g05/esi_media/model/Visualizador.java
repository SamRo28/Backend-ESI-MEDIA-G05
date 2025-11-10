package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Clase Visualizador que extiende Usuario.
 * 
 * HERENCIA EN MONGODB:
 * - Se guarda en la colección "users" junto con otros tipos de usuario
 * - MongoDB automáticamente añade un campo "_class" para identificar el tipo
 * - Permite consultas polimórficas: buscar todos los usuarios o solo visualizadores
 * 
 * CAMPOS ESPECÍFICOS DEL VISUALIZADOR:
 * - alias: nombre de usuario público
 * - fecha_nac: fecha de nacimiento para validaciones de edad
 * - vip: indica si tiene privilegios premium
 * - listas_privadas: colecciones personales de contenido
 * - contenido_fav: contenido marcado como favorito
 * 
 * NO necesita @Document porque Usuario ya lo tiene.
 * Spring Data MongoDB usará el discriminador _class para identificar el tipo.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Visualizador extends Usuario {

    private String alias;
    
    @JsonProperty("fechanac")
    private Date fechanac;
    private boolean vip;
    public List<Lista> listasprivadas = new ArrayList<>();
    public List<Contenido> contenidofav = new ArrayList<>();

    // Constructor vacío requerido por MongoDB
    public Visualizador() {
        super();
        this.listasprivadas = new ArrayList<>();
        this.contenidofav = new ArrayList<>();
    }

    public Visualizador(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto,
            String nombre, String alias, Date fechaNac, boolean vip) {
        super(apellidos, bloqueado, contrasenia, email, foto, nombre);
        this.alias = alias;
        this.fechanac = fechaNac;
        this.vip = vip;
        this.listasprivadas = new ArrayList<>();
        this.contenidofav = new ArrayList<>();
    }

    public void Visualizar(Contenido aC) {
        throw new UnsupportedOperationException();
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String a) {
        alias = a;
    }

    public Date getFechaNac() {
        return fechanac;
    }

    public void setFechaNac(Date d) {
        fechanac = d;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean v) {
        vip = v;
    }
}
