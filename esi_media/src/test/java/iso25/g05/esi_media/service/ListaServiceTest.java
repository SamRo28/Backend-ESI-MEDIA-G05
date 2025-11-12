package iso25.g05.esi_media.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import iso25.g05.esi_media.dto.PlaylistDto;
import iso25.g05.esi_media.model.Audio;
import iso25.g05.esi_media.model.Contenido;
import iso25.g05.esi_media.model.GestordeContenido;
import iso25.g05.esi_media.model.Lista;
import iso25.g05.esi_media.model.Token;
import iso25.g05.esi_media.model.Usuario;
import iso25.g05.esi_media.model.Visualizador;
import iso25.g05.esi_media.repository.ContenidoRepository;
import iso25.g05.esi_media.repository.GestorDeContenidoRepository;
import iso25.g05.esi_media.repository.ListaRepository;
import iso25.g05.esi_media.repository.UsuarioRepository;
import iso25.g05.esi_media.repository.VisualizadorRepository;

/**
 * Tests unitarios para ListaService
 * Usa Mockito para simular las dependencias y verificar el comportamiento del servicio
 */
@ExtendWith(MockitoExtension.class)
class ListaServiceTest {

    @Mock
    private ListaRepository listaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ContenidoRepository contenidoRepository;

    @Mock
    private GestorDeContenidoRepository gestorRepository;

    @Mock
    private VisualizadorRepository visualizadorRepository;

    @InjectMocks
    private ListaService listaService;

    private GestordeContenido gestor;
    private Visualizador visualizador;
    private Token tokenValido;
    private Lista listaValida;
    private Contenido contenido1;
    private Contenido contenido2;
    private static final String TOKEN_VALIDO = "token-valido-123";
    private static final String TOKEN_INVALIDO = "token-invalido-456";
    private static final String ID_GESTOR = "gestor-id-1";
    private static final String ID_VISUALIZADOR = "visualizador-id-1";
    private static final String ID_LISTA = "lista-id-1";
    private static final String ID_CONTENIDO_1 = "contenido-id-1";
    private static final String ID_CONTENIDO_2 = "contenido-id-2";

    @BeforeEach
    void setUp() {
        // Configurar token válido con fecha de expiración futura
        tokenValido = new Token();
        tokenValido.setToken(TOKEN_VALIDO);
        tokenValido.setExpirado(false);
        Calendar futuro = Calendar.getInstance();
        futuro.add(Calendar.HOUR, 24);
        tokenValido.setFechaExpiracion(futuro.getTime());

        // Configurar gestor de contenido
        gestor = new GestordeContenido();
        gestor.setId(ID_GESTOR);
        gestor.setNombre("Gestor Test");
        gestor.setEmail("gestor@test.com");
        gestor.setcampoespecializacion("Música");
        gestor.setSesionstoken(tokenValido);

        // Configurar visualizador
        visualizador = new Visualizador();
        visualizador.setId(ID_VISUALIZADOR);
        visualizador.setNombre("Visualizador Test");
        visualizador.setEmail("visu@test.com");
        Calendar fechaNacimiento = Calendar.getInstance();
        fechaNacimiento.set(2000, Calendar.JANUARY, 1);
        visualizador.setFechaNac(fechaNacimiento.getTime());
        visualizador.setVip(false);
        visualizador.setSesionstoken(tokenValido);

        // Configurar lista válida
        listaValida = new Lista();
        listaValida.setId(ID_LISTA);
        listaValida.setNombre("Mi Lista de Rock");
        listaValida.setDescripcion("Una lista con las mejores canciones de rock");
        listaValida.setCreadorId(ID_GESTOR);
        listaValida.setUsuario(gestor);
        listaValida.setVisible(true);
        listaValida.setTags(new HashSet<>(Arrays.asList("rock", "classic")));
        listaValida.setEspecializacionGestor("Música");
        listaValida.setFechaCreacion(LocalDateTime.now());
        listaValida.setFechaActualizacion(LocalDateTime.now());

        // Configurar contenidos
        contenido1 = new Audio();
        contenido1.setId(ID_CONTENIDO_1);
        contenido1.settitulo("Bohemian Rhapsody");
        contenido1.setvip(false);
        contenido1.setedadvisualizacion(13);

        contenido2 = new Audio();
        contenido2.setId(ID_CONTENIDO_2);
        contenido2.settitulo("Stairway to Heaven");
        contenido2.setvip(false);
        contenido2.setedadvisualizacion(13);
    }

