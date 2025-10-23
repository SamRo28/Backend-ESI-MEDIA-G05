package iso25.g05.esi_media.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import com.mongodb.client.MongoClients;

/**
 * Configuración específica para tests que asegura que todas las operaciones
 * MongoDB se realicen en la base de datos de pruebas.
 */
@TestConfiguration
@TestPropertySource(properties = {
    "spring.data.mongodb.database=esi_media_test",
    "spring.data.mongodb.uri=mongodb+srv://adminESIMedia:2uouFFser4ZXM28E@esimediacluster.qtbnyf4.mongodb.net/esi_media_test",
    "spring.profiles.active=test"
})
@Profile("test")
@ContextConfiguration(classes = MongoTestConfig.class)
public class MongoTestConfig {
    
    private static final String TEST_CONNECTION_STRING = 
        "mongodb+srv://adminESIMedia:2uouFFser4ZXM28E@esimediacluster.qtbnyf4.mongodb.net/esi_media_test";
    
    /**
     * Configura una fábrica de base de datos MongoDB para pruebas
     */
    @Bean
    @Primary
    public MongoDatabaseFactory mongoDatabaseFactory() {
        return new SimpleMongoClientDatabaseFactory(
                MongoClients.create(TEST_CONNECTION_STRING), "esi_media_test");
    }
    
    /**
     * Crea un MongoTemplate configurado específicamente para usar la base de datos de pruebas.
     * Esto asegura que todas las operaciones, incluidas las que usan @Document directamente,
     * utilicen la base de datos correcta.
     */
    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDatabaseFactory) {
        // Crear el convertidor personalizado para MongoDB
        MappingMongoConverter converter = new MappingMongoConverter(
                new DefaultDbRefResolver(mongoDatabaseFactory), 
                new MongoMappingContext());
        
        // Inicializar el convertidor
        converter.afterPropertiesSet();
        
        // Crear y devolver el MongoTemplate con el factory y el convertidor
        return new MongoTemplate(mongoDatabaseFactory, converter);
    }
}