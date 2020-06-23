package com.kapelse.ktmp.filter.decorator;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import reactor.core.publisher.Flux;

import static org.apache.commons.codec.Charsets.UTF_8;

/**
 * As each request is consumed once by a reactive processing, this decorator retains the reactive semantics but cache the request body
 * It could be used by any request duplicator in order to push the same request to another endpoint
 *
 *  @author ZANGUI Elmehdi
 */
public class CachingServerHttpRequestDecorator extends ServerHttpRequestDecorator {

    private final StringBuilder cachedBody = new StringBuilder();

    public CachingServerHttpRequestDecorator(ServerHttpRequest delegate) {
        super(delegate);
    }

    @Override
    public Flux<DataBuffer> getBody() {
        return super.getBody().doOnNext(this::cache);
    }

    private void cache(DataBuffer buffer) {
        cachedBody.append(UTF_8.decode(buffer.asByteBuffer())
                               .toString());
    }

    public String getCachedResponseBody() {
        return cachedBody.toString();
    }
}
