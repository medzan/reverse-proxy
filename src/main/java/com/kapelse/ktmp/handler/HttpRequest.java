package com.kapelse.ktmp.handler;

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

/**
 * @author ZANGUI Elmehdi
 */
public class HttpRequest implements ClientRequest {

    private ClientRequest delegate;

    public HttpRequest(ClientRequest request) {
        delegate = request;
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
    @Override
    public HttpMethod method() {
        return delegate.method();
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
