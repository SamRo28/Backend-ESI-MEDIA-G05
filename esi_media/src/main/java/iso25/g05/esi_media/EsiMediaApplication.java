package iso25.g05.esi_media;

import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.VisualizadorRepository;
import iso25.g05.esi_media.service.VisualizadorService;
import iso25.g05.esi_media.model.VisualizadorRegistroDTO;
import iso25.g05.esi_media.service.RegistroResultado;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Clase principal de la aplicación con prueba de MongoDB integrada.
 * 
 * PRUEBA AUTOMÁTICA AL INICIAR:
 * - Verifica conectividad con MongoDB Atlas
 * - Crea un visualizador de prueba
 * - Lista todos los visualizadores
 * - Muestra estadísticas
 */
@SpringBootApplication
public class EsiMediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsiMediaApplication.class, args);
    }
    
    /**
     * Prueba automática de MongoDB al iniciar la aplicación
     * 
     * CommandLineRunner se ejecuta después de que Spring Boot termine de cargar
     * Útil para verificar que todo funciona correctamente
     */
    @Bean
    CommandLineRunner testMongoConnection(VisualizadorService visualizadorService,
                                         VisualizadorRepository visualizadorRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(EsiMediaApplication.class);
            logger.info("\n{}", "=".repeat(60));
            logger.info("INICIANDO PRUEBA DE MONGODB");
            logger.info("{}", "=".repeat(60));
            
            try {
                // PRUEBA 1: Verificar conexión básica
                logger.info("\nPRUEBA 1: Verificar conexión básica...");
                long count = visualizadorRepository.count();
                logger.info("Conexión exitosa. Visualizadores existentes: {}", count);
                
                // PRUEBA 2: Listar visualizadores existentes
                logger.info("\nPRUEBA 2: Listar visualizadores existentes...");
                List<Visualizador> existentes = visualizadorService.obtenerTodosLosVisualizadores();
                if (existentes.isEmpty()) {
                    logger.info("No hay visualizadores en la base de datos");
                } else {
                    logger.info("Visualizadores encontrados:");
                    for (Visualizador v : existentes) {
                        logger.info("   - {} ({}) - VIP: {}", v.getNombre(), v.getEmail(), v.isVip());
                    }
                }
                
                // PRUEBA 3: Crear un visualizador de prueba
                logger.info("\nPRUEBA 3: Crear visualizador de prueba...");
                VisualizadorRegistroDTO dtoTest = new VisualizadorRegistroDTO();
                dtoTest.setNombre("Usuario Test");
                dtoTest.setApellidos("MongoDB");
                dtoTest.setEmail("test.mongodb@esimedia.com");
                dtoTest.setAlias("TestUser");
                dtoTest.setContrasenia("TestPass123!");
                dtoTest.setConfirmacionContrasenia("TestPass123!");
                dtoTest.setFechaNac(new Date(System.currentTimeMillis() - (25L * 365 * 24 * 60 * 60 * 1000))); // 25 años
                dtoTest.setVip(true);
                
                RegistroResultado resultado = visualizadorService.registrarVisualizador(dtoTest);
                
                if (resultado.isExitoso()) {
                    logger.info("Visualizador de prueba creado exitosamente:");
                    logger.info("   - ID: {}", resultado.getVisualizador().getId());
                    logger.info("   - Email: {}", resultado.getVisualizador().getEmail());
                    logger.info("   - VIP: {}", resultado.getVisualizador().isVip());
                } else {
                    logger.info("Visualizador de prueba no creado (probablemente ya existe):");
                    resultado.getErrores().forEach(error -> logger.info("   - {}", error));
                }
                
                // PRUEBA 4: Estadísticas finales
                logger.info("\nPRUEBA 4: Estadísticas finales...");
                long totalFinal = visualizadorService.obtenerTodosLosVisualizadores().size();
                long totalVips = visualizadorService.contarVisualizadoresVip();
                logger.info("Total visualizadores: {}", totalFinal);
                logger.info("Visualizadores VIP: {}", totalVips);
                logger.info("Visualizadores regulares: {}", (totalFinal - totalVips));
                
                logger.info("\n{}", "=".repeat(60));
                logger.info("PRUEBA MONGODB COMPLETADA EXITOSAMENTE");
                logger.info("Servidor REST iniciado en: http://localhost:8080");
                logger.info("Endpoints de prueba disponibles:");
                logger.info("   - GET  http://localhost:8080/api/test/ping");
                logger.info("   - GET  http://localhost:8080/api/test/stats");
                logger.info("   - GET  http://localhost:8080/api/test/visualizadores");
                logger.info("   - POST http://localhost:8080/api/visualizador/registro");
                logger.info("{}\n", "=".repeat(60));
                
            } catch (Exception e) {
                logger.error("ERROR EN PRUEBA MONGODB: {}", e.getMessage(), e);
                logger.error("POSIBLES SOLUCIONES:");
                logger.error("   1. Verificar conexión a internet");
                logger.error("   2. Verificar credenciales en application.properties");
                logger.error("   3. Verificar que MongoDB Atlas esté activo");
            }
        };
    }
}
