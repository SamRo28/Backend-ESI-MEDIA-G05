package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;

public class Audio extends Contenido {
	private Object _fichero;

	public Audio(String titulo, String descripcion, List<String> etiquetas, double tamano, boolean esPublico, boolean esDescargable, Date fechaCreacion, Date fechaPublicacion, int duracion, Object _fichero, int reproducciones) {
		super(titulo, descripcion, etiquetas, tamano, esPublico, esDescargable, fechaCreacion, fechaPublicacion, duracion, _fichero, reproducciones);
		this._fichero = _fichero;
	}

	public Object get_fichero() {
		return _fichero;
	}

	public void set_fichero(Object _fichero) {
		this._fichero = _fichero;
	}

	
}