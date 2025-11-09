package iso25.g05.esi_media.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import iso25.g05.esi_media.dto.VisualizadorRegistroDTO;
import iso25.g05.esi_media.model.Contrasenia;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContraseniaComunRepository;
import iso25.g05.esi_media.repository.ContraseniaRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/**
 * Servicio para gestionar el registro de visualizadores con persistencia MongoDB.
 * 
 * MIGRACIÓN A PERSISTENCIA REAL:
 * - Eliminado almacenamiento en memoria (ConcurrentHashMap)
 * - Agregada inyección de repositorios MongoDB
 * - Mantenidas todas las validaciones de negocio existentes
 * - Agregado manejo de transacciones y excepciones de BD
 * 
 * REPOSITORIOS UTILIZADOS:
 * - UsuarioRepository: Para verificaciones de unicidad (email) entre todos los tipos de usuario
 * - VisualizadorRepository: Para operaciones específicas de visualizadores
 */
@Service
public class VisualizadorService {
    
    @Autowired
    private UserService userService;

    @Autowired
    private ContraseniaComunRepository contraseniaComunRepository;

    /**
     * Repositorio general para operaciones de usuario (unicidad de email, etc.)
     */
    private final UsuarioRepository usuarioRepository;
    
    /**
     * Repositorio específico para operaciones de visualizador
     */
    private final VisualizadorRepository visualizadorRepository;
    
    /**
     * Repositorio para operaciones de contraseña
     */
    private final ContraseniaRepository contraseniaRepository;
    
    /**
     * Validador de Jakarta para verificar anotaciones
     */
    private final Validator validator;

    // Logger SLF4J
    private static final Logger logger = LoggerFactory.getLogger(VisualizadorService.class);
    
    /**
     * Constructor que inyecta repositorios y validador
     * 
     * @param usuarioRepository Repositorio base para todos los usuarios
     * @param visualizadorRepository Repositorio específico para visualizadores
     * @param validator Validador Jakarta para anotaciones
     */
    public VisualizadorService(UsuarioRepository usuarioRepository, 
                              VisualizadorRepository visualizadorRepository,
                              ContraseniaRepository contraseniaRepository,
                              Validator validator) {
        this.usuarioRepository = usuarioRepository;
        this.visualizadorRepository = visualizadorRepository;
        this.contraseniaRepository = contraseniaRepository;
        this.validator = validator;
    }
    
