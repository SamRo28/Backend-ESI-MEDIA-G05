package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;

public class Gestor_de_Contenido extends Usuario {

	private String alias;
	private String descripcion;
	private String campo_especializacion;
	private String tipo_contenido_video_o_audio;
	public List<Lista> listas_generadas ;

	// Constructor sin parámetros para MongoDB (necesario para el registro)
	public Gestor_de_Contenido() {
		super();
		this.listas_generadas = new ArrayList<>();
	}

	/**
	 * Constructor original de tus compañeros.
	 * 
	 * NOTA SOBRE CONTRASENIA:
	 * Este constructor no incluye Contrasenia como parámetro porque:
	 * - Evita referencias circulares (Usuario -> Contrasenia -> Usuario)
	 * - Las contraseñas se asignan después de crear el usuario
	 * - Mantiene el código limpio y sin dependencias problemáticas
	 * 
	 * Si necesitas asignar una contraseña:
	 * 1. Crea el Gestor_de_Contenido con este constructor
	 * 2. Usa setContrasenia() para asignar la contraseña después
	 */
	public Gestor_de_Contenido(String apellidos, boolean bloqueado, String email,
			Object foto, String nombre) {
		super(apellidos, bloqueado, email, foto, nombre);
		this.listas_generadas = new ArrayList<>();
		this.alias = null;
		this.descripcion = null;
		this.campo_especializacion = null;
		this.tipo_contenido_video_o_audio = null;
	}

	public String getAlias() {
		return alias;
	}


	public void setAlias(String alias) {
		this.alias = alias;
	}



	public String getDescripcion() {
		return descripcion;
	}



	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}



	public String getCampoEspecializacion() {
		return campo_especializacion;
	}



	public void setCampoEspecializacion(String campo_especializacion) {
		this.campo_especializacion = campo_especializacion;
	}



	public String getTipoContenidoVideoOAudio() {
		return tipo_contenido_video_o_audio;
	}



	public void setTipoContenidoVideoOAudio(String tipo_contenido_video_o_audio) {
		this.tipo_contenido_video_o_audio = tipo_contenido_video_o_audio;
	}



	public List<Lista> getListasGeneradas() {
		return listas_generadas;
	}



	public void setListasGeneradas(List<Lista> listas_generadas) {
		this.listas_generadas = listas_generadas;
	}



	public void subir(Contenido aC) {
		throw new UnsupportedOperationException();
	}


}