package iso25.g05.esi_media.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "codigos_recuperacion")
public class Codigo_recuperacion {
	@Id
	protected String id;
	private String codigo;
	private String fecha_expiracion;
	
	// DECISIÓN DEL EQUIPO: Sin referencia al usuario (consistente con Contrasenia y Token)
	// - Codigo_recuperacion NO conoce a su usuario
	// - Usuario SÍ conoce sus códigos de recuperación
	// - Eliminación: primero códigos, luego usuario

    // Constructor vacío para MongoDB
    public Codigo_recuperacion() {
    }

    // Constructor sin referencia a usuario - Consistente con decisión del equipo
	public Codigo_recuperacion(String codigo, String fecha_expiracion) {
		this.codigo = codigo;
		this.fecha_expiracion = fecha_expiracion;
    }

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String get_codigo() {
		return codigo;
	}
	public void set_codigo(String _codigo) {
		this.codigo = _codigo;
	}
	public String get_fecha_expiracion() {
		return fecha_expiracion;
	}
	public void set_fecha_expiracion(String _fecha_expiracion) {
		this.fecha_expiracion = _fecha_expiracion;
	}
	// DECISIÓN DEL EQUIPO: Métodos getUsuarioId/setUsuarioId eliminados
	// Motivo: Codigo_recuperacion no debe conocer al usuario directamente
	// El Usuario mantiene la lista de códigos con @DBRef(lazy = true)
	// Para operaciones que requieran el usuario, consultar desde Usuario por ID de código

	
}