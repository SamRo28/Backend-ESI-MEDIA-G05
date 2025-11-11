package iso25.g05.esi_media.model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@Document(collection = "listas")
public class Lista {
    @Id
	protected String id;
	private String nombre;
    private String descripcion;
    private boolean visible;
    private String creadorId;
    private Set<String> tags = new HashSet<>();
    private String especializacionGestor;
        	
	@DBRef
	private Set<Contenido> contenidos = new HashSet<>();

	private LocalDateTime fechaCreacion;
	
	private LocalDateTime fechaActualizacion;
	
	@DBRef
	@JsonIgnoreProperties({"listasgeneradas", "contrasenia", "codigosrecuperacion", "sesionstoken"})
	private Usuario usuario;
	private String publico;

    public Lista() {
		this.contenidos = new HashSet<>();
		this.tags = new HashSet<>();
		this.fechaCreacion = LocalDateTime.now();
		this.fechaActualizacion = LocalDateTime.now();
	}


	public Lista(String id, String nombre, Usuario usuario, String publico, List<Contenido> contenidos) {
		this.id = id;
		this.nombre = nombre;
		this.usuario = usuario;
		this.publico = publico;
		if (contenidos != null) {
			this.contenidos = new HashSet<>(contenidos);
		} else {
			this.contenidos = new HashSet<>();
		}
		this.tags = new HashSet<>();
		this.fechaCreacion = LocalDateTime.now();
		this.fechaActualizacion = LocalDateTime.now();
		// Inicializar creadorId desde usuario si está disponible
		if (usuario != null) {
			this.creadorId = usuario.getId();
		}
	}
	// ==================== MÉTODOS UTILITARIOS ====================
	
	/**
	 * Añade un contenido a la lista
	 * Respeta las reglas de negocio:
	 * - No permite duplicados
	 * - Actualiza la fecha de modificación
	 * 
	 * @param contenido Contenido a añadir
	 * @return true si se añadió, false si ya existía
	 * @throws IllegalArgumentException si el contenido es null
	 */
	public boolean addContenido(Contenido contenido) {
		if (contenido == null) {
			throw new IllegalArgumentException("El contenido no puede ser null");
		}
		
		// Verificar si ya existe (por ID)
		boolean yaExiste = this.contenidos.stream()
			.anyMatch(c -> c.getId().equals(contenido.getId()));
		
		if (yaExiste) {
			return false; // No se añadió porque ya existe
		}
		
		boolean added = this.contenidos.add(contenido);
		if (added) {
			this.fechaActualizacion = LocalDateTime.now();
		}
		return added;
	}
	
	/**
	 * Elimina un contenido de la lista por su ID
	 * Respeta las reglas de negocio:
	 * - No permite eliminar si quedaría la lista vacía (mínimo 1 contenido)
	 * - Actualiza la fecha de modificación
	 * 
	 * @param contenidoId ID del contenido a eliminar
	 * @return true si se eliminó, false si no se encontró
	 * @throws IllegalStateException si la eliminación dejaría la lista vacía
	 */
	public boolean removeContenido(String contenidoId) {
		if (contenidoId == null || contenidoId.isEmpty()) {
			throw new IllegalArgumentException("El ID del contenido no puede ser null o vacío");
		}
		
		// Verificar que no quedaría vacía
		if (this.contenidos.size() <= 1) {
			throw new IllegalStateException(
				"No se puede eliminar el contenido. La lista debe tener al menos 1 contenido"
			);
		}
		
		boolean removed = this.contenidos.removeIf(c -> c.getId().equals(contenidoId));
		if (removed) {
			this.fechaActualizacion = LocalDateTime.now();
		}
		return removed;
	}
	
	/**
	 * Verifica si la lista contiene un contenido específico
	 * 
	 * @param contenidoId ID del contenido a buscar
	 * @return true si el contenido está en la lista
	 */
	public boolean contieneContenido(String contenidoId) {
		return this.contenidos.stream()
			.anyMatch(c -> c.getId().equals(contenidoId));
	}
	
	/**
	 * Obtiene el número de contenidos en la lista
	 * 
	 * @return Cantidad de contenidos
	 */
	public int getCantidadContenidos() {
		return this.contenidos.size();
	}
	
	/**
	 * Verifica si la lista está vacía
	 * 
	 * @return true si no tiene contenidos
	 */
	public boolean estaVacia() {
		return this.contenidos.isEmpty();
	}

	// ==================== GETTERS Y SETTERS ====================
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
		this.fechaActualizacion = LocalDateTime.now();
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
		this.fechaActualizacion = LocalDateTime.now();
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		this.fechaActualizacion = LocalDateTime.now();
	}

	public String getCreadorId() {
		return creadorId;
	}

	public void setCreadorId(String creadorId) {
		this.creadorId = creadorId;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags != null ? tags : new HashSet<>();
		this.fechaActualizacion = LocalDateTime.now();
	}

	public String getEspecializacionGestor() {
		return especializacionGestor;
	}

	public void setEspecializacionGestor(String especializacionGestor) {
		this.especializacionGestor = especializacionGestor;
	}

	public Set<Contenido> getContenidos() {
		return contenidos;
	}

	/**
	 * Establece los contenidos de la lista
	 * NOTA: Usar preferiblemente addContenido() para respetar reglas de negocio
	 * 
	 * @param contenidos Set de contenidos
	 */
	public void setContenidos(Set<Contenido> contenidos) {
		this.contenidos = contenidos != null ? contenidos : new HashSet<>();
		this.fechaActualizacion = LocalDateTime.now();
	}

	public LocalDateTime getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(LocalDateTime fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public LocalDateTime getFechaActualizacion() {
		return fechaActualizacion;
	}

	public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
		this.fechaActualizacion = fechaActualizacion;
	}

	// ==================== GETTERS/SETTERS LEGACY (compatibilidad) ====================
	
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
		// Sincronizar con creadorId
		if (usuario != null) {
			this.creadorId = usuario.getId();
		}
	}

	public String getPublico() {
		return publico;
	}

	public void setPublico(String publico) {
		this.publico = publico;
	}
	
	/**
	 * Método legacy para compatibilidad con código existente
	 * @deprecated Usar getContenidos() que retorna Set
	 */
	@Deprecated
	public List<Contenido> getContenidosAsList() {
		return new ArrayList<>(this.contenidos);
	}
	
	/**
	 * Método legacy para compatibilidad con código existente
	 * @deprecated Usar setContenidos(Set) o addContenido()
	 */
	@Deprecated
	public void setContenidosFromList(List<Contenido> contenidos) {
		if (contenidos != null) {
			this.contenidos = new HashSet<>(contenidos);
		} else {
			this.contenidos = new HashSet<>();
		}
		this.fechaActualizacion = LocalDateTime.now();
	}
}