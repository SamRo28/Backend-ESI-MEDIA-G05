package iso25.g05.esi_media.model;

public class Administrador extends Usuario {
	private String departamento;

	public Administrador(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email, Object _foto, String _nombre, String departamento) {
		super(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre);
		this.departamento = departamento;
	}

    

	public String get_departamento() {
		return departamento;
	}

	public void set_departamento(String _departamento) {
		this.departamento = _departamento;
	}

	
}