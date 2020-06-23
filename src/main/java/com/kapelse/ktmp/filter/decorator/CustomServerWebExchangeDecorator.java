package com.kapelse.ktmp.filter.decorator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;

/**
 * @author ZANGUI Elmehdi
 */
public class CustomServerWebExchangeDecorator extends ServerWebExchangeDecorator {
    private final ServerHttpRequestDecorator requestDecorator;
    private final ServerHttpResponseDecorator responseDecorator;

    public CustomServerWebExchangeDecorator(ServerWebExchange delegate) {
        super(delegate);
        this.requestDecorator = new CachingServerHttpRequestDecorator(delegate.getRequest());
        this.responseDecorator = new ServerHttpResponseDecorator(delegate.getResponse());
    }

    @Override
    public ServerHttpRequest getRequest() {
        return requestDecorator;
    }

    @Override
    public ServerHttpResponse getResponse() {
        return responseDecorator;
    }
}
