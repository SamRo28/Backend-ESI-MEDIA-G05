package iso25.g05.esi_media.model;

<<<<<<< HEAD
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class Codigorecuperacion {
	protected String id;
	private String codigo;
	private Date fechaexpiracion;
	public Usuario unnamedUsuario;

    public Codigorecuperacion(Usuario unnamedUsuario) {
        this.id = UUID.randomUUID().toString();
        this.codigo = ""+generarNumeroAleatorio6Cifras();
        this.fechaexpiracion = new Date(System.currentTimeMillis() + 1 * 60 * 1000); // 1 minuto desde ahora
=======
public class Codigorecuperacion {
	protected String id;
	private String codigo;
	private String fechaexpiracion;
	public Usuario unnamedUsuario;

    public Codigorecuperacion(String id, String codigo, String fechaexpiracion, Usuario unnamedUsuario) {
        this.id = id;
        this.codigo = codigo;
        this.fechaexpiracion = fechaexpiracion;
>>>>>>> alvaro
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
<<<<<<< HEAD
	public Date getfechaexpiracion() {
		return fechaexpiracion;
	}
	public void setfechaexpiracion(Date fechaexpiracion) {
=======
	public String getfechaexpiracion() {
		return fechaexpiracion;
	}
	public void setfechaexpiracion(String fechaexpiracion) {
>>>>>>> alvaro
		this.fechaexpiracion = fechaexpiracion;
	}
	public Usuario getunnamedUsuario() {
		return unnamedUsuario;
	}
	public void setunnamedUsuario(Usuario unnamedUsuario) {
		this.unnamedUsuario = unnamedUsuario;
	}

<<<<<<< HEAD
	private int generarNumeroAleatorio6Cifras() {
        Random random = new Random();
        return 100000 + random.nextInt(900000);
    }

=======
>>>>>>> alvaro
	
}