package com.plima.payroll;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class EmployeeController {

  private final EmployeeRepository repository;

  private final EmployeeModelAssembler assembler;

  public EmployeeController(EmployeeRepository repository, EmployeeModelAssembler assembler) {
    this.repository = repository;
    this.assembler = assembler;
  }

  //Agregate root

  @GetMapping("/employees")
  CollectionModel<EntityModel<Employee>> all() {
    List<EntityModel<Employee>> employees = repository.findAll().stream()
        .map(assembler::toModel)
        .collect(Collectors.toList());

    return new CollectionModel<>(employees,
        linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
  }

  @PostMapping("/employees")
  ResponseEntity<EntityModel<Employee>> newEmployee(@RequestBody Employee newEmployee) throws URISyntaxException {
    EntityModel<Employee> model = assembler.toModel(repository.save(newEmployee));

    return ResponseEntity
        .created(linkTo(methodOn(EmployeeController.class).newEmployee(newEmployee)).toUri())
        .body(model);
  }

  // Single item
  @GetMapping("/employees/{id}")
  EntityModel<Employee> one(@PathVariable Long id) {

    Employee employee = repository.findById(id)
        .orElseThrow(() -> new EmployeeNotFoundException(id));

    return assembler.toModel(employee);
  }

  @PutMapping("/employees/{id}")
  ResponseEntity<EntityModel<Employee>> replaceEmployee(@RequestBody Employee newEmployee, @PathVariable Long id) {
    Employee updatedEmployee = repository.findById(id)
        .map(employee -> {
          employee.setName(newEmployee.getName());
          employee.setRole(newEmployee.getRole());
          return repository.save(employee);
        }).orElseGet(() -> {
          newEmployee.setId(id);
          return repository.save(newEmployee);
        });

    EntityModel<Employee> model = assembler.toModel(updatedEmployee);

    return ResponseEntity
        .created(linkTo(methodOn(EmployeeController.class).replaceEmployee(newEmployee, id)).toUri())
        .body(model);

  }

  @DeleteMapping("/employees/{id}")
  ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
    repository.deleteById(id);
    return ResponseEntity.noContent().build();
  }
}
