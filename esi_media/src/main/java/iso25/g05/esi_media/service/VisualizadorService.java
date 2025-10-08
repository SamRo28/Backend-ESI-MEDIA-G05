package iso25.g05.esi_media.service;

import iso25.g05.esi_media.model.*;
import org.springframework.stereotype.Service;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para gestionar el registro de visualizadores.
 * 
 * Por ahora trabaja solo en memoria sin persistencia real.
 * Incluye todas las validaciones de negocio específicas del dominio
 * que van más allá de las validaciones básicas de Jakarta.
 */
@Service
public class VisualizadorService {
    
    /**
     * Almacenamiento temporal en memoria (simula base de datos)
     * ConcurrentHashMap para thread-safety básico
     */
    private final Map<String, Visualizador> visualizadoresRegistrados = new ConcurrentHashMap<>();
    
    /**
     * Validador de Jakarta para verificar anotaciones
     */
    private final Validator validator;
    
    /**
     * Constructor que inyecta el validador
     */
    public VisualizadorService(Validator validator) {
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
        for (ConstraintViolation<VisualizadorRegistroDTO> violacion : violaciones) {
            errores.add(violacion.getMessage()); // Ejemplo: "El email es obligatorio"
        }
        
        // PASO 2: Verificar reglas específicas de nuestro negocio
        // Cosas como: ¿tiene 4+ años? ¿las contraseñas coinciden?
        errores.addAll(validarReglasDeNegocio(dto));
        
        // PASO 3: Verificar que no haya duplicados
        // ¿Ya existe alguien con ese email?
        errores.addAll(validarUnicidad(dto));
        
        // Si encontramos cualquier error, paramos aquí y se lo decimos al usuario
        if (!errores.isEmpty()) {
            return new RegistroResultado(errores, "Registro fallido: corrija los errores indicados");
        }
        
        // PASO 4: Si llegamos aquí, todo está bien. Crear el usuario
        try {
            // Convertir los datos del formulario en un objeto Visualizador completo
            Visualizador nuevoVisualizador = crearVisualizador(dto);
            
            // PASO 5: Guardarlo en nuestra "base de datos" temporal (HashMap)
            // La clave es el email, el valor es el visualizador completo
            visualizadoresRegistrados.put(nuevoVisualizador.getEmail(), nuevoVisualizador);
            
            // Devolver resultado exitoso con el usuario creado
            return new RegistroResultado(nuevoVisualizador, "Visualizador registrado exitosamente");
            
        } catch (Exception e) {
            // Si algo salió mal creando el usuario (error de programación probablemente)
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
        if (dto.getPassword() != null && dto.getPasswordConfirm() != null && !dto.getPassword().equals(dto.getPasswordConfirm())) {
            errores.add("La contraseña y su confirmación no coinciden");
        }
        
        return errores; // Devolvemos todos los errores que encontramos (o lista vacía si todo bien)
    }
    
    /**
     * Valida que no haya duplicados en el sistema
     */
    private List<String> validarUnicidad(VisualizadorRegistroDTO dto) {
        List<String> errores = new ArrayList<>();
        
        // REGLA: Email único
        if (dto.getEmail() != null && visualizadoresRegistrados.containsKey(dto.getEmail())) {
            errores.add("Ya existe un usuario registrado con este email");
        }
        
        return errores;
    }
    
    /**
     * Convierte los datos del formulario en un objeto Visualizador completo
     * 
     * PROPÓSITO: Tomar los datos "en crudo" del formulario y armar todos los objetos
     * necesarios para que el sistema funcione (Visualizador + Contrasenia)
     */
    private Visualizador crearVisualizador(VisualizadorRegistroDTO dto) {
        
        // PASO 1: Aplicar regla de negocio del alias
        // Si el usuario no escribió alias (o escribió espacios vacíos), usar su nombre
        String aliasDefinitivo = (dto.getAlias() == null || dto.getAlias().trim().isEmpty()) 
                                ? dto.getNombre()  // usar nombre como alias
                                : dto.getAlias();  // usar el alias que escribió
        
        // PASO 2: Crear el objeto Contrasenia
        // ¿Por qué separado? Porque el sistema maneja historial de contraseñas, expiración, etc.
        Contrasenia contrasenia = new Contrasenia(
            UUID.randomUUID().toString(), // ID único para esta contraseña
            new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)), // expira en 1 año desde hoy
            dto.getPassword(), // la contraseña que escribió el usuario
            new ArrayList<>(), // lista de contraseñas anteriores (vacía porque es nuevo)
            null // el usuario dueño (lo ponemos después)
        );
        
