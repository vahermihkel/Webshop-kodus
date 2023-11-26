package ee.mihkel.webshop.controller;

import ee.mihkel.webshop.entity.Product;
import ee.mihkel.webshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
public class ProductController {

    @Autowired
    ProductRepository productRepository;

    @GetMapping("products")
    public List<Product> getProducts() {
        return productRepository.findAllByOrderById();
    }

    @GetMapping("public-products")
    public String getProductsByPage() {
        // TODO: Vt alt
        return "SIIA TEHKE LEHEKÜLGEDE KAUPA VÕTMINE TOODETE OSAS";
    }

    @PostMapping("products")
    public List<Product> addProduct(@RequestBody Product product) {
        productRepository.save(product);
        return productRepository.findAllByOrderById();
    }

    @DeleteMapping("products/{id}")
    public List<Product> deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return productRepository.findAllByOrderById();
    }

    @GetMapping("products/{id}")
    public Product getProduct(@PathVariable Long id) throws NoSuchElementException {
        if (!productRepository.existsById(id)) {
            throw new NoSuchElementException("Product not found");
        }
        return productRepository.findById(id).orElseThrow();
    }

    @PutMapping("products/{id}")
    public List<Product> editProduct(@PathVariable Long id, @RequestBody Product product) {
        if (productRepository.existsById(id)) {
            productRepository.save(product);
        }
        return productRepository.findAllByOrderById();
    }

    @PatchMapping("increase-stock/{id}")
    public List<Product> increaseStock(@PathVariable Long id) {
        Product product = productRepository.findById(id).orElseThrow();;
        product.setStock(product.getStock()+1);
        productRepository.save(product);
        return productRepository.findAllByOrderById();
    }

    @PatchMapping("decrease-stock/{id}")
    public List<Product> decreaseStock(@PathVariable Long id) throws Exception {
        Product product = productRepository.findById(id).orElseThrow();;
        if (product.getStock() > 0) {
            product.setStock(product.getStock()-1);
            productRepository.save(product);
        } else {
            throw new Exception("Kogus ei saa miinusesse minna");
        }
        return productRepository.findAllByOrderById();
    }
}