    // ==================== TEST CASO 1: Crear lista válida ====================

    @Test
    void testCrearListaValida_RetornaListaGuardada() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Nueva Lista");
        inputLista.setDescripcion("Descripción de la nueva lista");
        inputLista.setVisible(true);
        inputLista.setTags(new HashSet<>(Arrays.asList("pop", "indie")));

        Lista listaGuardada = new Lista();
        listaGuardada.setId("nueva-lista-id");
        listaGuardada.setNombre(inputLista.getNombre());
        listaGuardada.setDescripcion(inputLista.getDescripcion());
        listaGuardada.setCreadorId(ID_GESTOR);
        listaGuardada.setUsuario(gestor);
        listaGuardada.setVisible(true);
        listaGuardada.setTags(inputLista.getTags());
        listaGuardada.setEspecializacionGestor("Música");
        listaGuardada.setFechaCreacion(LocalDateTime.now());
        listaGuardada.setFechaActualizacion(LocalDateTime.now());

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(gestorRepository.findById(ID_GESTOR)).thenReturn(Optional.of(gestor));
        when(listaRepository.save(any(Lista.class))).thenReturn(listaGuardada);

        // Act
        PlaylistDto resultado = listaService.createLista(inputLista, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertEquals("nueva-lista-id", resultado.getId());
        assertEquals("Nueva Lista", resultado.getNombre());
        assertEquals("Descripción de la nueva lista", resultado.getDescripcion());
        assertEquals(ID_GESTOR, resultado.getCreadorId());
        assertTrue(resultado.isVisible());
        assertEquals("Música", resultado.getEspecializacionGestor());
        
        verify(usuarioRepository).findBySesionToken(TOKEN_VALIDO);
        verify(gestorRepository).findById(ID_GESTOR);
        verify(listaRepository).save(any(Lista.class));
    }

    // ==================== TEST CASO 2: Crear lista sin token ====================

