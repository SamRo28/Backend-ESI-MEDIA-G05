package iso25.g05.esi_media.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

/**
 * Configuración personalizada para MongoDB.
 * 
 * PROPÓSITO:
 * - Configuración adicional de MongoDB más allá de application.properties
 * - Manejo de conexiones, índices, y optimizaciones
 * - Configuración de herencia para discriminadores automáticos
 * 
 * NOTA: Spring Boot maneja automáticamente la mayoría de la configuración
 * Esta clase es para configuraciones avanzadas si se necesitan en el futuro
 * 
 * TEMPORALMENTE DESACTIVADA: Para evitar conflictos con repositorios problemáticos
 */
@Configuration
@ConditionalOnProperty(name = "mongo.config.full", havingValue = "true")
public class MongoConfig extends AbstractMongoClientConfiguration {
    
    /**
     * Nombre de la base de datos (ya configurado en application.properties)
     * Este método es requerido por AbstractMongoClientConfiguration
     */
    @Override
    protected String getDatabaseName() {
        return "esimedia";
    }
    
    // Configuraciones adicionales se pueden agregar aquí según necesidades:
    // - Configuración de índices personalizados
    // - Configuración de serialización personalizada  
    // - Configuración de auditoria (fechas de creación/modificación automáticas)
    // - Configuración de transacciones si se necesitan
}