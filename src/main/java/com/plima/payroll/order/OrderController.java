package com.plima.payroll.order;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.mediatype.vnderrors.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class OrderController {
  private final OrderRepository orderRepository;
  private final OrderModelAssembler assembler;

  public OrderController(OrderRepository orderRepository, OrderModelAssembler assembler) {
    this.orderRepository = orderRepository;
    this.assembler = assembler;
  }

  @GetMapping("/orders")
  CollectionModel<EntityModel<Order>> all() {
    List<EntityModel<Order>> orders = orderRepository.findAll().stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

    return new CollectionModel<>(orders,
        linkTo(methodOn(OrderController.class).all()).withSelfRel());
  }

  @PostMapping("/orders")
  ResponseEntity<EntityModel<Order>> newOrder(@RequestBody Order order) throws URISyntaxException {
    order.setStatus(Status.IN_PROGRESS);
    Order newOrder = orderRepository.save(order);

    return ResponseEntity
        .created(linkTo(methodOn(OrderController.class).newOrder(order)).toUri())
        .body(assembler.toModel(newOrder));
  }

  @GetMapping("/orders/{id}")
  EntityModel<Order> one(@PathVariable Long id) {
    return assembler.toModel(orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id)));
  }

  @DeleteMapping("/orders/{id}/cancel")
  ResponseEntity<?> cancel(@PathVariable Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));

    if (order.getStatus() == Status.IN_PROGRESS) {
      order.setStatus(Status.CANCELLED);
      return ResponseEntity.ok(assembler.toModel(orderRepository.save(order)));
    }

    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new VndErrors.VndError("Method not allowed", "You can't cancel an order that is in the " + order.getStatus() + " status"));
  }

  @PutMapping("/orders/{id}/complete")
  ResponseEntity<?> complete(@PathVariable Long id) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));

    if (order.getStatus() == Status.IN_PROGRESS) {
      order.setStatus(Status.COMPLETED);
      return ResponseEntity.ok(assembler.toModel(orderRepository.save(order)));
    }

    return ResponseEntity
        .status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(new VndErrors.VndError("Method not allowed", "You can't complete an order that is in the " + order.getStatus() + " status"));
  }
}
