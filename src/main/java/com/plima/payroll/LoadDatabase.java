package com.plima.payroll;

import com.plima.payroll.employee.Employee;
import com.plima.payroll.employee.EmployeeRepository;
import com.plima.payroll.order.Order;
import com.plima.payroll.order.OrderRepository;
import com.plima.payroll.order.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LoadDatabase {

  @Bean
  CommandLineRunner initDatabase(EmployeeRepository repository, OrderRepository orderRepository) {
    return args -> {
      log.info("Preloading" + repository.save(new Employee("Bilbo", " Baggins", "Burglar")));
      log.info("Preloading" + repository.save(new Employee("Frodo", "Baggins", "thief")));

      orderRepository.save(new Order("MacBook Pro", Status.COMPLETED));
      orderRepository.save(new Order("iPhone", Status.IN_PROGRESS));
      orderRepository.findAll().forEach(order -> {
        log.info("Preloaded " + order);
      });
    };
  }
}
