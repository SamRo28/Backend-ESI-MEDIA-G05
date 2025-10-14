package iso25.g05.esi_media.model;

public class Codigorecuperacion {
	protected String id;
	private String codigo;
	private String fechaexpiracion;
	public Usuario unnamedUsuario;

    public Codigorecuperacion(String id, String codigo, String fechaexpiracion, Usuario unnamedUsuario) {
        this.id = id;
        this.codigo = codigo;
        this.fechaexpiracion = fechaexpiracion;
        this.unnamedUsuario = unnamedUsuario;
    }

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getcodigo() {
		return codigo;
	}
	public void setcodigo(String codigo) {
		this.codigo = codigo;
	}
	public String getfechaexpiracion() {
		return fechaexpiracion;
	}
	public void setfechaexpiracion(String fechaexpiracion) {
		this.fechaexpiracion = fechaexpiracion;
	}
	public Usuario getunnamedUsuario() {
		return unnamedUsuario;
	}
	public void setunnamedUsuario(Usuario unnamedUsuario) {
		this.unnamedUsuario = unnamedUsuario;
	}

	
}