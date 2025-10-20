package iso25.g05.esi_media.model;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "videos")
public class Video extends Contenido {

    private String url;
    private String resolucion;

	public Video() { }

	public Video(String id, String titulo, String descripcion, java.util.List<String> tags, double duracion, boolean vip,
            boolean estado, java.util.Date fechaestadoautomatico, java.util.Date fechadisponiblehasta,
            int edadvisualizacion, Object caratula, int nvisualizaciones, String url, String resolucion, String gestorId) {
        super(id, titulo, descripcion, tags, duracion, vip, estado, fechaestadoautomatico, fechadisponiblehasta,
                edadvisualizacion, caratula, nvisualizaciones, gestorId);
        this.url = url;
        this.resolucion = resolucion;
    }
    
    public String geturl() {
        return url;
    }

    public void seturl(String url) {
        this.url = url;
    }

    public String getresolucion() {
        return resolucion;
    }

    public void setresolucion(String resolucion) {
        this.resolucion = resolucion;
    }
}
