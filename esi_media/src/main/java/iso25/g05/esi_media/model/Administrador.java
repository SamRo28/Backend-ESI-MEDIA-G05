package iso25.g05.esi_media.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Administrador - hereda de Usuario
 * NO necesita @Document porque Usuario ya lo tiene
 * Spring Data MongoDB usará el discriminador _class para identificar el tipo
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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

	// Constructor vacío requerido por MongoDB
	public Administrador() {
		super("", false, null, "", null, "");
	}
	
}