package com.proyectoduoc.hamburgueseria_pedidos.repository;

import com.proyectoduoc.hamburgueseria_pedidos.model.PedidoModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("Pruebas de integración - PedidoRepository")
class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Test
    @DisplayName("save: debe persistir el pedido y generar un ID autoincremental")
    void save_deberiaGuardarPedidoYGenerarId() {
        PedidoModel pedido = new PedidoModel(null, "Pedro González", "Hamburguesa Doble", "pedro@test.com");

        PedidoModel guardado = pedidoRepository.save(pedido);

        assertNotNull(guardado.getId());
        assertTrue(guardado.getId() > 0);
        assertEquals("Pedro González", guardado.getCliente());
        assertEquals("Hamburguesa Doble", guardado.getHamburguesa());
        assertEquals("pedro@test.com", guardado.getCorreoCliente());
    }

    @Test
    @DisplayName("findAll: debe retornar todos los pedidos guardados")
    void findAll_deberiaRetornarTodosLosPedidosGuardados() {
        pedidoRepository.save(new PedidoModel(null, "Ana Torres", "Vegana", "ana@test.com"));
        pedidoRepository.save(new PedidoModel(null, "Luis Castro", "Picante", "luis@test.com"));
        pedidoRepository.save(new PedidoModel(null, "Sofia Vargas", "BBQ", "sofia@test.com"));

        List<PedidoModel> pedidos = pedidoRepository.findAll();

        assertEquals(3, pedidos.size());
    }

    @Test
    @DisplayName("findById: debe retornar el pedido correcto por ID")
    void findById_deberiaRetornarPedidoCorrecto() {
        PedidoModel guardado = pedidoRepository.save(
                new PedidoModel(null, "Roberto Mora", "Clásica", "roberto@test.com")
        );

        Optional<PedidoModel> encontrado = pedidoRepository.findById(guardado.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Roberto Mora", encontrado.get().getCliente());
        assertEquals("Clásica", encontrado.get().getHamburguesa());
    }

    @Test
    @DisplayName("deleteById: debe eliminar el pedido correctamente")
    void deleteById_deberiaEliminarPedidoCorrectamente() {
        PedidoModel guardado = pedidoRepository.save(
                new PedidoModel(null, "Elena Ríos", "Doble Queso", "elena@test.com")
        );
        Long id = guardado.getId();

        pedidoRepository.deleteById(id);

        Optional<PedidoModel> eliminado = pedidoRepository.findById(id);
        assertFalse(eliminado.isPresent());
    }

    @Test
    @DisplayName("findAll: debe retornar lista vacía cuando no hay pedidos")
    void findAll_deberiaRetornarListaVaciaSinPedidos() {
        List<PedidoModel> pedidos = pedidoRepository.findAll();

        assertNotNull(pedidos);
        assertTrue(pedidos.isEmpty());
    }
}
