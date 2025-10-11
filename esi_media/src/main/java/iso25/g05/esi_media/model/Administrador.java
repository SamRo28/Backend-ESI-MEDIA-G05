package iso25.g05.esi_media.model;

import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "users")
public class Administrador extends Usuario {
	public enum TipoAdministrador {
		SUPER_ADMINISTRADOR, ADMINISTRADOR
	}
	
	private TipoAdministrador _tipo_administrador;
	private String _departamento;

	// Constructor vacío requerido por MongoDB
	public Administrador() {
		super();
		this._tipo_administrador = TipoAdministrador.ADMINISTRADOR;
	}

	public Administrador(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto, String _nombre, String departamento, TipoAdministrador tipoAdministrador) {
		super(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre);
		this._departamento = departamento;
		this._tipo_administrador = tipoAdministrador;
	}
	
	// Constructor por defecto para administrador normal
	public Administrador(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto, String _nombre, String departamento) {
		this(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre, departamento, TipoAdministrador.ADMINISTRADOR);
	}

    

	public String get_departamento() {
		return _departamento;
	}

	public void set_departamento(String _departamento) {
		this._departamento = _departamento;
	}
	
	public TipoAdministrador getTipoAdministrador() {
		return _tipo_administrador;
	}
	
	public void setTipoAdministrador(TipoAdministrador tipoAdministrador) {
		this._tipo_administrador = tipoAdministrador;
	}
	
	public boolean esSuperAdministrador() {
		return _tipo_administrador == TipoAdministrador.SUPER_ADMINISTRADOR;
	}

	
}