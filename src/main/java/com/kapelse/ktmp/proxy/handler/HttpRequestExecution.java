package com.kapelse.ktmp.proxy.handler;

import io.netty.channel.ChannelException;
import org.slf4j.Logger;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

import static org.slf4j.LoggerFactory.getLogger;
/**
 * @author ZANGUI Elmehdi
 */
public class HttpRequestExecution {

    private static final Logger log = getLogger(HttpRequestExecution.class);
    private ExchangeFunction exchange;

    public HttpRequestExecution(ExchangeFunction exchange) {
        this.exchange = exchange;
    }

    public Mono<HttpResponse> execute(HttpRequest request) {
        return exchange.exchange(request)
                       .map(response -> response instanceof HttpResponse
                                        ? (HttpResponse) response
                                        : new HttpResponse(response))
                       .doOnError(ChannelException.class, e -> {
                           log.warn("Ignore  error on incoming request {}", e.getMessage());
                       });
    }

}
