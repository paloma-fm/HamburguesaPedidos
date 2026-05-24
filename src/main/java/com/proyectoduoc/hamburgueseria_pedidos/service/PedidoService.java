package com.proyectoduoc.hamburgueseria_pedidos.service;

import com.proyectoduoc.hamburgueseria_pedidos.model.PedidoModel;
import com.proyectoduoc.hamburgueseria_pedidos.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${notificaciones.url:http://localhost:8082}")
    private String notificacionesUrl;

    public PedidoModel guardarPedido(PedidoModel pedido) {
        PedidoModel pedidoGuardado = pedidoRepository.save(pedido);

        String urlNotificacion = notificacionesUrl + "/api/notificaciones";

        try {
            restTemplate.postForObject(urlNotificacion, pedidoGuardado, String.class);
        } catch (Exception e) {
            System.out.println("Aviso: No se envió la notificación porque el otro microservicio está apagado.");
        }

        return pedidoGuardado;
    }

    public List<PedidoModel> obtenerTodos() {
        return pedidoRepository.findAll();
    }
}
