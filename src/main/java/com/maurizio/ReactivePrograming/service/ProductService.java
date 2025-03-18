package com.maurizio.ReactivePrograming.service;

import com.maurizio.ReactivePrograming.dto.ProductDTO;
import com.maurizio.ReactivePrograming.entity.Product;
import com.maurizio.ReactivePrograming.repository.ProductRepository;
import com.maurizio.ReactivePrograming.exception.CustomException;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final static String NF_MESSAGE = "product not found";
    private final static String NAME_MESSAGE = "product name already in use";

    private final ProductRepository productRepository;

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    public Flux<Product> getAll(){
        return productRepository.findAll();
    }

    public Mono<Product> getById(int id){
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(HttpStatus.NOT_FOUND, NF_MESSAGE)));
    }

    public Mono<Product> save(ProductDTO dto) {
        Mono<Boolean> existsName = productRepository.findByName(dto.getName()).hasElement();
        return existsName.flatMap(exists -> exists ? Mono.error(new CustomException(HttpStatus.BAD_REQUEST, NAME_MESSAGE))
                : productRepository.save(Product.builder().name(dto.getName()).price(dto.getPrice()).build()));
    }

    public Mono<Product> update(int id, ProductDTO dto) {
        log.info("Attempting to update product with ID: {}", id);

        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new CustomException(HttpStatus.NOT_FOUND, NF_MESSAGE)))
            .flatMap(existingProduct -> {
                return productRepository.repeatedName(id, dto.getName())
                    .doOnNext(repeated -> log.info("Repeated name check result: {}", repeated))
                    .flatMap(repeated -> {
                        if (repeated != null && repeated.getId() != id) {
                            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST, NAME_MESSAGE));
                        }

                        existingProduct.setName(dto.getName());
                        existingProduct.setPrice(dto.getPrice());

                        return productRepository.save(existingProduct)
                            .doOnSuccess(updated -> log.info("Product updated successfully: {}", updated))
                            .onErrorMap(e -> {
                                return new CustomException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update product");
                            });
                    });
            });
    }


    public Mono<Void> delete(int id) {
        Mono<Boolean> productId = productRepository.findById(id).hasElement();
        return productId.flatMap(exists -> exists ? productRepository.deleteById(id) : Mono.error(new CustomException(HttpStatus.NOT_FOUND, NF_MESSAGE)));
    }
}
