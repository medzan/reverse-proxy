package com.kapelse.ktmp.interceptor;

import com.kapelse.ktmp.handler.HttpRequest;
import com.kapelse.ktmp.handler.HttpRequestExecution;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * @author ZANGUI Elmehdi
 */

public class HttpRequestInterceptor implements ExchangeFilterFunction {


    private RequestForwardingInterceptor requestForwardingInterceptor;

    public HttpRequestInterceptor( RequestForwardingInterceptor requestForwardingInterceptor) {
        this.requestForwardingInterceptor = requestForwardingInterceptor;
    }



    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction exchange) {
        HttpRequest httpRequest = request instanceof HttpRequest
                ? (HttpRequest) request
                : new HttpRequest(request);
        HttpRequestExecution requestExecution = exchange instanceof HttpRequestExecution
                ? (HttpRequestExecution) exchange
                : new HttpRequestExecution( exchange);
        return requestForwardingInterceptor.forward(httpRequest, requestExecution).cast(ClientResponse.class);
    }
}
