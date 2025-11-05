package iso25.g05.esi_media.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Representa un documento en la colección 'contrasenias_comunes'.
 */
@Document(collection = "contrasenias_comunes")
public class ContraseniaComun {

    @Id
    private String usadas; 

    // Constructor vacío requerido por MongoDB
    public ContraseniaComun() {
    }

    // Constructor para crear fácilmente nuevas entradas
    public ContraseniaComun(String usadas) {
        this.usadas = usadas;
    }


    public String getUsadas() {
        return usadas;
    }

    public void setUsadas(String usadas) {
        this.usadas = usadas;
    }
}