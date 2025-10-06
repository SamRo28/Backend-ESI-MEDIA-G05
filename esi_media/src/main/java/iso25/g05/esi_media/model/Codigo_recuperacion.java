package iso25.g05.esi_media.model;

public class Codigo_recuperacion {
	private String _codigo;
	private String _fecha_expiracion;
	public Usuario _unnamed_Usuario_;

    public Codigo_recuperacion(String _codigo, String _fecha_expiracion, Usuario _unnamed_Usuario_) {
        this._codigo = _codigo;
        this._fecha_expiracion = _fecha_expiracion;
        this._unnamed_Usuario_ = _unnamed_Usuario_;
    }

	public String get_codigo() {
		return _codigo;
	}
	public void set_codigo(String _codigo) {
		this._codigo = _codigo;
	}
	public String get_fecha_expiracion() {
		return _fecha_expiracion;
	}
	public void set_fecha_expiracion(String _fecha_expiracion) {
		this._fecha_expiracion = _fecha_expiracion;
	}
	public Usuario get_unnamed_Usuario_() {
		return _unnamed_Usuario_;
	}
	public void set_unnamed_Usuario_(Usuario _unnamed_Usuario_) {
		this._unnamed_Usuario_ = _unnamed_Usuario_;
	}

	
}