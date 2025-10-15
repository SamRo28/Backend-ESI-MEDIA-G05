package iso25.g05.esi_media.model;

public class Administrador extends Usuario {
	private String _departamento;

	// Constructor sin parámetros para MongoDB (necesario para el registro)
	public Administrador() {
		super();
	}

	/**
	 * Constructor original adaptado.
	 * 
	 * NOTA IMPORTANTE - CAMBIO RESPECTO AL CÓDIGO ORIGINAL:
	 * El constructor original incluía: Contrasenia _contrasenia
	 * 
	 * ¿POR QUÉ SE QUITÓ?
	 * - Referencias circulares: Usuario -> Contrasenia -> Usuario (bucle infinito)
	 * - Problemas de serialización JSON y StackOverflow
	 * - MongoDB no podía guardar objetos con estas referencias
	 * 
	 * SOLUCIÓN ACTUAL:
	 * - Las contraseñas se asignan por separado usando setContrasenia()
	 * - Contrasenia ahora referencia Usuario por ID, no por objeto completo
	 * - Esto mantiene la funcionalidad pero evita los problemas técnicos
	 */
	public Administrador(String _apellidos, boolean _bloqueado, String _email, Object _foto, String _nombre, String departamento) {
		super(_apellidos, _bloqueado, _email, _foto, _nombre);
		this.departamento = departamento;
	}


	private String departamento;

	public String get_departamento() {
		return departamento;
	}

	public void set_departamento(String _departamento) {
		this.departamento = _departamento;
	}

	
}