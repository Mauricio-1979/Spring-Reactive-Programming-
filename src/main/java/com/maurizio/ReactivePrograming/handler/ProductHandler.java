package com.maurizio.ReactivePrograming.handler;

import com.maurizio.ReactivePrograming.dto.ProductDTO;
import com.maurizio.ReactivePrograming.exception.CustomException;
import com.maurizio.ReactivePrograming.exception.ErrorResponse;
import com.maurizio.ReactivePrograming.service.ProductService;
import com.maurizio.ReactivePrograming.validation.ObjectValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductService productService;

    private final ObjectValidator objectValidator;

    public Mono<ServerResponse> getAll(ServerRequest request) {
        return productService.getAll()
                .collectList()
                .flatMap(products -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(products));
    }

    public Mono<ServerResponse> getOne(ServerRequest request) {
        int id = Integer.valueOf(request.pathVariable("id"));
        return productService.getById(id)
                .flatMap(product -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(product));
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(ProductDTO.class)
                .doOnNext(objectValidator::validate)
                .flatMap(product -> productService.save(product)
                        .flatMap(savedProduct -> ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(savedProduct)));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        int id = Integer.valueOf(request.pathVariable("id"));
        return request.bodyToMono(ProductDTO.class)
                .doOnNext(objectValidator::validate)
                .flatMap(product -> productService.update(id, product)
                        .flatMap(updatedProduct -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(updatedProduct)))
                .onErrorResume(CustomException.class, ex -> createErrorResponse(ex));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        int id = Integer.valueOf(request.pathVariable("id"));
        return productService.delete(id)
                .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> createErrorResponse(CustomException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getStatus().value(), ex.getMessage());
        return ServerResponse.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(errorResponse);
    }

}
