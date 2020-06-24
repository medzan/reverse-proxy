package com.kapelse.ktmp.proxy.filter;

import com.kapelse.ktmp.proxy.filter.decorator.CustomServerWebExchangeDecorator;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * @author ZANGUI Elmehdi
 */
public class CachingFilter implements OrderedWebFilter {

    private int order;

    public CachingFilter(int order) {
        this.order = order;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(new CustomServerWebExchangeDecorator(exchange));
    }

}
