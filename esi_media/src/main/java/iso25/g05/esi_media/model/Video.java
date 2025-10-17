package iso25.g05.esi_media.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "videos")
public class Video extends Contenido {

    private String url;
    private String resolucion;

	public Video(String id, String titulo, String descripcion, java.util.List<String> tags, double duracion, boolean vip,
            boolean estado, java.util.Date fechaEstadoAutomatico, java.util.Date fechaDisponibleHasta,
            int edadVisualizacion, Object caratula, int nVisualizaciones, String url, String resolucion, String gestorId) {
        super(id, titulo, descripcion, tags, duracion, vip, estado, fechaEstadoAutomatico, fechaDisponibleHasta,
                edadVisualizacion, caratula, nVisualizaciones, gestorId);
        this.url = url;
        this.resolucion = resolucion;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getResolucion() {
        return resolucion;
    }

    public void setResolucion(String resolucion) {
        this.resolucion = resolucion;
    }
}
