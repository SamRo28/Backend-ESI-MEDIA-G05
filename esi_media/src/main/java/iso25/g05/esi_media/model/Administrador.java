package iso25.g05.esi_media.model;

public class Administrador extends Usuario {
	private String departamento;

	// Constructor vacío requerido por MongoDB
	public Administrador() {
		super("", false, null, "", null, "");
		// MongoDB usará este constructor y luego los setters para poblar los campos
	}

	public Administrador(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email, Object foto, String nombre, String departamento) {
		super(apellidos, bloqueado, contrasenia, email, foto, nombre);
		this.departamento = departamento;
	}

    

	public String getdepartamento() {
		return departamento;
	}

	public void setdepartamento(String departamento) {
		this.departamento = departamento;
	}

	
}