package iso25.g05.esi_media.config;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Limpiador de índices problemáticos en MongoDB.
 * 
 * ¿POR QUÉ EXISTEN ÍNDICES PROBLEMÁTICOS?
 * 
 * Imagina que estás organizando una biblioteca:
 * 1. Al principio, creaste fichas para organizar los libros por "autor", "género", "fecha"
 * 2. Después decidiste que la ficha "género" era problemática y la eliminaste del sistema
 * 3. PERO las fichas físicas siguieron en los cajones de la biblioteca
 * 4. Cuando intentas añadir un libro nuevo, la biblioteca dice:
 *    "ERROR: La ficha 'género' ya tiene un valor vacío, no puedo añadir otro vacío"
 * 
 * LO MISMO PASA CON MONGODB:
 * 1. Antes teníamos código como: List<Codigo_recuperacion> _codigos_recuperacion_;
 * 2. MongoDB creó automáticamente un índice único para este campo
 * 3. Comentamos el código pero MongoDB mantuvo el índice
 * 4. Al registrar usuarios nuevos, MongoDB dice:
 *    "E11000 duplicate key error: ya existe un valor null en _codigos_recuperacion_._email"
 * 
 * ¿POR QUÉ HAY QUE ELIMINAR ESTOS ÍNDICES?
 * 
 * - Los índices únicos NO PERMITEN duplicados (ni siquiera valores null duplicados)
 * - Campos comentados = valores null en todos los usuarios
 * - MongoDB ve: null, null, null... y dice "¡DUPLICADOS!"
 * - Resultado: No se pueden crear usuarios nuevos
 * 
 * LA SOLUCIÓN:
 * - Eliminar índices de campos que ya no usamos (problemáticos)
 * - Mantener índices de campos que sí usamos (útiles como email, nombre)
 * - Permitir que MongoDB funcione normalmente sin errores E11000
 */
@Component
public class MongoIndexCleaner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexCleaner.class);
    private static final String COLLECTION_USERS = "users";
    
    private final MongoTemplate mongoTemplate;

    public MongoIndexCleaner(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Método que se ejecuta automáticamente cuando arranca Spring Boot.
     */
    @Override
    public void run(String[] args) {
    logger.info("INICIANDO LIMPIEZA DE ÍNDICES PROBLEMÁTICOS");
        
        try {
            // Llamar al método que hace la limpieza real
            limpiarIndicesProblematicos();
            logger.info("LIMPIEZA DE ÍNDICES COMPLETADA EXITOSAMENTE");
            
        } catch (Exception e) {
            // Si algo sale mal, registrar el error en los logs
            logger.error("Error durante limpieza de índices: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Este método hace la limpieza real de los índices problemáticos.
     * 
     * PASOS QUE HACE:
     * 1. Obtiene la lista de TODOS los índices que tiene MongoDB
     * 2. Revisa cada índice uno por uno
     * 3. Si el índice es problemático, lo elimina
     * 4. Si el índice es útil, lo deja
     * 5. Al final dice cuántos eliminó
     */
    private void limpiarIndicesProblematicos() {
        // 1. Obtener TODOS los índices de la colección "users"
        List<IndexInfo> indices = mongoTemplate.indexOps(COLLECTION_USERS).getIndexInfo();
    logger.info("Índices encontrados en colección '{}': {}", COLLECTION_USERS, indices.size());
        
        // Variable para contar cuántos índices eliminamos
        int eliminados = 0;
        
        // 2. Revisar cada índice uno por uno
        for (IndexInfo index : indices) {
            // Obtener el nombre del índice (ejemplo: "_id_", "email_1", etc.)
            String nombreIndice = index.getName();
            logger.info("  - {}: {}", nombreIndice, index.getIndexFields());
            
            // 3. Preguntar: ¿Este índice es problemático?
            if (esIndiceProblematico(nombreIndice)) {
                // SÍ es problemático → ELIMINARLO
                logger.warn("ELIMINANDO índice problemático: {}", nombreIndice);
                mongoTemplate.indexOps(COLLECTION_USERS).dropIndex(nombreIndice);
                logger.info("Índice eliminado exitosamente: {}", nombreIndice);
                eliminados++; // Contar que eliminamos uno más
            } else {
                // NO es problemático → MANTENERLO
                logger.info("Índice mantenido (útil): {}", nombreIndice);
            }
        }
        
        // 5. Reportar cuántos eliminamos en total
    logger.info("Total índices eliminados: {}", eliminados);
    }
    
    /**
     * Este método decide si un índice es problemático o no.
     * 
     * RECIBE: El nombre de un índice (ejemplo: "_id_", "email_1", "_codigos_recuperacion_._email")
     * DEVUELVE: 
     *   - true = "Este índice es problemático, hay que eliminarlo"
     *   - false = "Este índice está bien, hay que mantenerlo"
     * 
     * @param nombreIndice el nombre del índice a revisar
     * @return true si hay que eliminarlo, false si hay que mantenerlo
     */
    private boolean esIndiceProblematico(String nombreIndice) {
        
        // PASO 1: Revisar si es un índice IMPORTANTE que debemos MANTENER
        // Estos índices son útiles para que la aplicación funcione rápido
        if ("_id_".equals(nombreIndice) ||           // Índice principal de MongoDB
            nombreIndice.startsWith("email") ||      // Para buscar usuarios por email rápido
            nombreIndice.startsWith("nombre") ||     // Para buscar por nombre rápido
            nombreIndice.startsWith("apellidos")) {  // Para buscar por apellidos rápido
            
            return false; // NO es problemático, hay que MANTENERLO
        }

        // PASO 2: Revisar si es un índice PROBLEMÁTICO que debemos ELIMINAR
        // ACTUALIZACIÓN: Códigos de recuperación REACTIVADOS - Solo eliminar índices corruptos
        
        // Estos son artefactos problemáticos de MongoDB que siempre causan E11000:
        boolean tieneUsuarioSinNombre = nombreIndice.contains("_unnamed_Usuario_");
        boolean tieneIndiceCorrupto = nombreIndice.contains("_contrasenia._");
        
        if (tieneUsuarioSinNombre || tieneIndiceCorrupto) {
            // Estos índices son artefactos de referencias circulares mal resueltos
            return true; // SÍ es problemático, hay que ELIMINARLO
        }
        
        // NOTA: Ya NO eliminamos _codigos_recuperacion_ porque hemos resuelto las referencias circulares
        
        // PASO 3: Si llegamos aquí, el índice no es ni importante ni problemático
        // Por seguridad, lo mantenemos (mejor mantener algo útil que eliminar algo necesario)
        return false; // NO es problemático, hay que MANTENERLO
        
        // NOTA IMPORTANTE: No eliminamos sesions_token_ porque pueden ser útiles para el login
    }
}