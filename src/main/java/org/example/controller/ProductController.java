package org.example.controller;

import org.example.dto.product.ProductItemDto;
import org.example.dto.product.ProductPostDto;
import org.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public List<ProductItemDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductItemDto> getProductById(@PathVariable Integer id) {
        var productDto = productService.getProductById(id);
        return productDto != null
                ? new ResponseEntity<>(productDto, HttpStatus.OK)
                : ResponseEntity.notFound().build();
    }

    @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductItemDto> createProduct(@ModelAttribute ProductPostDto product) {
        var productDto = productService.createProduct(product);
        return new ResponseEntity<>(productDto, HttpStatus.CREATED);
    }

    @PutMapping(path = "/{id}", consumes = MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateProduct(@PathVariable Integer id, @ModelAttribute ProductPostDto product) {
        return productService.updateProduct(id, product)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        return productService.deleteProduct(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
