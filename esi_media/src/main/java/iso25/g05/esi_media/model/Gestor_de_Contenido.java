package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;

public class Gestor_de_Contenido extends Usuario {

	private String alias;
	private String descripcion;
	private String campo_especializacion;
	private String tipo_contenido_video_o_audio;
	private List<Lista> listas_generadas;
	private List<String> contenidos_subidos = new ArrayList<>();	// Lista de IDs de contenidos subidos por el gestor

	public Gestor_de_Contenido(String apellidos, boolean bloqueado, Contrasenia contrasenia, String email,
			Object foto, String nombre) {
		super(apellidos, bloqueado, contrasenia, email, foto, nombre);
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

	public String getCampo_especializacion() {
		return campo_especializacion;
	}

	public void setCampo_especializacion(String campo_especializacion) {
		this.campo_especializacion = campo_especializacion;
	}

	public String getTipo_contenido_video_o_audio() {
		return tipo_contenido_video_o_audio;
	}

	public void setTipo_contenido_video_o_audio(String tipo_contenido_video_o_audio) {
		this.tipo_contenido_video_o_audio = tipo_contenido_video_o_audio;
	}



	public List<Lista> getListas_generadas() {
		return listas_generadas;
	}



	public void setListas_generadas(List<Lista> listas_generadas) {
		this.listas_generadas = listas_generadas;
	}



	public List<String> getContenidos_subidos() {
		return contenidos_subidos;
	}

	public void setContenidos_subidos(List<String> contenidos_subidos) {
		this.contenidos_subidos = contenidos_subidos;
	}

	//TODO: Habria que meter un add y un remove para las listas generadas y otro para los contenidos subidos

	public void subir(Contenido aC) {
		throw new UnsupportedOperationException();
	}


}