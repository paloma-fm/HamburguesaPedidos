package com.proyectoduoc.hamburgueseria_pedidos.service;

import com.proyectoduoc.hamburgueseria_pedidos.model.PedidoModel;
import com.proyectoduoc.hamburgueseria_pedidos.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias - PedidoService")
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pedidoService, "notificacionesUrl", "http://localhost:8082");
    }

    @Test
    @DisplayName("guardarPedido: debe guardar y retornar el pedido correctamente")
    void guardarPedido_deberiaGuardarYRetornarPedidoCorrectamente() {
        PedidoModel pedido = new PedidoModel(null, "Juan Pérez", "Hamburguesa Clásica", "juan@test.com");
        PedidoModel guardado = new PedidoModel(1L, "Juan Pérez", "Hamburguesa Clásica", "juan@test.com");
        when(pedidoRepository.save(any(PedidoModel.class))).thenReturn(guardado);

        PedidoModel resultado = pedidoService.guardarPedido(pedido);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan Pérez", resultado.getCliente());
        assertEquals("Hamburguesa Clásica", resultado.getHamburguesa());
        assertEquals("juan@test.com", resultado.getCorreoCliente());
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    @DisplayName("guardarPedido: debe completarse aunque el servicio de notificaciones falle")
    void guardarPedido_deberiaCompletarseCuandoNotificacionFalla() {
        PedidoModel pedido = new PedidoModel(null, "María López", "Hamburguesa BBQ", "maria@test.com");
        PedidoModel guardado = new PedidoModel(2L, "María López", "Hamburguesa BBQ", "maria@test.com");
        when(pedidoRepository.save(any(PedidoModel.class))).thenReturn(guardado);
        when(restTemplate.postForObject(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("Microservicio de notificaciones no disponible"));

        PedidoModel resultado = pedidoService.guardarPedido(pedido);

        assertNotNull(resultado);
        assertEquals("María López", resultado.getCliente());
        verify(pedidoRepository, times(1)).save(pedido);
    }

    @Test
    @DisplayName("obtenerTodos: debe retornar la lista completa de pedidos")
    void obtenerTodos_deberiaRetornarListaCompletaDePedidos() {
        List<PedidoModel> pedidos = List.of(
                new PedidoModel(1L, "Juan Pérez", "Clásica", "juan@test.com"),
                new PedidoModel(2L, "María López", "BBQ", "maria@test.com"),
                new PedidoModel(3L, "Carlos Ruiz", "Vegana", "carlos@test.com")
        );
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        List<PedidoModel> resultado = pedidoService.obtenerTodos();

        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("Juan Pérez", resultado.get(0).getCliente());
        assertEquals("María López", resultado.get(1).getCliente());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("obtenerTodos: debe retornar lista vacía cuando no hay pedidos")
    void obtenerTodos_deberiaRetornarListaVaciaCuandoNoHayPedidos() {
        when(pedidoRepository.findAll()).thenReturn(List.of());

        List<PedidoModel> resultado = pedidoService.obtenerTodos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(pedidoRepository, times(1)).findAll();
    }
}
