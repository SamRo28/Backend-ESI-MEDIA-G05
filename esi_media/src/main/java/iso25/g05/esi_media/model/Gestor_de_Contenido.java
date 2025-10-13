package iso25.g05.esi_media.model;

import java.util.ArrayList;
import java.util.List;

public class Gestor_de_Contenido extends Usuario {

	private String _alias;
	private String _descripcion;
	private String _campo_especializacion;
	private String _tipo_contenido_video_o_audio;
	public List<Lista> listas_generadas ;
	private List<String> contenidosSubidos = new ArrayList<>();	// Lista de IDs de contenidos subidos por el gestor

	public Gestor_de_Contenido(String _apellidos, boolean _bloqueado, Contrasenia _contrasenia, String _email,
			Object _foto, String _nombre) {
		super(_apellidos, _bloqueado, _contrasenia, _email, _foto, _nombre);
		this.listas_generadas = new ArrayList<>();
		this._alias = null;
		this._descripcion = null;
		this._campo_especializacion = null;
		this._tipo_contenido_video_o_audio = null;

	}

	public String get_alias() {
		return _alias;
	}


	public void set_alias(String _alias) {
		this._alias = _alias;
	}



	public String get_descripcion() {
		return _descripcion;
	}



	public void set_descripcion(String _descripcion) {
		this._descripcion = _descripcion;
	}



	public String get_campo_especializacion() {
		return _campo_especializacion;
	}



	public void set_campo_especializacion(String _campo_especializacion) {
		this._campo_especializacion = _campo_especializacion;
	}



	public String get_tipo_contenido_video_o_audio() {
		return _tipo_contenido_video_o_audio;
	}



	public void set_tipo_contenido_video_o_audio(String _tipo_contenido_video_o_audio) {
		this._tipo_contenido_video_o_audio = _tipo_contenido_video_o_audio;
	}



	public List<Lista> getListas_generadas() {
		return listas_generadas;
	}



	public void setListas_generadas(List<Lista> listas_generadas) {
		this.listas_generadas = listas_generadas;
	}



	public List<String> getContenidosSubidos() {
		return contenidosSubidos;
	}

	public void setContenidosSubidos(List<String> contenidosSubidos) {
		this.contenidosSubidos = contenidosSubidos;
	}

	//TODO: Habria que meter un add y un remove para las listas generadas y los contenidos subidos

	public void subir(Contenido aC) {
		throw new UnsupportedOperationException();
	}


}