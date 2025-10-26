package iso25.g05.esi_media.model;

import java.util.Date;
import java.util.List;
import org.bson.types.Binary;

public class Audio extends Contenido {
    private Binary fichero;        // Archivo binario real (.mp3)
    private String mimeType;       // Tipo MIME, debe ser "audio/mpeg"
    private long tamanoBytes;      // Tamaño en bytes

	public Audio() { }

	public Audio(String id, String titulo, String descripcion, List<String> tags, double duracion, 
				boolean vip, boolean estado, Date fechaestadoautomatico, Date fechadisponiblehasta, int edadvisualizacion, 
				Object caratula, int nvisualizaciones, Binary fichero, String mimeType, long tamanoBytes, String gestorId) {
		super(id, titulo, descripcion, tags, duracion, vip, estado, fechaestadoautomatico, fechadisponiblehasta, 
				edadvisualizacion, caratula, nvisualizaciones, gestorId);
		this.fichero = fichero;
		this.mimeType = mimeType;
		this.tamanoBytes = tamanoBytes;
	}

	public Binary getfichero() {
		return fichero;
	}

	public void setfichero(Binary fichero) {

		this.fichero = fichero;
	}

	public String getmimeType() {
		return mimeType;
    }

    public void setmimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long gettamanoBytes() {
        return tamanoBytes;
    }

    public void settamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

	
}