    @Test
    void testCrearListaSinToken_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Lista sin token");
        inputLista.setDescripcion("Esta lista no debería crearse");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, null);
        });

        assertEquals("Token no proporcionado", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testCrearListaConTokenInvalido_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Lista con token inválido");
        inputLista.setDescripcion("Esta lista no debería crearse");

        when(usuarioRepository.findBySesionToken(TOKEN_INVALIDO)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, TOKEN_INVALIDO);
        });

        assertEquals("Token inválido", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    // ==================== TEST CASO 3: Editar lista propia ====================

    @Test
    void testEditarListaPropia_SeActualizaCorrectamente() {
        // Arrange
        Lista datosActualizados = new Lista();
        datosActualizados.setNombre("Lista Actualizada");
        datosActualizados.setDescripcion("Descripción actualizada");
        datosActualizados.setVisible(false);
        datosActualizados.setTags(new HashSet<>(Arrays.asList("rock", "metal")));

        Lista listaActualizada = new Lista();
        listaActualizada.setId(ID_LISTA);
        listaActualizada.setNombre("Lista Actualizada");
        listaActualizada.setDescripcion("Descripción actualizada");
        listaActualizada.setCreadorId(ID_GESTOR);
        listaActualizada.setUsuario(gestor);
        listaActualizada.setVisible(false);
        listaActualizada.setTags(new HashSet<>(Arrays.asList("rock", "metal")));
        listaActualizada.setEspecializacionGestor("Música");
        listaActualizada.setFechaCreacion(listaValida.getFechaCreacion());
        listaActualizada.setFechaActualizacion(LocalDateTime.now());

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(gestorRepository.findById(ID_GESTOR)).thenReturn(Optional.of(gestor));
        when(listaRepository.save(any(Lista.class))).thenReturn(listaActualizada);

        // Act
        PlaylistDto resultado = listaService.updateLista(ID_LISTA, datosActualizados, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertEquals("Lista Actualizada", resultado.getNombre());
        assertEquals("Descripción actualizada", resultado.getDescripcion());
        assertFalse(resultado.isVisible());
        assertTrue(resultado.getTags().contains("metal"));
        
        verify(usuarioRepository).findBySesionToken(TOKEN_VALIDO);
        verify(listaRepository).findById(ID_LISTA);
        verify(listaRepository).save(any(Lista.class));
    }

    // ==================== TEST CASO 4: Editar lista ajena ====================

    @Test
    void testEditarListaAjena_LanzaExcepcion() {
        // Arrange - Visualizador intenta editar lista del gestor
        Lista datosActualizados = new Lista();
        datosActualizados.setNombre("Intento de edición");
        datosActualizados.setDescripcion("No debería funcionar");

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(visualizador));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.updateLista(ID_LISTA, datosActualizados, TOKEN_VALIDO);
        });

        assertEquals("No tienes permisos para editar esta lista", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    // ==================== TEST CASO 5: Eliminar lista propia ====================

    @Test
    void testEliminarListaPropia_EliminaConExito() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        doNothing().when(listaRepository).deleteById(ID_LISTA);

        // Act
        assertDoesNotThrow(() -> {
            listaService.deleteLista(ID_LISTA, TOKEN_VALIDO);
        });

        // Assert
        verify(usuarioRepository).findBySesionToken(TOKEN_VALIDO);
        verify(listaRepository).findById(ID_LISTA);
        verify(listaRepository).deleteById(ID_LISTA);
    }

    // ==================== TEST CASO 6: Eliminar lista ajena ====================

    @Test
    void testEliminarListaAjena_LanzaExcepcion() {
        // Arrange - Visualizador intenta eliminar lista del gestor
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(visualizador));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.deleteLista(ID_LISTA, TOKEN_VALIDO);
        });

        assertEquals("No tienes permisos para eliminar esta lista", exception.getMessage());
        verify(listaRepository, never()).deleteById(any());
    }

    // ==================== TEST CASO 7: Añadir contenido duplicado ====================

    @Test
    void testAñadirContenidoDuplicado_LanzaExcepcion() {
        // Arrange - La lista ya tiene el contenido1
        listaValida.addContenido(contenido1);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(contenidoRepository.findById(ID_CONTENIDO_1)).thenReturn(Optional.of(contenido1));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.addContenido(ID_LISTA, ID_CONTENIDO_1, TOKEN_VALIDO);
        });

        assertEquals("El contenido ya está en la lista", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testAñadirContenidoNuevo_SeAgregaCorrectamente() {
        // Arrange - La lista está vacía
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(contenidoRepository.findById(ID_CONTENIDO_1)).thenReturn(Optional.of(contenido1));
        when(listaRepository.save(any(Lista.class))).thenReturn(listaValida);

        // Act
        PlaylistDto resultado = listaService.addContenido(ID_LISTA, ID_CONTENIDO_1, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.getContenidosIds().contains(ID_CONTENIDO_1));
        verify(listaRepository).save(any(Lista.class));
    }

    // ==================== TEST CASO 8: Eliminar contenido dejando lista vacía ====================

    @Test
    void testEliminarUnicoContenido_LanzaExcepcion() {
        // Arrange - La lista tiene solo 1 contenido
        listaValida.addContenido(contenido1);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removeContenido(ID_LISTA, ID_CONTENIDO_1, TOKEN_VALIDO);
        });

        assertTrue(exception.getMessage().contains("al menos un contenido") || 
                   exception.getMessage().contains("debe tener al menos"));
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testEliminarContenidoDejandoOtros_SeEliminaCorrectamente() {
        // Arrange - La lista tiene 2 contenidos
        listaValida.addContenido(contenido1);
        listaValida.addContenido(contenido2);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(listaRepository.save(any(Lista.class))).thenReturn(listaValida);

        // Act
        PlaylistDto resultado = listaService.removeContenido(ID_LISTA, ID_CONTENIDO_1, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.getContenidosIds().contains(ID_CONTENIDO_1));
        assertTrue(resultado.getContenidosIds().contains(ID_CONTENIDO_2));
        verify(listaRepository).save(any(Lista.class));
    }

    // ==================== TEST CASO 9: Obtener listas por usuario ====================

    @Test
    void testObtenerListasPorUsuario_DevuelveSoloLasSuyas() {
        // Arrange
        Lista lista2 = new Lista();
        lista2.setId("lista-id-2");
        lista2.setNombre("Segunda Lista");
        lista2.setDescripcion("Otra lista del gestor");
        lista2.setCreadorId(ID_GESTOR);
        lista2.setUsuario(gestor);
        lista2.setVisible(false);
        lista2.setFechaCreacion(LocalDateTime.now());
        lista2.setFechaActualizacion(LocalDateTime.now());

        List<Lista> listasDelGestor = Arrays.asList(listaValida, lista2);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findByCreadorId(ID_GESTOR)).thenReturn(listasDelGestor);

        // Act
        List<PlaylistDto> resultado = listaService.findListasPropias(TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(dto -> dto.getCreadorId().equals(ID_GESTOR)));
        assertEquals("Mi Lista de Rock", resultado.get(0).getNombre());
        assertEquals("Segunda Lista", resultado.get(1).getNombre());
        
        verify(usuarioRepository).findBySesionToken(TOKEN_VALIDO);
        verify(listaRepository).findByCreadorId(ID_GESTOR);
    }

    @Test
    void testObtenerListasPorUsuarioSinListas_DevuelveListaVacia() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findByCreadorId(ID_GESTOR)).thenReturn(Collections.emptyList());

        // Act
        List<PlaylistDto> resultado = listaService.findListasPropias(TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(listaRepository).findByCreadorId(ID_GESTOR);
    }

    // ==================== TESTS ADICIONALES PARA COBERTURA ====================

    @Test
    void testCrearListaVisualizador_FuerzaVisibleFalse() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Lista Privada");
        inputLista.setDescripcion("Lista de visualizador");
        inputLista.setVisible(true); // Intenta hacer pública

        Lista listaGuardada = new Lista();
        listaGuardada.setId("lista-visu-id");
        listaGuardada.setNombre(inputLista.getNombre());
        listaGuardada.setDescripcion(inputLista.getDescripcion());
        listaGuardada.setCreadorId(ID_VISUALIZADOR);
        listaGuardada.setUsuario(visualizador);
        listaGuardada.setVisible(false); // Sistema fuerza a false
        listaGuardada.setFechaCreacion(LocalDateTime.now());
        listaGuardada.setFechaActualizacion(LocalDateTime.now());

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(visualizador));
        when(gestorRepository.findById(ID_VISUALIZADOR)).thenReturn(Optional.empty());
        when(visualizadorRepository.findById(ID_VISUALIZADOR)).thenReturn(Optional.of(visualizador));
        when(listaRepository.save(any(Lista.class))).thenReturn(listaGuardada);

        // Act
        PlaylistDto resultado = listaService.createLista(inputLista, TOKEN_VALIDO);

        // Assert
        assertNotNull(resultado);
        assertFalse(resultado.isVisible()); // Forzado a false por reglas de negocio
        assertNull(resultado.getEspecializacionGestor()); // Visualizadores no tienen especialización
        
        verify(visualizadorRepository).findById(ID_VISUALIZADOR);
    }

    @Test
    void testCrearListaSinNombre_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setDescripcion("Lista sin nombre");

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, TOKEN_VALIDO);
        });

        assertEquals("El nombre de la lista es obligatorio", exception.getMessage());
    }

    @Test
    void testCrearListaSinDescripcion_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Lista sin descripción");

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, TOKEN_VALIDO);
        });

        assertEquals("La descripción de la lista es obligatoria", exception.getMessage());
    }

    @Test
    void testEditarListaInexistente_LanzaExcepcion() {
        // Arrange
        Lista datosActualizados = new Lista();
        datosActualizados.setNombre("Lista Inexistente");

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById("lista-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.updateLista("lista-inexistente", datosActualizados, TOKEN_VALIDO);
        });

        assertEquals("Lista no encontrada", exception.getMessage());
    }

    @Test
    void testAñadirContenidoInexistente_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(contenidoRepository.findById("contenido-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.addContenido(ID_LISTA, "contenido-inexistente", TOKEN_VALIDO);
        });

        assertEquals("Contenido no encontrado", exception.getMessage());
    }

    @Test
    void testEliminarContenidoInexistenteEnLista_LanzaExcepcion() {
        // Arrange - Lista con 2 contenidos para poder eliminar sin violar la regla de mínimo 1
        listaValida.addContenido(contenido1);
        listaValida.addContenido(contenido2);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert - Intentamos eliminar un contenido que no está en la lista
        String idContenidoInexistente = "contenido-inexistente-xyz";
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removeContenido(ID_LISTA, idContenidoInexistente, TOKEN_VALIDO);
        });

        assertEquals("El contenido no está en la lista", exception.getMessage());
    }

    // ==================== TESTS ADICIONALES PARA COBERTURA COMPLETA DE RAMAS ====================

    @Test
    void testTokenExpirado_LanzaExcepcion() {
        // Arrange - Token con fecha de expiración pasada
        Token tokenExpirado = new Token();
        tokenExpirado.setToken("token-expirado");
        tokenExpirado.setExpirado(false);
        Calendar pasado = Calendar.getInstance();
        pasado.add(Calendar.HOUR, -24); // Hace 24 horas
        tokenExpirado.setFechaExpiracion(pasado.getTime());

        Usuario usuarioConTokenExpirado = new GestordeContenido();
        usuarioConTokenExpirado.setId("usuario-test");

        usuarioConTokenExpirado.setSesionstoken(tokenExpirado);

        when(usuarioRepository.findBySesionToken("token-expirado"))
            .thenReturn(Optional.of(usuarioConTokenExpirado));

        Lista inputLista = new Lista();
        inputLista.setNombre("Lista Test");
        inputLista.setDescripcion("Descripción test");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, "token-expirado");
        });

        assertEquals("Token expirado o inválido", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testTokenMarcadoComoExpirado_LanzaExcepcion() {
        // Arrange - Token marcado como expirado aunque la fecha sea válida
        Token tokenMarcadoExpirado = new Token();
        tokenMarcadoExpirado.setToken("token-marcado-expirado");
        tokenMarcadoExpirado.setExpirado(true); // Marcado como expirado
        Calendar futuro = Calendar.getInstance();
        futuro.add(Calendar.HOUR, 24);
        tokenMarcadoExpirado.setFechaExpiracion(futuro.getTime());

        Usuario usuarioConTokenMarcado = new GestordeContenido();
        usuarioConTokenMarcado.setId("usuario-test");
        usuarioConTokenMarcado.setSesionstoken(tokenMarcadoExpirado);

        when(usuarioRepository.findBySesionToken("token-marcado-expirado"))
            .thenReturn(Optional.of(usuarioConTokenMarcado));

        Lista inputLista = new Lista();
        inputLista.setNombre("Lista Test");
        inputLista.setDescripcion("Descripción test");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, "token-marcado-expirado");
        });

        assertEquals("Token expirado o inválido", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testTokenVacio_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Lista Test");
        inputLista.setDescripcion("Descripción test");

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, "");
        });

        assertEquals("Token no proporcionado", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testCrearListaNull_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(null, TOKEN_VALIDO);
        });

        assertEquals("La lista no puede ser nula", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testEditarListaConDatosNull_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.updateLista(ID_LISTA, null, TOKEN_VALIDO);
        });

        assertEquals("Los datos de actualización no pueden ser nulos", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testEliminarListaInexistente_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById("lista-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.deleteLista("lista-inexistente", TOKEN_VALIDO);
        });

        assertEquals("Lista no encontrada", exception.getMessage());
        verify(listaRepository, never()).deleteById(any());
    }

    @Test
    void testAñadirContenidoAListaInexistente_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById("lista-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.addContenido("lista-inexistente", ID_CONTENIDO_1, TOKEN_VALIDO);
        });

        assertEquals("Lista no encontrada", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testEliminarContenidoDeListaInexistente_LanzaExcepcion() {
        // Arrange
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById("lista-inexistente")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removeContenido("lista-inexistente", ID_CONTENIDO_1, TOKEN_VALIDO);
        });

        assertEquals("Lista no encontrada", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testAñadirContenidoSinPermisos_LanzaExcepcion() {
        // Arrange - Visualizador intenta añadir contenido a lista del gestor
        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(visualizador));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.addContenido(ID_LISTA, ID_CONTENIDO_1, TOKEN_VALIDO);
        });

        assertEquals("No tienes permisos para modificar esta lista", exception.getMessage());
        verify(contenidoRepository, never()).findById(any());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testEliminarContenidoSinPermisos_LanzaExcepcion() {
        // Arrange - Visualizador intenta eliminar contenido de lista del gestor
        listaValida.addContenido(contenido1);
        listaValida.addContenido(contenido2);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(visualizador));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.removeContenido(ID_LISTA, ID_CONTENIDO_1, TOKEN_VALIDO);
        });

        assertEquals("No tienes permisos para modificar esta lista", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testCrearListaConNombreVacio_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("   "); // Solo espacios
        inputLista.setDescripcion("Descripción válida");

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, TOKEN_VALIDO);
        });

        assertEquals("El nombre de la lista es obligatorio", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testCrearListaConDescripcionVacia_LanzaExcepcion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Nombre válido");
        inputLista.setDescripcion("   "); // Solo espacios

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            listaService.createLista(inputLista, TOKEN_VALIDO);
        });

        assertEquals("La descripción de la lista es obligatoria", exception.getMessage());
        verify(listaRepository, never()).save(any(Lista.class));
    }

    @Test
    void testEditarListaConNombreVacio_NoActualizaNombre() {
        // Arrange
        String nombreOriginal = listaValida.getNombre();
        
        Lista datosActualizados = new Lista();
        datosActualizados.setNombre("   "); // Solo espacios
        datosActualizados.setDescripcion("Nueva descripción");
        datosActualizados.setVisible(true);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(gestorRepository.findById(ID_GESTOR)).thenReturn(Optional.of(gestor));
        when(listaRepository.save(any(Lista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PlaylistDto resultado = listaService.updateLista(ID_LISTA, datosActualizados, TOKEN_VALIDO);

        // Assert
        assertEquals(nombreOriginal, resultado.getNombre()); // Nombre no cambió
        assertEquals("Nueva descripción", resultado.getDescripcion()); // Descripción sí cambió
        verify(listaRepository).save(any(Lista.class));
    }

    @Test
    void testEditarListaConDescripcionVacia_NoActualizaDescripcion() {
        // Arrange
        String descripcionOriginal = listaValida.getDescripcion();
        
        Lista datosActualizados = new Lista();
        datosActualizados.setNombre("Nuevo nombre");
        datosActualizados.setDescripcion("   "); // Solo espacios
        datosActualizados.setVisible(false);

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(listaRepository.findById(ID_LISTA)).thenReturn(Optional.of(listaValida));
        when(gestorRepository.findById(ID_GESTOR)).thenReturn(Optional.of(gestor));
        when(listaRepository.save(any(Lista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        PlaylistDto resultado = listaService.updateLista(ID_LISTA, datosActualizados, TOKEN_VALIDO);

        // Assert
        assertEquals("Nuevo nombre", resultado.getNombre()); // Nombre sí cambió
        assertEquals(descripcionOriginal, resultado.getDescripcion()); // Descripción no cambió
        verify(listaRepository).save(any(Lista.class));
    }

    @Test
    void testCrearListaGestorSinEspecializacion_AsignaEspecializacion() {
        // Arrange
        Lista inputLista = new Lista();
        inputLista.setNombre("Lista Test");
        inputLista.setDescripcion("Descripción test");
        inputLista.setVisible(true);
        inputLista.setEspecializacionGestor(null); // No especifica especialización

        Lista listaGuardada = new Lista();
        listaGuardada.setId("nueva-lista");
        listaGuardada.setNombre(inputLista.getNombre());
        listaGuardada.setDescripcion(inputLista.getDescripcion());
        listaGuardada.setCreadorId(ID_GESTOR);
        listaGuardada.setVisible(true);
        listaGuardada.setEspecializacionGestor("Música"); // Sistema asigna la del gestor
        listaGuardada.setFechaCreacion(LocalDateTime.now());
        listaGuardada.setFechaActualizacion(LocalDateTime.now());

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(gestor));
        when(gestorRepository.findById(ID_GESTOR)).thenReturn(Optional.of(gestor));
        when(listaRepository.save(any(Lista.class))).thenReturn(listaGuardada);

        // Act
        PlaylistDto resultado = listaService.createLista(inputLista, TOKEN_VALIDO);

        // Assert
        assertEquals("Música", resultado.getEspecializacionGestor());
        verify(gestorRepository).findById(ID_GESTOR);
    }

    @Test
    void testCrearListaUsuarioNoEsGestorNiVisualizador_FuerzaPrivada() {
        // Arrange - Crear un Usuario genérico (Administrador)
        Usuario admin = new Usuario();
        admin.setId("admin-id");
        admin.setNombre("Admin");
        admin.setEmail("admin@test.com");
        admin.setSesionstoken(tokenValido);

        Lista inputLista = new Lista();
        inputLista.setNombre("Lista Admin");
        inputLista.setDescripcion("Descripción admin");
        inputLista.setVisible(true); // Intenta pública

        Lista listaGuardada = new Lista();
        listaGuardada.setId("lista-admin");
        listaGuardada.setNombre(inputLista.getNombre());
        listaGuardada.setDescripcion(inputLista.getDescripcion());
        listaGuardada.setCreadorId(admin.getId());
        listaGuardada.setVisible(false); // Forzado a false
        listaGuardada.setEspecializacionGestor(null);
        listaGuardada.setFechaCreacion(LocalDateTime.now());
        listaGuardada.setFechaActualizacion(LocalDateTime.now());

        when(usuarioRepository.findBySesionToken(TOKEN_VALIDO)).thenReturn(Optional.of(admin));
        when(gestorRepository.findById(admin.getId())).thenReturn(Optional.empty());
        when(visualizadorRepository.findById(admin.getId())).thenReturn(Optional.empty());
        when(listaRepository.save(any(Lista.class))).thenReturn(listaGuardada);

        // Act
        PlaylistDto resultado = listaService.createLista(inputLista, TOKEN_VALIDO);

        // Assert
        assertFalse(resultado.isVisible()); // Forzado a false
        assertNull(resultado.getEspecializacionGestor());
        verify(gestorRepository).findById(admin.getId());
        verify(visualizadorRepository).findById(admin.getId());
    }
}
