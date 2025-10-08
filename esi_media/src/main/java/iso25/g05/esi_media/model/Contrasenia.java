package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;

public class Contrasenia {

    protected String id;
    private Date _fecha_expiracion;
    private String _contrasenia_actual;
    private List<String> _contrasenia_usadas;
    public Usuario _unnamed_Usuario_;

    public Contrasenia(String id, Date _fecha_expiracion, String _contrasenia_actual, List<String> _contrasenia_usadas, Usuario _unnamed_Usuario_) {
        this.id = id;
        this._fecha_expiracion = _fecha_expiracion;
        this._contrasenia_actual = _contrasenia_actual;
        this._contrasenia_usadas = _contrasenia_usadas;
        this._unnamed_Usuario_ = _unnamed_Usuario_;
    }

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public Date getFechaExpiracion() {
        return _fecha_expiracion;
    }

    public void setFechaExpiracion(Date d) {
        _fecha_expiracion = d;
    }

    public String getContraseniaActual() {
        return _contrasenia_actual;
    }

    public void setContraseniaActual(String c) {
        _contrasenia_actual = c;
    }

    public List<String> getContraseniasUsadas() {
        return _contrasenia_usadas;
    }

    public void setContraseniasUsadas(List<String> l) {
        _contrasenia_usadas = l;
    }
    
    /**
     * Getter para el usuario dueño de esta contraseña
     */
    public Usuario get_unnamed_Usuario_() {
        return _unnamed_Usuario_;
    }
    
    /**
     * Setter para establecer el usuario dueño de esta contraseña
     * 
     * Crear relación bidireccional entre Usuario y Contraseña
     * - El Usuario conoce su Contraseña (a través de su campo _contrasenia)
     * - La Contraseña conoce su Usuario (a través de este campo _unnamed_Usuario_)
     * 
     * ¿Por qué es importante?
     * - Auditoría: La contraseña sabe a quién pertenece
     * - Validaciones: Podemos verificar consistencia de datos
     * - Navegación: Desde una contraseña podemos llegar al usuario
     */
    public void set_unnamed_Usuario_(Usuario _unnamed_Usuario_) {
        this._unnamed_Usuario_ = _unnamed_Usuario_;
    }
}
