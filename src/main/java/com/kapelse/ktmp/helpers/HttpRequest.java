package com.kapelse.ktmp.helpers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static org.springframework.web.reactive.function.client.ClientRequest.from;

public class HttpRequest implements ClientRequest {

    private ClientRequest delegate;

    public HttpRequest(ClientRequest request) {
        delegate = request;
    }

    public ClientRequest clientRequest(){
        return delegate;
    }
    @Override
    public URI url() {
        return delegate.url();
    }

    public void setUrl(URI url) {
        delegate = from(delegate)
                .url(url)
                .build();
    }
    public HttpRequest clone(URI url){
       return new HttpRequest(from(delegate).url(url).build());
    }

    @Override
    public HttpMethod method() {
        return delegate.method();
    }

    public void setMethod(HttpMethod method) {
        delegate = from(delegate)
                .method(method)
                .build();
    }

    @Override
    public HttpHeaders headers() {
        return delegate.headers();
    }

    public void setHeaders(HttpHeaders headers) {
        delegate = from(delegate)
                .headers(httpHeaders -> {
                    httpHeaders.clear();
                    httpHeaders.putAll(headers);
                })
                .build();
    }

    @Override
    public MultiValueMap<String, String> cookies() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public BodyInserter<?, ? super ClientHttpRequest> body() {
        return delegate.body();
    }

    public void setBody(BodyInserter<?, ? super ClientHttpRequest> body) {
        delegate = from(delegate)
                .body(body)
                .build();
    }

    @Override
    public Map<String, Object> attributes() {
        return delegate.attributes();
    }

    @Override
    public String logPrefix() {
        return delegate.logPrefix();
    }

    @Override
    public Mono<Void> writeTo(ClientHttpRequest request, ExchangeStrategies strategies) {
        return delegate.writeTo(request, strategies);
    }
}
