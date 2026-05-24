package com.proyectoduoc.hamburgueseria_pedidos.controller;

import com.proyectoduoc.hamburgueseria_pedidos.model.PedidoModel;
import com.proyectoduoc.hamburgueseria_pedidos.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    public PedidoModel crear(@RequestBody PedidoModel pedido) {
        return pedidoService.guardarPedido(pedido);
    }

    @GetMapping
    public List<PedidoModel> listar() {
        return pedidoService.obtenerTodos();
    }
}