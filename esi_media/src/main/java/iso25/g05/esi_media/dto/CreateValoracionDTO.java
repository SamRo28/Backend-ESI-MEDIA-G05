package iso25.g05.esi_media.dto;

public class CreateValoracionDTO {
    private String contenidoId;

    public CreateValoracionDTO() {
        // Constructor vacío requerido por Jackson para deserialización
        // No añadir lógica aquí; este DTO solo define el contrato del API.
    }

    public String getContenidoId() {
        return contenidoId;
    }

    public void setContenidoId(String contenidoId) {
        this.contenidoId = contenidoId;
    }
}
