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

    public Visualizador(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto,
            String _nombre, String alias, Date fechaNac, boolean vip) {
        super(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre);
        this._alias = alias;
        this._fecha_nac = fechaNac;
        this._vip = vip;
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
        return _fecha_nac;
    }

    public void setFechaNac(Date d) {
        _fecha_nac = d;
    }

    public boolean isVip() {
        return vip;
    }

    public void setVip(boolean v) {
        _vip = v;
    }
}
