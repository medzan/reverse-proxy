package com.kapelse.ktmp.handler;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyExtractor;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author ZANGUI Elmehdi
 */

public class HttpResponse implements ClientResponse {

    private Mono<byte[]> body;
    private ClientResponse delegate;

    HttpResponse(ClientResponse response) {
        body = response.bodyToMono(byte[].class); // Releases connection
        delegate = response;
    }

    @Override
    public HttpStatus statusCode() {
        return delegate.statusCode();
    }

    @Override
    public int rawStatusCode() {
        return delegate.rawStatusCode();
    }

    @Override
    public Headers headers() {
        return delegate.headers();
    }

    @Override
    public MultiValueMap<String, ResponseCookie> cookies() {
        return delegate.cookies();
    }

    @Override
    public ExchangeStrategies strategies() {
        return delegate.strategies();
    }

    public Mono<byte[]> getBody() {
        return body;
    }


    @Override
    public <T> T body(BodyExtractor<T, ? super ClientHttpResponse> extractor) {

        throw new IllegalStateException("Method not implemented");
    }

    @Override

    @SuppressWarnings("unchecked")
    public <T> Mono<T> bodyToMono(Class<? extends T> elementClass) {
        if (byte[].class.isAssignableFrom(elementClass)) {
            return (Mono<T>) getBody();
        }
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Mono<T> bodyToMono(ParameterizedTypeReference<T> typeReference) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Flux<T> bodyToFlux(Class<? extends T> elementClass) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Flux<T> bodyToFlux(ParameterizedTypeReference<T> typeReference) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public Mono<Void> releaseBody() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Mono<ResponseEntity<T>> toEntity(Class<T> bodyType) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Mono<ResponseEntity<T>> toEntity(ParameterizedTypeReference<T> typeReference) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Mono<ResponseEntity<List<T>>> toEntityList(Class<T> elementType) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public <T> Mono<ResponseEntity<List<T>>> toEntityList(ParameterizedTypeReference<T> typeReference) {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public Mono<ResponseEntity<Void>> toBodilessEntity() {
        return delegate.toBodilessEntity();
    }

    @Override
    public Mono<WebClientResponseException> createException() {
        return delegate.createException();
    }

    @Override
    public String logPrefix() {
        return delegate.logPrefix();
    }
}
