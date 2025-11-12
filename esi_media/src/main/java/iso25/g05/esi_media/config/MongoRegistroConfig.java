package iso25.g05.esi_media.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Configuración de MongoDB específica para el sistema de registro.
 * 
 * PROPÓSITO:
 * - Activa solo los repositorios necesarios para el registro de usuarios
 * - Evita problemas con repositorios existentes que tienen consultas mal formadas
 * - Permite que la aplicación arranque y funcione para el sistema de registro
 * - FUERZA la conexión a MongoDB Atlas
 * 
 * REPOSITORIOS ACTIVOS:
 * - UsuarioRepository: Para operaciones básicas de usuario
 * - VisualizadorRepository: Para operaciones específicas de visualizadores
 * 
 * NOTA: Una vez que todos los repositorios estén corregidos, se puede usar
 * la configuración completa MongoConfig.java
 */
@Configuration
@Profile("!test")  // Esta configuración no se aplicará en el perfil de prueba
@EnableMongoRepositories(
    basePackages = "iso25.g05.esi_media.repository",
    includeFilters = @org.springframework.context.annotation.ComponentScan.Filter(
        type = org.springframework.context.annotation.FilterType.REGEX,
        pattern = ".*(Usuario|Visualizador|Administrador|GestorDeContenido|Contrasenia|Token|CodigoRecuperacion|Audio|Video|Contenido|Lista|ContraseniaComun|IpLoginAttempt|LogEntry)Repository"
    )
)
public class MongoRegistroConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "esimedia";
    }

    @Override
    protected boolean autoIndexCreation() {
        return false;
    }
    
    /**
     * Configuración EXPLÍCITA del cliente MongoDB para conectar a Atlas
     * Sobrescribe cualquier configuración automática que apunte a localhost
     */
    @Bean
    @Primary
    @Override
    public MongoClient mongoClient() {
        String connectionString = "mongodb+srv://adminESIMedia:2uouFFser4ZXM28E@esimediacluster.qtbnyf4.mongodb.net/esimedia";
        return MongoClients.create(connectionString);
    }
}
