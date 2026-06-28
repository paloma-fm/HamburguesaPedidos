package com.proyectoduoc.hamburgueseria_pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoduoc.hamburgueseria_pedidos.model.PedidoModel;
import com.proyectoduoc.hamburgueseria_pedidos.service.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PedidoController.class)
@DisplayName("Pruebas unitarias - PedidoController")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/pedidos: debe retornar el pedido guardado con status 200")
    void crear_deberiaRetornarPedidoGuardadoConStatus200() throws Exception {
        PedidoModel pedido = new PedidoModel(null, "Juan Pérez", "Hamburguesa Clásica", "juan@test.com");
        PedidoModel guardado = new PedidoModel(1L, "Juan Pérez", "Hamburguesa Clásica", "juan@test.com");
        when(pedidoService.guardarPedido(any(PedidoModel.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cliente").value("Juan Pérez"))
                .andExpect(jsonPath("$.hamburguesa").value("Hamburguesa Clásica"))
                .andExpect(jsonPath("$.correoCliente").value("juan@test.com"));
    }

    @Test
    @DisplayName("GET /api/pedidos: debe retornar lista con todos los pedidos")
    void listar_deberiaRetornarListaDePedidosConStatus200() throws Exception {
        List<PedidoModel> pedidos = List.of(
                new PedidoModel(1L, "Juan Pérez", "Clásica", "juan@test.com"),
                new PedidoModel(2L, "María López", "BBQ", "maria@test.com")
        );
        when(pedidoService.obtenerTodos()).thenReturn(pedidos);

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cliente").value("Juan Pérez"))
                .andExpect(jsonPath("$[1].cliente").value("María López"));
    }

    @Test
    @DisplayName("GET /api/pedidos: debe retornar lista vacía cuando no hay pedidos")
    void listar_deberiaRetornarListaVaciaCuandoNoHayPedidos() throws Exception {
        when(pedidoService.obtenerTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/pedidos: debe guardar pedido con todos los campos correctos")
    void crear_deberiaGuardarPedidoConTodosLosCampos() throws Exception {
        PedidoModel pedido = new PedidoModel(null, "Carlos Ruiz", "Hamburguesa Vegana", "carlos@test.com");
        PedidoModel guardado = new PedidoModel(3L, "Carlos Ruiz", "Hamburguesa Vegana", "carlos@test.com");
        when(pedidoService.guardarPedido(any(PedidoModel.class))).thenReturn(guardado);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.hamburguesa").value("Hamburguesa Vegana"))
                .andExpect(jsonPath("$.correoCliente").value("carlos@test.com"));
    }
}

