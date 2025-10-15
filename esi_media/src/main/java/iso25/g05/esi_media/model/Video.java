package iso25.g05.esi_media.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "videos")
public class Video extends Contenido {

    private String _url;
    private String _resolucion;

	public Video(String id, String titulo, String descripcion, java.util.List<String> tags, double duracion, boolean vip,
            boolean estado, java.util.Date fechaEstadoAutomatico, java.util.Date fechaDisponibleHasta,
            int edadVisualizacion, Object caratula, int nVisualizaciones, String url, String resolucion, String gestorId) {
        super(id, titulo, descripcion, tags, duracion, vip, estado, fechaEstadoAutomatico, fechaDisponibleHasta,
                edadVisualizacion, caratula, nVisualizaciones, gestorId);
        this._url = url;
        this._resolucion = resolucion;
    }    public String getUrl() {
        return _url;
    }

    public void setUrl(String u) {
        _url = u;
    }

    public String getResolucion() {
        return _resolucion;
    }

    public void setResolucion(String r) {
        _resolucion = r;
    }
}
