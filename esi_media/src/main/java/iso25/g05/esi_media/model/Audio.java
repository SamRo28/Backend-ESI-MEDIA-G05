package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "audios")
public class Audio extends Contenido {
	private Object _fichero;

	public Audio(String id, String titulo, String descripcion, List<String> etiquetas, double duracion, boolean vip, boolean estado, Date fechaEstadoAutomatico, Date fechaDisponibleHasta, int edadVisualizacion, Object caratula, int nVisualizaciones, Object fichero) {
		super(id, titulo, descripcion, etiquetas, duracion, vip, estado, fechaEstadoAutomatico, fechaDisponibleHasta, edadVisualizacion, caratula, nVisualizaciones);
		this._fichero = fichero;
	}

	public Object get_fichero() {
		return _fichero;
	}

	public void set_fichero(Object _fichero) {
		this._fichero = _fichero;
	}

	
}