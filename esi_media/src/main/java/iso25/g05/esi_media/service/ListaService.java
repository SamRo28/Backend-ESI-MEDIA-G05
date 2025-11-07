package iso25.g05.esi_media.service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.dto.PlaylistDto;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Lista;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.ListaRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;

/**
 * Servicio para gestionar las operaciones CRUD de listas de contenido.
 * Incluye validación de tokens de sesión y reglas de negocio por tipo de usuario.
 */
@Service
public class ListaService {

    @Autowired
    private ListaRepository listaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ContenidoRepository contenidoRepository;
    
    @Autowired
    private GestorDeContenidoRepository gestorRepository;
    
    @Autowired
    private VisualizadorRepository visualizadorRepository;

    // ==================== MÉTODOS PRIVADOS DE UTILIDAD ====================
    
    /**
     * Valida un token de sesión y retorna el usuario asociado si es válido
     * 
     * @param token Token de sesión a validar
     * @return Usuario asociado al token
     * @throws RuntimeException si el token es inválido o ha expirado
     */
    private Usuario validarToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token no proporcionado");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findBySesionToken(token);
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Token inválido");
        }

        Usuario usuario = usuarioOpt.get();
        
        // Verificar que el token existe en la lista de tokens del usuario y no ha expirado
        boolean tokenValido = usuario.getSesionstoken().stream()
            .anyMatch(t -> t.getToken().equals(token) && !t.isExpirado() 
                && t.getFechaExpiracion().after(new Date()));
        
        if (!tokenValido) {
            throw new RuntimeException("Token expirado o inválido");
        }

        return usuario;
    }
    
    /**
     * Convierte una entidad Lista a PlaylistDto
     * 
     * @param lista Entidad Lista
     * @return PlaylistDto con los datos mapeados
     */
    private PlaylistDto mapToDto(Lista lista) {
        List<String> contenidosIds = lista.getContenidos().stream()
            .map(Contenido::getId)
            .collect(Collectors.toList());
        
        return new PlaylistDto(
            lista.getId(),
            lista.getNombre(),
            lista.getDescripcion(),
            lista.isVisible(),
            lista.getCreadorId(),
            lista.getTags(),
            lista.getEspecializacionGestor(),
            contenidosIds,
            lista.getFechaCreacion(),
            lista.getFechaActualizacion()
        );
    }
    
    /**
     * Aplica reglas de negocio según el tipo de usuario creador
     * 
     * @param lista Lista a validar y ajustar
     * @param usuario Usuario creador
     */
    private void aplicarReglasSegunTipoUsuario(Lista lista, Usuario usuario) {
        // Verificar si es GestordeContenido
        Optional<GestordeContenido> gestorOpt = gestorRepository.findById(usuario.getId());
        
        if (gestorOpt.isPresent()) {
            GestordeContenido gestor = gestorOpt.get();
            // Gestor puede tener listas visibles o privadas
            // Establecer especialización si está disponible
            if (lista.getEspecializacionGestor() == null) {
                lista.setEspecializacionGestor(gestor.getcampoespecializacion());
            }
        } else {
            // Es Visualizador o Administrador
            // Verificar si es Visualizador
            Optional<Visualizador> visualizadorOpt = visualizadorRepository.findById(usuario.getId());
            
            if (visualizadorOpt.isPresent()) {
                // Visualizador: forzar visible=false y especializacionGestor=null
                lista.setVisible(false);
                lista.setEspecializacionGestor(null);
            } else {
                // Administrador no debería crear listas (pero por si acaso)
                lista.setVisible(false);
                lista.setEspecializacionGestor(null);
            }
        }
    }
    
    // ==================== MÉTODOS PÚBLICOS DEL SERVICIO ====================

    /**
     * Crea una nueva lista de contenido
     * Aplica reglas según el tipo de usuario (Gestor o Visualizador)
     * 
     * @param input Lista con los datos a crear
     * @param token Token de sesión del usuario
     * @return PlaylistDto con la lista creada
     * @throws RuntimeException si hay errores de validación o token inválido
     */
    public PlaylistDto createLista(Lista input, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Validaciones básicas
        if (input == null) {
            throw new RuntimeException("La lista no puede ser nula");
        }

        if (input.getNombre() == null || input.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre de la lista es obligatorio");
        }

        if (input.getDescripcion() == null || input.getDescripcion().trim().isEmpty()) {
            throw new RuntimeException("La descripción de la lista es obligatoria");
        }

        // Crear nueva lista
        Lista lista = new Lista();
        lista.setNombre(input.getNombre());
        lista.setDescripcion(input.getDescripcion());
        lista.setCreadorId(usuario.getId());
        lista.setUsuario(usuario); // Legacy
        lista.setTags(input.getTags() != null ? input.getTags() : lista.getTags());
        lista.setVisible(input.isVisible());
        
        // Aplicar reglas según tipo de usuario
        aplicarReglasSegunTipoUsuario(lista, usuario);
        
        // Inicializar fechas si no están establecidas
        if (lista.getFechaCreacion() == null) {
            lista.setFechaCreacion(LocalDateTime.now());
        }
        if (lista.getFechaActualizacion() == null) {
            lista.setFechaActualizacion(LocalDateTime.now());
        }

        // Guardar y retornar como DTO
        Lista listaGuardada = listaRepository.save(lista);
        return mapToDto(listaGuardada);
    }

    /**
     * Actualiza una lista existente
     * Re-aplica reglas según el tipo de usuario (Gestor o Visualizador)
     * 
     * @param id ID de la lista a actualizar
     * @param input Datos actualizados de la lista
     * @param token Token de sesión del usuario
     * @return PlaylistDto con la lista actualizada
     * @throws RuntimeException si hay errores de validación, token inválido o sin permisos
     */
    public PlaylistDto updateLista(String id, Lista input, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista existente
        Lista listaExistente = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        // Verificar permisos (el usuario debe ser el creador)
        if (!listaExistente.getCreadorId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para editar esta lista");
        }

        // Validaciones
        if (input == null) {
            throw new RuntimeException("Los datos de actualización no pueden ser nulos");
        }

        // Actualizar campos permitidos
        if (input.getNombre() != null && !input.getNombre().trim().isEmpty()) {
            listaExistente.setNombre(input.getNombre());
        }

        if (input.getDescripcion() != null && !input.getDescripcion().trim().isEmpty()) {
            listaExistente.setDescripcion(input.getDescripcion());
        }

        if (input.getTags() != null) {
            listaExistente.setTags(input.getTags());
        }

        // Actualizar visibilidad y re-aplicar reglas
        listaExistente.setVisible(input.isVisible());
        aplicarReglasSegunTipoUsuario(listaExistente, usuario);
        
        // Actualizar fecha de modificación
        listaExistente.setFechaActualizacion(LocalDateTime.now());

        // Guardar y retornar como DTO
        Lista listaActualizada = listaRepository.save(listaExistente);
        return mapToDto(listaActualizada);
    }

    /**
     * Elimina una lista
     * Solo el creador puede eliminar su lista
     * 
     * @param id ID de la lista a eliminar
     * @param token Token de sesión del usuario
     * @throws RuntimeException si el token es inválido, la lista no existe o el usuario no tiene permisos
     */
    public void deleteLista(String id, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista existente
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        // Verificar permisos (el usuario debe ser el creador)
        if (!lista.getCreadorId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para eliminar esta lista");
        }

        // Eliminar la lista
        listaRepository.deleteById(id);
    }

    /**
     * Obtiene todas las listas propias del usuario autenticado
     * 
     * @param token Token de sesión del usuario
     * @return Lista de PlaylistDto con las listas del usuario
     * @throws RuntimeException si el token es inválido
     */
    public List<PlaylistDto> findListasPropias(String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar todas las listas creadas por el usuario
        List<Lista> listas = listaRepository.findByCreadorId(usuario.getId());
        
        // Mapear a DTOs
        return listas.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Añade un contenido a una lista
     * Valida que el contenido exista y no esté duplicado
     * 
     * @param idLista ID de la lista
     * @param idContenido ID del contenido a añadir
     * @param token Token de sesión del usuario
     * @return PlaylistDto con la lista actualizada
     * @throws RuntimeException si el token es inválido, la lista no existe, 
     *         el contenido no existe, el usuario no tiene permisos o ya existe el contenido
     */
    public PlaylistDto addContenido(String idLista, String idContenido, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista existente
        Lista lista = listaRepository.findById(idLista)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        // Verificar permisos (el usuario debe ser el creador)
        if (!lista.getCreadorId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para modificar esta lista");
        }

        // Buscar el contenido existente
        Contenido contenido = contenidoRepository.findById(idContenido)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));

        // Usar el método de la entidad Lista que valida duplicados
        boolean agregado = lista.addContenido(contenido);
        
        if (!agregado) {
            throw new RuntimeException("El contenido ya está en la lista");
        }
        
        // Actualizar fecha de modificación
        lista.setFechaActualizacion(LocalDateTime.now());

        // Guardar y retornar como DTO
        Lista listaActualizada = listaRepository.save(lista);
        return mapToDto(listaActualizada);
    }

    /**
     * Elimina un contenido de una lista
     * Valida que siempre quede al menos 1 contenido
     * 
     * @param idLista ID de la lista
     * @param idContenido ID del contenido a eliminar
     * @param token Token de sesión del usuario
     * @return PlaylistDto con la lista actualizada
     * @throws RuntimeException si el token es inválido, la lista no existe,
     *         el contenido no está en la lista, el usuario no tiene permisos,
     *         o se intenta eliminar el último contenido
     */
    public PlaylistDto removeContenido(String idLista, String idContenido, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista existente
        Lista lista = listaRepository.findById(idLista)
                .orElseThrow(() -> new RuntimeException("Lista no encontrada"));

        // Verificar permisos (el usuario debe ser el creador)
        if (!lista.getCreadorId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para modificar esta lista");
        }

        // Usar el método de la entidad Lista que valida mínimo 1 contenido
        try {
            boolean eliminado = lista.removeContenido(idContenido);
            
            if (!eliminado) {
                throw new RuntimeException("El contenido no está en la lista");
            }
        } catch (IllegalStateException e) {
            // Re-lanzar la excepción de negocio (mínimo 1 contenido)
            throw new RuntimeException(e.getMessage());
        }
        
        // Actualizar fecha de modificación
        lista.setFechaActualizacion(LocalDateTime.now());

        // Guardar y retornar como DTO
        Lista listaActualizada = listaRepository.save(lista);
        return mapToDto(listaActualizada);
    }
    
    // ==================== MÉTODOS DE FILTRADO PARA VISUALIZADORES ====================
    
    /**
     * Filtra los contenidos de una lista según las restricciones del visualizador
     * 
     * REGLAS DE FILTRADO:
     * - Excluye contenidos VIP si el usuario no es VIP
     * - Excluye contenidos cuya edad mínima supera la edad del visualizador
     * - Excluye contenidos cuya fecha de disponibilidad ha expirado
     * - Si un contenido vuelve a cumplir las condiciones (ej: renovación VIP, 
     *   fecha disponible actualizada), se vuelve a mostrar automáticamente
     * 
     * NOTA: Este método está preparado para uso futuro en vistas de reproducción.
     * No se invoca desde el controller actualmente.
     * 
     * @param usuario Usuario que intenta acceder (debe ser Visualizador o derivado)
     * @param lista Lista con contenidos a filtrar
     * @return Lista con solo los contenidos disponibles para el usuario
     */
    public Lista filtrarContenidosParaVisualizador(Usuario usuario, Lista lista) {
        // Solo aplicar filtrado a visualizadores
        if (!(usuario instanceof Visualizador)) {
            // Gestores y Administradores pueden ver todos los contenidos sin restricciones
            return lista;
        }
        
        Visualizador visualizador = (Visualizador) usuario;
        Date fechaActual = new Date();
        
        // Calcular edad del visualizador
        int edadUsuario = calcularEdad(visualizador.getFechaNac());
        boolean esVip = visualizador.isVip();
        
        // Filtrar contenidos según las restricciones
        List<Contenido> contenidosFiltrados = lista.getContenidos().stream()
            .filter(contenido -> esContenidoDisponiblePara(contenido, edadUsuario, esVip, fechaActual))
            .collect(Collectors.toList());
        
        // Crear una copia de la lista con los contenidos filtrados
        Lista listaFiltrada = new Lista();
        listaFiltrada.setId(lista.getId());
        listaFiltrada.setNombre(lista.getNombre());
        listaFiltrada.setDescripcion(lista.getDescripcion());
        listaFiltrada.setVisible(lista.isVisible());
        listaFiltrada.setCreadorId(lista.getCreadorId());
        listaFiltrada.setTags(lista.getTags());
        listaFiltrada.setEspecializacionGestor(lista.getEspecializacionGestor());
        listaFiltrada.setFechaCreacion(lista.getFechaCreacion());
        listaFiltrada.setFechaActualizacion(lista.getFechaActualizacion());
        
        // Convertir lista a set y asignar
        contenidosFiltrados.forEach(listaFiltrada::addContenido);
        
        return listaFiltrada;
    }
    
    /**
     * Verifica si un contenido está disponible para un visualizador
     * según edad, VIP y fecha de disponibilidad
     * 
     * @param contenido Contenido a verificar
     * @param edadUsuario Edad del visualizador
     * @param esVip Si el usuario tiene suscripción VIP
     * @param fechaActual Fecha actual para comparar disponibilidad
     * @return true si el contenido está disponible, false en caso contrario
     */
    private boolean esContenidoDisponiblePara(Contenido contenido, int edadUsuario, 
                                              boolean esVip, Date fechaActual) {
        // 1. Verificar restricción VIP
        if (contenido.isvip() && !esVip) {
            return false; // Contenido VIP no disponible para usuarios no VIP
        }
        
        // 2. Verificar restricción de edad
        if (contenido.getedadvisualizacion() > edadUsuario) {
            return false; // Usuario no cumple edad mínima
        }
        
        // 3. Verificar fecha de disponibilidad
        Date fechaDisponibleHasta = contenido.getfechadisponiblehasta();
        if (fechaDisponibleHasta != null && fechaDisponibleHasta.before(fechaActual)) {
            return false; // Contenido ya no está disponible
        }
        
        // Contenido disponible para el visualizador
        return true;
    }
    
    /**
     * Calcula la edad de una persona a partir de su fecha de nacimiento
     * 
     * @param fechaNacimiento Fecha de nacimiento
     * @return Edad en años
     */
    private int calcularEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return 0;
        }
        
        // Convertir Date a LocalDate para cálculo de edad
        java.util.Calendar calNacimiento = java.util.Calendar.getInstance();
        calNacimiento.setTime(fechaNacimiento);
        
        java.util.Calendar calActual = java.util.Calendar.getInstance();
        
        int edad = calActual.get(java.util.Calendar.YEAR) - calNacimiento.get(java.util.Calendar.YEAR);
        
        // Ajustar si aún no ha cumplido años este año
        if (calActual.get(java.util.Calendar.MONTH) < calNacimiento.get(java.util.Calendar.MONTH) ||
            (calActual.get(java.util.Calendar.MONTH) == calNacimiento.get(java.util.Calendar.MONTH) &&
             calActual.get(java.util.Calendar.DAY_OF_MONTH) < calNacimiento.get(java.util.Calendar.DAY_OF_MONTH))) {
            edad--;
        }
        
        return edad;
    }
}
