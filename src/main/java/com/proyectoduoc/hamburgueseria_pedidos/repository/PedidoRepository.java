package com.proyectoduoc.hamburgueseria_pedidos.repository;
import com.proyectoduoc.hamburgueseria_pedidos.model.PedidoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

    @Repository
    public interface PedidoRepository extends JpaRepository<PedidoModel, Long> {
    
    }