    /**
     * Método principal para registrar un nuevo visualizador.
     * 
     * Recibe datos del formulario y los convierte en un usuario válido
     * o devuelve una lista clara de qué está mal.
     * 
     * @param dto Datos que vienen del formulario web
     * @return RegistroResultado que dice si funcionó o qué errores hubo
     */
    public RegistroResultado registrarVisualizador(VisualizadorRegistroDTO dto) {
        // Lista donde vamos acumulando todos los errores que encontremos
        List<String> errores = new ArrayList<>();
        
        // PASO 1: Verificar las validaciones automáticas (@NotBlank, @Email, etc.)
        // Esto revisa si el nombre está vacío, si el email tiene formato correcto, etc.
        Set<ConstraintViolation<VisualizadorRegistroDTO>> violaciones = validator.validate(dto);
        logger.debug("Violaciones automáticas encontradas: {}", violaciones.size());
        for (ConstraintViolation<VisualizadorRegistroDTO> violacion : violaciones) {
            logger.warn("Validación fallida - {}: {}", violacion.getPropertyPath(), violacion.getMessage());
            errores.add(violacion.getMessage()); // Ejemplo: "El email es obligatorio"
        }
        
        // PASO 2: Verificar reglas específicas de nuestro negocio
        // Cosas como: ¿tiene 4+ años? ¿las contraseñas coinciden?
        List<String> erroresNegocio = validarReglasDeNegocio(dto);
        logger.debug("Errores de negocio encontrados: {}", erroresNegocio.size());
        for (String error : erroresNegocio) {
            logger.warn("Regla de negocio: {}", error);
        }
        errores.addAll(erroresNegocio);
        
        // PASO 3: Verificar que no haya duplicados
        // ¿Ya existe alguien con ese email?
        List<String> erroresUnicidad = validarUnicidad(dto);
        logger.debug("Errores de unicidad encontrados: {}", erroresUnicidad.size());
        for (String error : erroresUnicidad) {
            logger.warn("Unicidad: {}", error);
        }
        errores.addAll(erroresUnicidad);
        
        // Si encontramos cualquier error, paramos aquí y se lo decimos al usuario
        if (!errores.isEmpty()) {
            return new RegistroResultado(errores, "Registro fallido: corrija los errores indicados");
        }
        
        // PASO 4: Si llegamos aquí, todo está bien. Crear el usuario
        try {
            // Convertir los datos del formulario en un objeto Visualizador completo
            Visualizador nuevoVisualizador = crearVisualizador(dto);
            
            // PASO 5: Guardar en MongoDB usando VisualizadorRepository
            // Spring Data MongoDB maneja automáticamente:
            // - Generación de ID único (ObjectId)
            // - Serialización a BSON
            // - Índices únicos (email)
            // - Campo discriminador "_class" para herencia
            Visualizador visualizadorGuardado = visualizadorRepository.save(nuevoVisualizador);
            
            // Devolver resultado exitoso con el usuario guardado (incluye ID generado)
            return new RegistroResultado(visualizadorGuardado, "Visualizador registrado exitosamente en base de datos");
            
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // Error específico: intento de guardar email duplicado (violación de índice único)
            logger.error("DuplicateKeyException capturada: {}", e.getMessage(), e);
            logger.error("Causa: {}", e.getCause() != null ? e.getCause().getMessage() : "Sin causa");

            // Volver a verificar el email para debugging
            boolean existsNow = usuarioRepository.existsByEmail(dto.getEmail());
            logger.debug("Email existe después de excepción: {}", existsNow);

            return new RegistroResultado("El email ya está registrado en el sistema", 
                                        "Registro fallido: email duplicado detectado por MongoDB");
        } catch (org.springframework.data.mongodb.UncategorizedMongoDbException e) {
            // Error de conexión o configuración de MongoDB
            logger.error("UncategorizedMongoDbException: {}", e.getMessage(), e);
            return new RegistroResultado("Error de conexión con la base de datos: " + e.getMessage(), 
                                        "Registro fallido: problema de conectividad");
        } catch (Exception e) {
            // Cualquier otro error inesperado
            logger.error("Error interno al crear visualizador: {}", e.getMessage(), e);
            return new RegistroResultado("Error interno al crear visualizador: " + e.getMessage(), 
                                        "Registro fallido por error del sistema");
        }
    }
    
    /**
     * Valida las reglas especiales de nuestro negocio
     * 
     * Verificar cosas que las anotaciones @NotBlank no pueden revisar
     * Por ejemplo: cálculos de fecha, comparar dos campos entre sí, etc.
     */
    private List<String> validarReglasDeNegocio(VisualizadorRegistroDTO dto) {
        List<String> errores = new ArrayList<>();
        
        // REGLA 1: Verificar que tenga al menos 4 años
        // ¿Por qué? Es política de la empresa - menores de 4 no pueden registrarse
        if (dto.getFechaNac() != null) {
            // Convertir la fecha de nacimiento a LocalDate para poder hacer cálculos
            LocalDate fechaNac = dto.getFechaNac().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
            
            // Calcular cuál sería la fecha de alguien que tiene exactamente 4 años hoy
            LocalDate fechaMinima = LocalDate.now().minusYears(4);
            
            // Si nació después de esa fecha, tiene menos de 4 años
            if (fechaNac.isAfter(fechaMinima)) {
                errores.add("Debe tener al menos 4 años para registrarse");
            }
        }
        
        // REGLA 2: Las dos contraseñas que escribió deben ser idénticas
        // ¿Por qué? Para evitar que se equivoque escribiendo su contraseña
        // Si a la hora de comprobar que coinciden las contraseñas alguna es nula (tanto la
        // original como la repetida) o no coinciden entre ellas agregamos el error
        if (dto.getContrasenia() != null && dto.getConfirmacionContrasenia() != null && !dto.getContrasenia().equals(dto.getConfirmacionContrasenia())) {
            errores.add("La contraseña y su confirmación no coinciden");
        }
        
        return errores; // Devolvemos todos los errores que encontramos (o lista vacía si todo bien)
    }
    
