package iso25.g05.esi_media.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "valoraciones")
public class Valoracion {

    @Id
    private String id;

    private String visualizadorId;
    private String contenidoId;
    private Double valoracionFinal; // si está en null, significa que está visto pero no valorado

    public Valoracion() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVisualizadorId() {
        return visualizadorId;
    }

    public void setVisualizadorId(String visualizadorId) {
        this.visualizadorId = visualizadorId;
    }

    public String getContenidoId() {
        return contenidoId;
    }

    public void setContenidoId(String contenidoId) {
        this.contenidoId = contenidoId;
    }

    public Double getValoracionFinal() {
        return valoracionFinal;
    }

    public void setValoracionFinal(Double valoracionFinal) {
        this.valoracionFinal = valoracionFinal;
    }
}
