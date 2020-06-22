package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.WebClientProvider;
import com.kapelse.ktmp.decorator.CachingServerHttpRequestDecorator;
import com.kapelse.ktmp.helpers.HttpRequestMapper;
import com.kapelse.ktmp.helpers.HttpResponseMapper;
import com.kapelse.ktmp.interceptor.*;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ReverseProxyFilter implements OrderedWebFilter {

    private final WebClient webClient;
    private int order;
    private RequestQueueSender requestQueueSender;
    private HttpRequestMapper httpRequestMapper;
    private HttpResponseMapper httpResponseMapper;

    public ReverseProxyFilter(int order, WebClientProvider webClientProvider, RequestQueueSender requestQueueSender) {
        this.order = order;
        this.requestQueueSender = requestQueueSender;
        httpRequestMapper = new HttpRequestMapper();
        httpResponseMapper = new HttpResponseMapper();
        webClient = webClientProvider.getDefaultWebClient().mutate()
                                     .filters(f -> f.addAll(createHttpRequestInterceptors()))
                                     .build();
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Assert.isInstanceOf(CachingServerHttpRequestDecorator.class, exchange.getRequest(),
                            "The request is not decorated :: check that cachingFilter is registered before ReverseProxyFilter ");
        CachingServerHttpRequestDecorator request = (CachingServerHttpRequestDecorator)exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

       return webClient.method(httpRequestMapper.extractMethod(request))
                 .uri(httpRequestMapper.extractUri(request))
                 .headers(headers -> headers.putAll(httpRequestMapper.extractHeaders(request)))
                 .body(httpRequestMapper.extractBody(request))
                 .exchange()
                 .flatMap(clientResponse -> httpResponseMapper.map(clientResponse, response))
                 .doAfterTerminate(() -> {
                     requestQueueSender.push(request);
                 });

    }

    private List<HttpRequestInterceptor> createHttpRequestInterceptors() {
        List<RequestForwardingInterceptor> requestForwardingInterceptors = new ArrayList<>();
        requestForwardingInterceptors.add(new RequestServerNameRewriter(100, "http://127.0.0.1:8080"));
        requestForwardingInterceptors.add(new RequestCommonHeadersRewriter(200));
        return requestForwardingInterceptors.stream()
                                            .map(HttpRequestInterceptor::new)
                                            .collect(toList());
    }
}
