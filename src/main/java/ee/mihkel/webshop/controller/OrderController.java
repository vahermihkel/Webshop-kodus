package ee.mihkel.webshop.controller;

import ee.mihkel.webshop.entity.Order;
import ee.mihkel.webshop.entity.OrderRow;
import ee.mihkel.webshop.repository.OrderRepository;
import ee.mihkel.webshop.service.OrderService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderService orderService;

    @GetMapping("orders")
    public ResponseEntity<List<Order>> getOrders() {
        return new ResponseEntity<>(orderRepository.findAll(), HttpStatus.OK);
    }

    @PostMapping("orders")
    public ResponseEntity<String> addOrder(
            @RequestBody List<OrderRow> orderRows
    ) throws Exception {
        double totalSum = orderService.getTotalSum(orderRows);
        Long id = orderService.saveOrderToDb(totalSum, orderRows, 1L);
        String paymentUrl = orderService.makePayment(totalSum, id);
        return new ResponseEntity<>(paymentUrl, HttpStatus.CREATED);
    }

    @DeleteMapping("orders/{id}")
    public ResponseEntity<List<Order>> deleteOrder(@PathVariable Long id) {
        orderRepository.deleteById(id);
        return new ResponseEntity<>(orderRepository.findAll(), HttpStatus.OK);
    }

    @GetMapping("orders/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        return new ResponseEntity<>(orderRepository.findById(id).get(), HttpStatus.OK);
    }

    @PutMapping("orders/{id}")
    public List<Order> updateOrder(@PathVariable Long id, @RequestBody Order order) {
        if (orderRepository.existsById(id)) {
            order.setId(orderRepository.findById(id).get().getId());
            orderRepository.save(order);
        }
        return orderRepository.findAll();
    }

}
