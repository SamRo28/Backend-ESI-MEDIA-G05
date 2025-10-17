package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;
import org.bson.types.Binary;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "audios")
public class Audio extends Contenido {
    private Binary fichero;        // Archivo binario real (.mp3)
    private String mimeType;       // Tipo MIME, ej. "audio/mpeg"
    private long tamanoBytes;      // Tamaño en bytes

	public Audio(String id, String titulo, String descripcion, List<String> etiquetas, double duracion, 
				boolean vip, boolean estado, Date fechaEstadoAutomatico, Date fechaDisponibleHasta, int edadVisualizacion, 
				Object caratula, int nVisualizaciones, Binary fichero, String mimeType, long tamanoBytes, String gestorId) {
		super(id, titulo, descripcion, etiquetas, duracion, vip, estado, fechaEstadoAutomatico, fechaDisponibleHasta, 
				edadVisualizacion, caratula, nVisualizaciones, gestorId);
		this.fichero = fichero;
		this.mimeType = mimeType;
		this.tamanoBytes = tamanoBytes;
	}

	public Binary getFichero() {
		return fichero;
	}

	public void setFichero(Binary fichero) {
		this.fichero = fichero;
	}

	public String getMimeType() {
		return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

	
}