package iso25.g05.esi_media.model;
import java.util.ArrayList;
import java.util.List;


public class Lista {
	protected String id;
	private String _nombre;
	private Usuario usuario;
	public String publico;
	public List<Contenido> contenidos = new ArrayList<>();

	public Lista(String id, String _nombre, Usuario usuario, String publico, List<Contenido> contenidos) {
		this.id = id;
		this._nombre = _nombre;
		this.usuario = usuario;
		this.publico = publico;
		this.contenidos = contenidos;
	}

    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return _nombre;
    }

    public void setNombre(String _nombre) {
        this._nombre = _nombre;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getPublico() {
        return publico;
    }

    public void setPublico(String publico) {
        this.publico = publico;
    }

    public List<Contenido> getContenidos() {
        return contenidos;
    }

    public void setContenidos(List<Contenido> contenidos) {
        this.contenidos = contenidos;
    }

	
	
}