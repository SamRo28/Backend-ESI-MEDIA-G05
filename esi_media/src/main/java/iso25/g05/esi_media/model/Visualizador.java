package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
 */
public class Visualizador extends Usuario {

    private String alias;
    private Date fecha_nac;
    private boolean vip;
    public List<Lista> listas_privadas = new ArrayList<>();
    public List<Contenido> contenido_fav = new ArrayList<>();

    /**
     * Constructor vacío requerido por Spring Data MongoDB
     * Spring Data necesita poder crear instancias sin parámetros para mapear desde la BD
     */
    public Visualizador() {
        super();
        this.listas_privadas = new ArrayList<>();
        this.contenido_fav = new ArrayList<>();
    }

    /**
     * Constructor completo para crear nuevos visualizadores (temporal sin Contrasenia)
     */
    public Visualizador(String _apellidos, boolean _bloqueado, String _email, Object _foto,
            String _nombre, String alias, Date fechaNac, boolean vip) {
        super(_apellidos, _bloqueado, _email, _foto, _nombre);
        this.alias = alias;
        this.fecha_nac = fechaNac;
        this.vip = vip;
        this.listas_privadas = new ArrayList<>();
        this.contenido_fav = new ArrayList<>();
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
        return fecha_nac;
    }

    public void setFechaNac(Date d) {
        fecha_nac = d;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean v) {
        vip = v;
    }
    
    // === GETTERS/SETTERS PARA LISTAS ===
    
    public List<Lista> getListasPrivadas() {
        return listas_privadas;
    }
    
    public void setListasPrivadas(List<Lista> listas) {
        this.listas_privadas = listas != null ? listas : new ArrayList<>();
    }
    
    public List<Contenido> getContenidoFav() {
        return contenido_fav;
    }
    
    public void setContenidoFav(List<Contenido> contenido) {
        this.contenido_fav = contenido != null ? contenido : new ArrayList<>();
    }
}