        // PASO 3: Crear el objeto Visualizador principal
        // Usar el constructor que ya existía en la clase
        Visualizador visualizador = new Visualizador(
            dto.getApellidos(),     // apellidos del formulario
            false,                  // no está bloqueado (usuario nuevo)
            contrasenia,           // objeto contraseña que creamos arriba
            dto.getEmail(),        // email del formulario
            dto.getFoto(),         // foto (puede ser null si no subió)
            dto.getNombre(),       // nombre del formulario
            aliasDefinitivo,       // alias procesado en el PASO 1
            dto.getFechaNac(),     // fecha de nacimiento del formulario
            dto.isVip()            // si marcó la casilla VIP
        );
        
        // PASO 4: Establecer la relación bidireccional Usuario ↔ Contraseña
        // 
        // EXPLICACIÓN DE LA RELACIÓN BIDIRECCIONAL:
        // 
        // ANTES de esta línea:
        // Visualizador -----> Contrasenia    (el visualizador conoce su contraseña)
        // Visualizador  ? <-- Contrasenia    (pero la contraseña NO sabe de qué usuario es)
        // 
        // DESPUÉS de esta línea:
        // Visualizador <----> Contrasenia    (ambos se conocen mutuamente)
        // 
        // ¿Por qué es importante?
        // - Auditoría: La contraseña sabe a quién pertenece
        // - Validaciones: Podemos verificar consistencia desde cualquier lado
        // - Navegación: Desde contraseña podemos llegar al usuario y viceversa
        // - Base de datos: Facilita las relaciones cuando agregemos persistencia
        contrasenia.set_unnamed_Usuario_(visualizador);
        
        // Devolver el visualizador completo y listo para usar
        return visualizador;
    }
    
    /**
     * Método helper para obtener todos los visualizadores registrados
     * (útil para testing y debugging)
     */
    public Collection<Visualizador> obtenerTodosLosVisualizadores() {
        return visualizadoresRegistrados.values();
    }
    
    /**
     * Método helper para limpiar el almacenamiento
     * (útil para testing)
     */
    public void limpiarRegistros() {
        visualizadoresRegistrados.clear();
    }
    
    /**
     * Método helper para buscar por email
     * 
     * EXPLICACIÓN DE: Optional.ofNullable(visualizadoresRegistrados.get(email))
     * 
     * 1. visualizadoresRegistrados.get(email) → Busca en el HashMap por email
     *    - Si encuentra el usuario: devuelve el objeto Visualizador
     *    - Si NO encuentra el usuario: devuelve null
     * 
     * 2. Optional.ofNullable(...) → Envuelve el resultado en un Optional
     *    - Si el resultado era un Visualizador: devuelve Optional<Visualizador> con contenido
     *    - Si el resultado era null: devuelve Optional.empty()
     * 
     * 3. ¿Por qué Optional? Para evitar NullPointerException en el código que use este método
     *    
     * EJEMPLO DE USO:
     * Optional<Visualizador> resultado = service.buscarPorEmail("juan@email.com");
     * if (resultado.isPresent()) {
     *     Visualizador usuario = resultado.get(); // Seguro, sabemos que existe
     * } else {
     *     System.out.println("No existe usuario con ese email");
     * }
     */
    public Optional<Visualizador> buscarPorEmail(String email) {
        // Buscar en el HashMap y envolver el resultado en Optional para manejo seguro de nulls
        return Optional.ofNullable(visualizadoresRegistrados.get(email));
    }
}