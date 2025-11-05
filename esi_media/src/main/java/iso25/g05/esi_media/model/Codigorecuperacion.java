package iso25.g05.esi_media.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Codigorecuperacion {
	protected String id;
	private String codigo;
	private String fechaexpiracion;
	@org.springframework.data.mongodb.core.mapping.DBRef
	public Usuario unnamedUsuario;

    // Constructor completo (original)
    public Codigorecuperacion(String id, String codigo, String fechaexpiracion, Usuario unnamedUsuario) {
        this.id = id;
        this.codigo = codigo;
        this.fechaexpiracion = fechaexpiracion;
        this.unnamedUsuario = unnamedUsuario;
    }

	public Codigorecuperacion(){}

    // Constructor simplificado para EmailService - genera código automáticamente
    public Codigorecuperacion(Usuario usuario) {
        this.id = null; // MongoDB asignará el ID
        this.codigo = generarCodigo6Digitos();
        this.fechaexpiracion = calcularFechaExpiracion();
        this.unnamedUsuario = usuario;
    }

    private String generarCodigo6Digitos() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000); // Genera número entre 100000 y 999999
        return String.valueOf(codigo);
    }

    private String calcularFechaExpiracion() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusMinutes(15); // Expira en 15 minutos
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return expiracion.format(formatter);
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