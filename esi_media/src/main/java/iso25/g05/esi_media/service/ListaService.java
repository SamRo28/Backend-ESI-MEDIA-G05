package iso25.g05.esi_media.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import iso25.g05.esi_media.dto.ContenidoResumenDTO;
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
    private static final Logger logger = LoggerFactory.getLogger(ListaService.class);

    public static final String SUCCESS = "success";
    public static final String PERMISOS_ERROR = "Error de permisos";
    public static final String LISTAS = "No se encontró la lista solicitada";
    public static final String TOKEN_ERROR = "Error por token";

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
            throw new RuntimeException(TOKEN_ERROR);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findBySesionToken(token);
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException(TOKEN_ERROR);
        }

        Usuario usuario = usuarioOpt.get();
        
        // Verificar que el token existe en la lista de tokens del usuario y no ha expirado
        boolean tokenValido = usuario.getSesionstoken().getToken().equals(token);
        
        if (!tokenValido) {
            throw new RuntimeException(TOKEN_ERROR);
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
     * Valida los datos básicos de una lista (nombre y descripción)
     * 
     * @param input Lista con los datos a validar
     * @throws RuntimeException si hay errores de validación
     */
    private void validarDatosBasicosLista(Lista input) {
        if (input == null) {
            throw new RuntimeException("La lista no puede ser nula");
        }

        validarNombreYDescripcion(input.getNombre(), input.getDescripcion());
    }

    /**
     * Valida los datos básicos de un DTO de lista (nombre y descripción)
     * 
     * @param dto DTO con los datos a validar
     * @throws RuntimeException si hay errores de validación
     */
    private void validarDatosBasicosDto(PlaylistDto dto) {
        if (dto == null) {
            throw new RuntimeException("Los datos de la lista no pueden ser nulos");
        }

        validarNombreYDescripcion(dto.getNombre(), dto.getDescripcion());
    }

    /**
     * Valida nombre y descripción de una lista
     * 
     * @param nombre Nombre a validar
     * @param descripcion Descripción a validar
     * @throws RuntimeException si hay errores de validación
     */
    private void validarNombreYDescripcion(String nombre, String descripcion) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la lista es obligatorio");
        }
        
        if (nombre.trim().length() < 3) {
            throw new RuntimeException("El nombre debe tener al menos 3 caracteres");
        }

        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new RuntimeException("La descripción de la lista es obligatoria");
        }
        
        if (descripcion.trim().length() < 10) {
            throw new RuntimeException("La descripción debe tener al menos 10 caracteres");
        }
    }

    /**
     * Valida la unicidad del nombre para listas visibles de gestores
     * 
     * @param usuario Usuario creador de la lista
     * @param lista Lista a validar
     * @throws RuntimeException si ya existe una lista visible con el mismo nombre
     */
    private void validarUnicidadNombreLista(Usuario usuario, Lista lista) {
        if (!esGestorConListaVisible(usuario, lista)) {
            return; // No aplica validación de unicidad
        }
        
        if (existeListaVisibleConMismoNombre(usuario.getId(), lista.getNombre())) {
            throw new RuntimeException("El nombre de la lista ya existe entre las visibles");
        }
    }

    /**
     * Verifica si el usuario es gestor y la lista es visible
     * 
     * @param usuario Usuario a verificar
     * @param lista Lista a verificar
     * @return true si es gestor y la lista es visible
     */
    private boolean esGestorConListaVisible(Usuario usuario, Lista lista) {
        Optional<GestordeContenido> gestorOpt = gestorRepository.findById(usuario.getId());
        return gestorOpt.isPresent() && lista.isVisible();
    }

    /**
     * Verifica si ya existe una lista visible con el mismo nombre para el usuario
     * 
     * @param usuarioId ID del usuario
     * @param nombre Nombre de la lista a verificar
     * @return true si existe una lista visible con el mismo nombre
     */
    private boolean existeListaVisibleConMismoNombre(String usuarioId, String nombre) {
        Optional<Lista> listaExistente = listaRepository.findByCreadorIdAndNombreAndVisibleIsTrue(
            usuarioId, nombre
        );
        return listaExistente.isPresent();
    }

    /**
     * Valida la unicidad global del nombre para listas visibles de gestores
     * (El nombre debe ser único entre TODOS los gestores, no solo del mismo creador)
     * 
     * @param usuario Usuario creador de la lista
     * @param lista Lista a validar
     * @throws RuntimeException si ya existe una lista visible con el mismo nombre globalmente
     */
    private void validarUnicidadGlobalNombreLista(Usuario usuario, Lista lista) {
        if (!esGestorConListaVisible(usuario, lista)) {
            return; // No aplica validación de unicidad global
        }
        
        if (existeListaVisibleGlobalConMismoNombre(lista.getNombre())) {
            throw new RuntimeException("Ya existe una lista visible con el nombre '" + lista.getNombre() + "'. Los nombres de listas visibles deben ser únicos.");
        }
    }

    /**
     * Verifica si existe una lista visible con el mismo nombre entre TODOS los gestores
     * 
     * @param nombre Nombre de la lista a verificar
     * @return true si existe una lista visible con el mismo nombre globalmente
     */
    private boolean existeListaVisibleGlobalConMismoNombre(String nombre) {
        List<String> idsGestores = obtenerIdsDeGestores();
        
        List<Lista> listasVisiblesConMismoNombre = listaRepository
            .findByCreadorIdInAndVisibleIsTrue(idsGestores)
            .stream()
            .filter(l -> l.getNombre().equalsIgnoreCase(nombre))
            .toList();
            
        return !listasVisiblesConMismoNombre.isEmpty();
    }

    /**
     * Obtiene los IDs de todos los gestores de contenido
     * 
     * @return Lista con los IDs de todos los gestores
     */
    private List<String> obtenerIdsDeGestores() {
        return gestorRepository.findAll()
            .stream()
            .map(GestordeContenido::getId)
            .toList();
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

                // Visualizador: forzar visible=false y especializacionGestor=null
                lista.setVisible(false);
                lista.setEspecializacionGestor(null);

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
        validarDatosBasicosLista(input);

        // Crear nueva lista
        Lista lista = new Lista();
        lista.setNombre(input.getNombre().trim());
        lista.setDescripcion(input.getDescripcion().trim());
        lista.setCreadorId(usuario.getId());
        lista.setUsuario(usuario); // Legacy
        lista.setTags(input.getTags() != null ? input.getTags() : lista.getTags());
        lista.setVisible(input.isVisible());
        
        // Aplicar reglas según tipo de usuario
        aplicarReglasSegunTipoUsuario(lista, usuario);
        
        // Validar unicidad del nombre para listas visibles de gestores
        validarUnicidadNombreLista(usuario, lista);
        
        // Inicializar fechas si no están establecidas
        if (lista.getFechaCreacion() == null) {
            lista.setFechaCreacion(LocalDateTime.now());
        }
        if (lista.getFechaActualizacion() == null) {
            lista.setFechaActualizacion(LocalDateTime.now());
        }

        // Guardar lista primero
        Lista listaGuardada = listaRepository.save(lista);
        
        // Si hay contenidos en el input, procesarlos (para compatibilidad con frontend)
        if (input.getContenidos() != null && !input.getContenidos().isEmpty()) {
            for (Contenido contenido : input.getContenidos()) {
                if (contenido != null && contenido.getId() != null) {
                    Optional<Contenido> contenidoExistente = contenidoRepository.findById(contenido.getId());
                    if (contenidoExistente.isPresent()) {
                        listaGuardada.addContenido(contenidoExistente.get());
                    }
                }
            }
            listaGuardada = listaRepository.save(listaGuardada);
        }
        
        // Actualizar el usuario o gestor creador añadiendo la lista a su colección
        actualizarUsuarioConLista(usuario.getId(), listaGuardada);
        
        return mapToDto(listaGuardada);
    }

    /**
     * Crea una nueva lista desde el DTO del frontend
     * 
     * @param dto DTO con los datos de la lista desde el frontend
     * @param token Token de sesión del usuario
     * @return PlaylistDto con la lista creada
     * @throws RuntimeException si hay errores de validación o token inválido
     */
    public PlaylistDto crearListaDesdeDto(PlaylistDto dto, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // ============= VALIDACIONES OBLIGATORIAS =============
        validarDatosBasicosDto(dto);

        // 3. VALIDACIÓN: Que tenga al menos 1 contenido (obligatorio)
        if (dto.getContenidosIds() == null || dto.getContenidosIds().isEmpty()) {
            throw new RuntimeException("La lista debe contener al menos un contenido");
        }

        // 4. VALIDACIÓN: Sin elementos repetidos (eliminar duplicados)
        List<String> contenidosUnicos = dto.getContenidosIds().stream()
            .distinct()
            .toList();
            
        if (contenidosUnicos.size() != dto.getContenidosIds().size()) {
            logger.info("Se eliminaron {} elementos duplicados de la lista", 
                dto.getContenidosIds().size() - contenidosUnicos.size());
        }

        // Crear nueva lista
        Lista lista = new Lista();
        lista.setNombre(dto.getNombre().trim());
        lista.setDescripcion(dto.getDescripcion().trim());
        lista.setCreadorId(usuario.getId());
        lista.setUsuario(usuario);
        
        // Procesar tags si existen
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            Set<String> tagsSet = dto.getTags().stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());
            lista.setTags(tagsSet);
        }
        
        lista.setVisible(dto.isVisible());
        lista.setEspecializacionGestor(dto.getEspecializacionGestor());
        
        // Aplicar reglas según tipo de usuario
        aplicarReglasSegunTipoUsuario(lista, usuario);
        
        // Validar unicidad global del nombre para listas visibles de gestores
        validarUnicidadGlobalNombreLista(usuario, lista);
        
        // Establecer fechas
        lista.setFechaCreacion(LocalDateTime.now());
        lista.setFechaActualizacion(LocalDateTime.now());

        // Guardar lista primero
        Lista listaGuardada = listaRepository.save(lista);
        
        // Procesar y añadir contenidos
        for (String contenidoId : contenidosUnicos) {
            Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
            if (contenidoOpt.isPresent()) {
                listaGuardada.addContenido(contenidoOpt.get());
            } else {
                // Log del contenido no encontrado pero continuar
                System.out.println("Contenido no encontrado: " + contenidoId);
            }
        }
        
        // Guardar con contenidos
        listaGuardada = listaRepository.save(listaGuardada);
        
        // Actualizar el usuario o gestor creador añadiendo la lista a su colección
        actualizarUsuarioConLista(usuario.getId(), listaGuardada);
        
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
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Verificar permisos usando el nuevo método de validación
        if (!validarPermisosModificacion(usuario, listaExistente)) {
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
     * Actualiza una lista existente desde un DTO que incluye contenidosIds
     * Maneja tanto los datos básicos como los contenidos de la lista
     * 
     * @param id ID de la lista a actualizar
     * @param dto DTO con los datos actualizados incluyendo contenidosIds
     * @param token Token de sesión del usuario
     * @return PlaylistDto con la lista actualizada
     * @throws RuntimeException si hay errores de validación, token inválido o sin permisos
     */
    public PlaylistDto updateListaDesdeDto(String id, PlaylistDto dto, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista existente
        Lista listaExistente = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Verificar permisos usando el nuevo método de validación
        if (!validarPermisosModificacion(usuario, listaExistente)) {
            throw new RuntimeException("No tienes permisos para editar esta lista");
        }

        // Validaciones básicas del DTO
        if (dto == null) {
            throw new RuntimeException("Los datos de actualización no pueden ser nulos");
        }

        // Validar datos básicos
        validarNombreYDescripcion(dto.getNombre(), dto.getDescripcion());

        // Validar que tenga al menos 1 contenido
        if (dto.getContenidosIds() == null || dto.getContenidosIds().isEmpty()) {
            throw new RuntimeException("La lista debe contener al menos un contenido");
        }

        // Eliminar duplicados en contenidosIds
        List<String> contenidosUnicos = dto.getContenidosIds().stream()
            .distinct()
            .toList();
            
        if (contenidosUnicos.size() != dto.getContenidosIds().size()) {
            logger.info("Se eliminaron {} elementos duplicados de la lista en actualización", 
                dto.getContenidosIds().size() - contenidosUnicos.size());
        }

        // Actualizar campos básicos
        listaExistente.setNombre(dto.getNombre().trim());
        listaExistente.setDescripcion(dto.getDescripcion().trim());
        
        // Procesar tags si existen
        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            Set<String> tagsSet = dto.getTags().stream()
                .filter(tag -> tag != null && !tag.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());
            listaExistente.setTags(tagsSet);
        } else {
            listaExistente.setTags(Set.of()); // Limpiar tags si viene vacío
        }

        // Actualizar visibilidad y re-aplicar reglas
        listaExistente.setVisible(dto.isVisible());
        if (dto.getEspecializacionGestor() != null) {
            listaExistente.setEspecializacionGestor(dto.getEspecializacionGestor());
        }
        aplicarReglasSegunTipoUsuario(listaExistente, usuario);

        // ================ ACTUALIZAR CONTENIDOS ================
        // Limpiar contenidos actuales
        listaExistente.getContenidos().clear();
        
        // Añadir nuevos contenidos
        for (String contenidoId : contenidosUnicos) {
            Optional<Contenido> contenidoOpt = contenidoRepository.findById(contenidoId);
            if (contenidoOpt.isPresent()) {
                listaExistente.addContenido(contenidoOpt.get());
            } else {
                // Log del contenido no encontrado pero continuar
                logger.warn("Contenido no encontrado en actualización: {}", contenidoId);
            }
        }

        // Validar que al menos quedó un contenido válido después del procesamiento
        if (listaExistente.getContenidos().isEmpty()) {
            throw new RuntimeException("No se encontraron contenidos válidos. La lista debe tener al menos un contenido.");
        }
        
        // Actualizar fecha de modificación
        listaExistente.setFechaActualizacion(LocalDateTime.now());

        // Guardar y retornar como DTO
        Lista listaActualizada = listaRepository.save(listaExistente);
        
        logger.info("Lista actualizada exitosamente: {} (ID: {}) con {} contenido(s)", 
            listaActualizada.getNombre(), listaActualizada.getId(), listaActualizada.getContenidos().size());
            
        return mapToDto(listaActualizada);
    }

    /**
     * Elimina una lista
     * Solo el creador original puede eliminar completamente su lista
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
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Verificar permisos usando el nuevo método de validación
        if (!validarPermisosEliminacion(usuario, lista)) {
            throw new RuntimeException("Solo el creador original puede eliminar completamente la lista");
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
     * Obtiene todas las listas visibles de gestores (accesibles para visualizadores)
     * 
     * @param token Token de sesión del usuario
     * @return Lista de PlaylistDto con las listas visibles de gestores
     * @throws RuntimeException si el token es inválido
     */
    public List<PlaylistDto> findListasVisiblesGestores(String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Obtener todos los IDs de gestores
        List<GestordeContenido> gestores = gestorRepository.findAll();
        List<String> idsGestores = gestores.stream()
                .map(GestordeContenido::getId)
                .toList();

        // Buscar todas las listas visibles creadas por gestores
        List<Lista> listas = listaRepository.findByCreadorIdInAndVisibleIsTrue(idsGestores);
        
        // Filtrar contenidos automáticamente según el usuario que accede
        List<Lista> listasFiltradas = listas.stream()
                .map(lista -> filtrarContenidosParaVisualizador(usuario, lista))
                .filter(lista -> !lista.getContenidos().isEmpty()) // Solo mostrar listas con contenidos visibles
                .toList();
        
        // Mapear a DTOs
        return listasFiltradas.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Obtiene una lista específica por su ID
     * Valida que el usuario tenga permisos para ver la lista
     * 
     * @param id ID de la lista
     * @param token Token de sesión del usuario
     * @return PlaylistDto con los datos de la lista
     * @throws RuntimeException si el token es inválido, la lista no existe o el usuario no tiene permisos
     */
    public PlaylistDto findListaById(String id, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Verificar que el usuario tenga permisos para ver la lista
        // Solo el creador puede ver sus listas privadas
        if (!lista.getCreadorId().equals(usuario.getId())) {
            throw new RuntimeException(PERMISOS_ERROR);
        }
        
        // Mapear a DTO y retornar
        return mapToDto(lista);
    }

    /**
     * Obtiene una lista específica por su ID con validación de permisos mejorada
     * Permite a visualizadores acceder a listas VISIBLES de gestores (solo lectura)
     * 
     * @param id ID de la lista
     * @param token Token de sesión del usuario
     * @return PlaylistDto con los datos de la lista
     * @throws RuntimeException si el token es inválido, la lista no existe o el usuario no tiene permisos
     */
    public PlaylistDto findListaByIdConPermisos(String id, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Validar permisos según tipo de usuario y visibilidad de la lista
        if (!validarPermisosLectura(usuario, lista)) {
            throw new RuntimeException(PERMISOS_ERROR);
        }
        
        // Mapear a DTO y retornar
        return mapToDto(lista);
    }

    /**
     * Valida si un usuario puede leer/acceder a una lista específica
     * 
     * REGLAS:
     * - El creador siempre puede acceder a sus propias listas
     * - Los gestores pueden acceder a listas visibles de otros gestores
     * - Los visualizadores pueden acceder a listas visibles de gestores
     * - Nadie puede acceder a listas no visibles de otros usuarios
     * 
     * @param usuario Usuario que intenta acceder
     * @param lista Lista a la que se intenta acceder
     * @return true si tiene permisos, false en caso contrario
     */
    private boolean validarPermisosLectura(Usuario usuario, Lista lista) {
        // Si es el creador, siempre puede acceder
        if (lista.getCreadorId().equals(usuario.getId())) {
            return true;
        }
        
        // Si la lista no es visible, solo el creador puede acceder
        if (!lista.isVisible()) {
            return false;
        }
        
        // La lista es visible, verificar si es una lista de gestor
        Optional<GestordeContenido> creadorGestor = gestorRepository.findById(lista.getCreadorId());
        if (creadorGestor.isEmpty()) {
            // No es una lista de gestor, no se puede acceder
            return false;
        }
        
        // Es una lista visible de gestor, cualquier usuario autenticado puede acceder
        return true;
    }

    /**
     * Valida si un usuario puede modificar (editar/eliminar contenidos) una lista específica
     * 
     * REGLAS:
     * - Solo el creador puede modificar sus propias listas
     * - Los visualizadores NO pueden modificar listas visibles de gestores
     * - Los gestores pueden modificar sus propias listas (visibles y no visibles)
     * 
     * @param usuario Usuario que intenta modificar
     * @param lista Lista que se intenta modificar
     * @return true si tiene permisos, false en caso contrario
     */
    private boolean validarPermisosModificacion(Usuario usuario, Lista lista) {
        // Solo el creador puede modificar la lista
        return lista.getCreadorId().equals(usuario.getId());
    }

    /**
     * Valida si un usuario puede eliminar completamente una lista
     * 
     * REGLAS:
     * - SOLO el creador original puede eliminar completamente la lista
     * 
     * @param usuario Usuario que intenta eliminar
     * @param lista Lista que se intenta eliminar
     * @return true si tiene permisos, false en caso contrario
     */
    private boolean validarPermisosEliminacion(Usuario usuario, Lista lista) {
        // Solo el creador puede eliminar la lista
        return lista.getCreadorId().equals(usuario.getId());
    }

    /**
     * Obtiene los contenidos de una lista específica
     * Valida que el usuario tenga permisos para ver la lista
     * 
     * @param id ID de la lista
     * @param token Token de sesión del usuario
     * @return Lista de contenidos de la lista como DTOs
     * @throws RuntimeException si el token es inválido, la lista no existe o el usuario no tiene permisos
     */
    public List<ContenidoResumenDTO> findContenidosLista(String id, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Verificar que el usuario tenga permisos para ver la lista
        // Solo el creador puede ver sus listas privadas
        if (!lista.getCreadorId().equals(usuario.getId())) {
            throw new RuntimeException("No tienes permisos para acceder a esta lista");
        }
        
        // Filtrar contenidos según el tipo de usuario
        Lista listaFiltrada = filtrarContenidosParaVisualizador(usuario, lista);
        
        // Convertir los contenidos a DTOs
        return listaFiltrada.getContenidos().stream()
                .map(this::mapContenidoToResumenDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los contenidos de una lista con validación mejorada de permisos y filtrado
     * Permite acceso a listas visibles de gestores y aplica filtros según tipo de usuario
     * 
     * @param id ID de la lista
     * @param token Token de sesión del usuario
     * @return Lista de contenidos filtrados de la lista como DTOs
     * @throws RuntimeException si el token es inválido, la lista no existe o el usuario no tiene permisos
     */
    public List<ContenidoResumenDTO> findContenidosListaConFiltrado(String id, String token) {
        // Validar token y obtener usuario
        Usuario usuario = validarToken(token);

        // Buscar la lista
        Lista lista = listaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(LISTAS));

        // Verificar que el usuario tenga permisos para ver la lista usando el nuevo método
        if (!validarPermisosLectura(usuario, lista)) {
            throw new RuntimeException(PERMISOS_ERROR);
        }
        
        // Filtrar contenidos según el tipo de usuario y las restricciones de negocio
        Lista listaFiltrada = filtrarContenidosParaVisualizador(usuario, lista);
        
        // Convertir los contenidos a DTOs
        return listaFiltrada.getContenidos().stream()
                .map(this::mapContenidoToResumenDto)
                .collect(Collectors.toList());
    }

    /**
     * Añade un contenido a una lista
     * Valida que el contenido exista y no esté duplicado
     * Solo permite modificaciones al creador original
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

        // Verificar permisos usando el nuevo método de validación
        if (!validarPermisosModificacion(usuario, lista)) {
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
     * Solo permite modificaciones al creador original
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

        // Verificar permisos usando el nuevo método de validación
        if (!validarPermisosModificacion(usuario, lista)) {
            throw new RuntimeException("No tienes permisos para modificar esta lista");
        }

        // APLICAR RESTRICCIÓN: Una lista debe tener AL MENOS 1 CONTENIDO
        if (lista.getContenidos().size() <= 1) {
            throw new RuntimeException("No se puede eliminar el contenido. La lista debe mantener al menos un contenido.");
        }
        
        // Usar el método de la entidad Lista que valida mínimo 1 contenido
        try {
            boolean eliminado = lista.removeContenido(idContenido);
            
            if (!eliminado) {
                throw new RuntimeException("El contenido no está en la lista");
            }
        } catch (IllegalStateException e) {
            // Re-lanzar la excepción de negocio (mínimo 1 contenido)
            throw new RuntimeException("No se puede eliminar el contenido: " + e.getMessage());
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
     * Actualiza el usuario o gestor añadiendo la lista creada a su colección correspondiente
     * 
     * @param creadorId ID del usuario creador
     * @param lista Lista creada para añadir al usuario
     */
    private void actualizarUsuarioConLista(String creadorId, Lista lista) {
        if (creadorId != null) {
            // OBTENER EL USUARIO REAL PRIMERO
            Optional<Usuario> usuarioOpt = usuarioRepository.findById(creadorId);
            if (usuarioOpt.isEmpty()) {
                return;
            }
            
            Usuario usuario = usuarioOpt.get();
            
            // VERIFICAR EL TIPO REAL DEL USUARIO
            if (usuario instanceof GestordeContenido) {
                GestordeContenido gestor = (GestordeContenido) usuario;
                if (gestor.getListasgeneradas() == null) {
                    gestor.setListasgeneradas(new ArrayList<>());
                }
                gestor.getListasgeneradas().add(lista);
                usuarioRepository.save(gestor);
                return;
            }
            
            if (usuario instanceof Visualizador) {
                Visualizador visualizador = (Visualizador) usuario;
                if (visualizador.listasprivadas == null) {
                    visualizador.listasprivadas = new ArrayList<>();
                }
                visualizador.listasprivadas.add(lista);
                usuarioRepository.save(visualizador);
                return;
            }
            
            // Si no es ni gestor ni visualizador, no hacer nada
        }
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
    
    /**
     * Mapea un objeto Contenido a ContenidoResumenDTO
     * 
     * @param contenido Objeto Contenido a mapear
     * @return ContenidoResumenDTO con los datos del contenido
     */
    private ContenidoResumenDTO mapContenidoToResumenDto(Contenido contenido) {
        String tipo = "AUDIO"; // Valor por defecto
        
        // Determinar el tipo basándose en las subclases
        if (contenido instanceof iso25.g05.esi_media.model.Video) {
            tipo = "VIDEO";
        } else if (contenido instanceof iso25.g05.esi_media.model.Audio) {
            tipo = "AUDIO";
        }
        
        return new ContenidoResumenDTO(
            contenido.getId(),
            contenido.gettitulo(),
            tipo,
            contenido.getcaratula(),
            contenido.isvip()
        );
    }
}
