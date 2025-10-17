package iso25.g05.esi_media.model;

public class Administrador extends Usuario {
	private String departamento;

	public Administrador(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto, String nombre, String departamento) {
		super(apellidos, bloqueado, contrasenia, email, foto, nombre);
		this.departamento = departamento;
	}

	public String getDepartamento() {
		return departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	
}