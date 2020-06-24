package com.kapelse.ktmp.proxy.handler;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.BodyInserter;
import reactor.core.publisher.Flux;

import java.net.URI;

import static org.springframework.web.reactive.function.BodyInserters.fromDataBuffers;

/**
 * @author ZANGUI Elmehdi
 */

public class HttpRequestMapper {

    public URI extractUri(ServerHttpRequest request) {
        return request.getURI();
    }

    public HttpMethod extractMethod(ServerHttpRequest request) {
        return request.getMethod();
    }

    public HttpHeaders extractHeaders(ServerHttpRequest request) {
        return request.getHeaders();
    }

    public BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> extractBody(ServerHttpRequest request) {
        return fromDataBuffers(request.getBody());
    }
}
