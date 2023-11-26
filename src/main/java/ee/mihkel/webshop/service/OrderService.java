package ee.mihkel.webshop.service;

import ee.mihkel.webshop.dto.everypay.EverypayData;
import ee.mihkel.webshop.dto.everypay.EverypayResponse;
import ee.mihkel.webshop.entity.Order;
import ee.mihkel.webshop.entity.OrderRow;
import ee.mihkel.webshop.entity.Person;
import ee.mihkel.webshop.entity.Product;
import ee.mihkel.webshop.repository.OrderRepository;
import ee.mihkel.webshop.repository.PersonRepository;
import ee.mihkel.webshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    PersonRepository personRepository;

    @Autowired
    OrderRepository orderRepository;

    @Value("${everypay.url}")
    String url;

    @Value("${everypay.token}")
    String token;

    @Value("${everypay.customer-url}")
    String customerUrl;

    @Value("${everypay.username}")
    String username;

    @Value("${everypay.account-name}")
    String accountName;

    public Long saveOrderToDb(double totalSum, List<OrderRow> orderRows, Long personId) {
        if (personRepository.findById(personId).isEmpty()) {
            throw new NoSuchElementException("Person not found");
        }

        Person person = personRepository.findById(personId).get();

        Order order = new Order();
        order.setPaymentState("initial");
        order.setPerson(person);
        order.setOrderRow(orderRows);
        order.setCreationDate(new Date());
        order.setTotalSum(totalSum);
        Order newOrder = orderRepository.save(order);
        return newOrder.getId();
    }

    public double getTotalSum(List<OrderRow> orderRows) throws Exception {
        double totalSum = 0;
        for (OrderRow o : orderRows) {
            Optional<Product> productOptional = productRepository.findById(o.getProduct().getId());
            if (productOptional.isEmpty()) {
                throw new NoSuchElementException("Product not found");
            }
            Product product = productOptional.orElseThrow();
            decreaseStock(o, product);
            totalSum += product.getPrice();
        }
        return totalSum;
    }

    private void decreaseStock(OrderRow o, Product product) throws Exception {
        if (product.getStock() < o.getQuantity()) {
            throw new Exception("Not enough in stock: " + product.getName() + ", id: " + product.getId());
        }
        product.setStock( product.getStock() - o.getQuantity() );
        productRepository.save(product);
    }

    public String makePayment(double totalSum, Long id) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + token);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        EverypayData body = new EverypayData();
        body.setApi_username(username);
        body.setAccount_name(accountName);
        body.setAmount(totalSum);
        body.setOrder_reference(id.toString());
        body.setNonce("adasdsad3121" + ZonedDateTime.now() + Math.random());
        body.setTimestamp(ZonedDateTime.now().toString());
        body.setCustomer_url(customerUrl);

        HttpEntity<EverypayData> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<EverypayResponse> response = restTemplate.exchange(url + "/payments/oneoff", HttpMethod.POST, httpEntity, EverypayResponse.class);
        return response.getBody().getPayment_link();

    }
}