    /**
     * Valida que no haya duplicados en el sistema usando MongoDB
     * 
     * MEJORA CON PERSISTENCIA:
     * - Consulta real a la base de datos en lugar de HashMap en memoria
     * - Verifica contra TODOS los tipos de usuario (Visualizador, Admin, Gestor)
     * - Usa el índice único de email para consulta eficiente
     */
    private List<String> validarUnicidad(VisualizadorRegistroDTO dto) {
        List<String> errores = new ArrayList<>();
        
        // REGLA: Email único en toda la base de datos - DEPURACIÓN EXTENDIDA
        if (dto.getEmail() != null) {
            logger.debug("Validando email: {}", dto.getEmail());

            // Primero, veamos todos los usuarios en la base de datos
            List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
            logger.debug("Total usuarios en BD: {}", todosLosUsuarios.size());
            for (Usuario u : todosLosUsuarios) {
                logger.debug("Usuario en BD: {} - {}", u.getNombre(), u.getEmail());
            }

            // Ahora probemos el exists
            boolean exists = usuarioRepository.existsByEmail(dto.getEmail());
            logger.debug("existsBy_email('{}'): {}", dto.getEmail(), exists);

            // Y también el findBy
            Optional<Usuario> usuario = usuarioRepository.findByEmail(dto.getEmail());
            logger.debug("findBy_email encontrado: {}", usuario.isPresent());
            if (usuario.isPresent()) {
                logger.debug("Usuario encontrado: {} - {}", usuario.get().getNombre(), usuario.get().getEmail());
            }

            // Verificar si hay algún problema con mayúsculas/minúsculas
            String emailLower = dto.getEmail().toLowerCase();
            boolean existsLower = usuarioRepository.existsByEmail(emailLower);
            logger.debug("existsBy_email con lowercase('{}'): {}", emailLower, existsLower);

            if (exists) {
                errores.add("El email ya está registrado en el sistema");
            }
        }
        
        return errores;
    }
    
