package iso25.g05.esi_media.model;

public class Codigo_recuperacion {
	protected String id;
	private String codigo;
	private String fecha_expiracion;
	private Usuario usuario;

    public Codigo_recuperacion(String id, String codigo, String fecha_expiracion, Usuario usuario) {
        this.id = id;
        this.codigo = codigo;
        this.fecha_expiracion = fecha_expiracion;
        this.usuario = usuario;
    }

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getCodigo() {
		return codigo;
	}
	
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}
	
	public String getFecha_expiracion() {
		return fecha_expiracion;
	}
	
	public void setFecha_expiracion(String fecha_expiracion) {
		this.fecha_expiracion = fecha_expiracion;
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	
}