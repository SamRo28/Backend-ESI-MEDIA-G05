package iso25.g05.esi_media.dto;

public class ValorarDTO {
    private Double valoracion;

    public ValorarDTO() {
        // Constructor vacío requerido por Jackson para deserialización
        // No añadir lógica aquí; este DTO sólo encapsula la puntuación enviada por el cliente.
    }

    public Double getValoracion() {
        return valoracion;
    }

    public void setValoracion(Double valoracion) {
        this.valoracion = valoracion;
    }
}