    /**
     * Convierte los datos del formulario en un objeto Visualizador completo
     * 
     * PROPÓSITO: Tomar los datos "en crudo" del formulario y armar todos los objetos
     * necesarios para que el sistema funcione (Visualizador + Contrasenia)
     */
    private Visualizador crearVisualizador(VisualizadorRegistroDTO dto) throws Exception {
        
        // PASO 1: Aplicar regla de negocio del alias
        // Si el usuario no escribió alias (o escribió espacios vacíos), usar su nombre
        String aliasDefinitivo = (dto.getAlias() == null || dto.getAlias().trim().isEmpty()) 
                                ? dto.getNombre()  // usar nombre como alias
                                : dto.getAlias();  // usar el alias que escribió
        
        // TEMPORAL: Sin objeto Contrasenia para evitar relaciones circulares problemáticas
        // PASO 1: Crear contraseña con nuevo constructor sin referencia de usuario
        Contrasenia c = new Contrasenia(
            null, // ID será generado por MongoDB
            new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)), // Expira en 1 año
            dto.getContrasenia(), // Contraseña actual
            new ArrayList<>() // Lista de contraseñas anteriores vacía
        );

        Contrasenia contrasenia = userService.hashearContrasenia(c);

        if(contraseniaComunRepository.existsById(contrasenia.getContraseniaActual())){
            throw new Exception("La contraseña está en la lista de contraseñas comunes");
        }
        
        // PASO 2: GUARDAR contraseña en MongoDB para que obtenga ID
    logger.debug("Guardando contraseña en MongoDB...");
    Contrasenia contraseniaGuardada = contraseniaRepository.save(contrasenia);
    logger.debug("Contraseña guardada con ID: {}", contraseniaGuardada.getId());
        
        // PASO 3: Crear el objeto Visualizador principal (SIN RELACIONES PROBLEMÁTICAS)
        Visualizador visualizador = new Visualizador();
        visualizador.setApellidos(dto.getApellidos());
        visualizador.setBloqueado(false);
        
        // PASO 4: Asignar contraseña guardada (que ya tiene ID)
        if (contraseniaGuardada.getId() != null) {
            visualizador.setContrasenia(contraseniaGuardada);
            logger.debug("Contraseña asignada correctamente al visualizador");
        } else {
            logger.error("Contraseña no tiene ID después de guardar");
            // Lanzamos una excepción específica para facilitar el manejo y pruebas
            throw new RegistroException("Error al guardar contraseña: sin ID");
        }
        visualizador.setEmail(dto.getEmail());
        visualizador.setFoto(dto.getFoto());
        visualizador.setNombre(dto.getNombre());
        visualizador.setAlias(aliasDefinitivo);
        visualizador.setFechaNac(dto.getFechaNac());
        visualizador.setVip(dto.isVip());
        // Establecer fecha de registro actual
        visualizador.setFechaRegistro(new Date());
        
        // PASO 4: TEMPORALMENTE COMENTADO - Evitar referencias circulares
        // Nota: El problema de StackOverflow se evita actualmente no estableciendo
        // la referencia inversa desde entidades como Contrasenia hacia Usuario.
        // Diseño actual: Usuario conoce su Contrasenia, pero Contrasenia no mantiene
        // un puntero al Usuario. Cuando se requiera una refactorización completa
        // del modelo para relaciones bidireccionales, crear tests que verifiquen
        // serialización y evitar ciclos mediante @JsonIgnore o DBRef lazy.
        // Devolver el visualizador completo y listo para usar
        return visualizador;
    }

    /**
     * Excepción específica para errores durante el proceso de registro.
     *
     * Usamos una excepción dedicada en lugar de RuntimeException para que el
     * controlador o tests puedan capturar y distinguir errores de registro.
     */
    public static class RegistroException extends RuntimeException {
        public RegistroException(String message) {
            super(message);
        }

        public RegistroException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // === MÉTODOS HELPER PARA OPERACIONES ADICIONALES CON MONGODB ===
    
    /**
     * Método helper para obtener todos los visualizadores registrados
     * (útil para testing, debugging y listados administrativos)
     * 
     * MIGRACIÓN A MONGODB:
     * - Antes: visualizadoresRegistrados.values() → datos en memoria
     * - Ahora: visualizadorRepository.findAll() → consulta a base de datos
     */
    public List<Visualizador> obtenerTodosLosVisualizadores() {
        return visualizadorRepository.findAll();
    }
    
    /**
     * Método helper para limpiar el almacenamiento
     * (útil para testing - USAR CON PRECAUCIÓN EN PRODUCCIÓN)
     * 
     * ADVERTENCIA: Este método ELIMINA TODOS los visualizadores de la base de datos
     * Solo usar en entornos de desarrollo y testing
     */
    public void limpiarRegistros() {
        visualizadorRepository.deleteAll();
    }
    
    /**
     * Método helper para buscar visualizador por email
     * 
     * EXPLICACIÓN DE LA MIGRACIÓN A MONGODB:
     * 
     * ANTES (HashMap):
     * 1. visualizadoresRegistrados.get(email) → Busca en memoria
     * 2. Optional.ofNullable(...) → Manejo seguro de nulls
     * 
     * AHORA (MongoDB):
     * 1. visualizadorRepository.findBy_email(email) → Consulta a base de datos
     * 2. Spring Data ya devuelve Optional<Visualizador> automáticamente
     * 3. MongoDB usa índice único en email para búsqueda eficiente
     * 
     * VENTAJAS:
     * - Datos persistentes (no se pierden al reiniciar)
     * - Consulta optimizada con índices
     * - Tipo específico Visualizador (no Usuario genérico)
     * 
     * EJEMPLO DE USO (sin cambios):
     * Optional<Visualizador> resultado = service.buscarPorEmail("juan@email.com");
     * if (resultado.isPresent()) {
     *     Visualizador usuario = resultado.get(); // Seguro, sabemos que existe
     * } else {
     *     System.out.println("No existe visualizador con ese email");
     * }
     */
    public Optional<Visualizador> buscarPorEmail(String email) {
        // Usar repositorio específico de Visualizador para garantizar tipo correcto
        return visualizadorRepository.findBy_email(email);
    }
    
    /**
     * Método helper adicional: contar visualizadores VIP
     * (nuevo método posible gracias a la persistencia)
     */
    public long contarVisualizadoresVip() {
        return visualizadorRepository.countBy_vip(true);
    }
    
    /**
     * Método helper adicional: buscar visualizadores por alias
     * (útil para funciones de búsqueda de usuarios públicos)
     */
    public List<Visualizador> buscarPorAlias(String alias) {
        return visualizadorRepository.findByAliasContainingIgnoreCase(alias);
    }
    
    /**
     * Método para eliminar un visualizador específico por ID
     */
    public boolean eliminarVisualizador(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        
        try {
            visualizadorRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            logger.error("Error al eliminar visualizador con ID {}: {}", id, e.getMessage());
            return false;
        }
    }

    public String activar2FA(String email) {
        String res = "";
        Optional<Usuario> existingUser = this.usuarioRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            GoogleAuthenticator gAuth = new GoogleAuthenticator();
            GoogleAuthenticatorKey key = gAuth.createCredentials();

            String secret = key.getKey();

            String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthURL("MiApp", email, key);
            Usuario user = existingUser.get();
            user.setSecretkey(secret); 
            usuarioRepository.save(user);

            res = otpAuthURL;
        }

        return res;
    }

}