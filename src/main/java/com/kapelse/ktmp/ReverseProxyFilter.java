package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.WebClientProvider;
import com.kapelse.ktmp.decorator.CachingServerHttpRequestDecorator;
import com.kapelse.ktmp.helpers.HttpRequestMapper;
import com.kapelse.ktmp.helpers.HttpResponseMapper;
import com.kapelse.ktmp.interceptor.HttpRequestInterceptor;
import com.kapelse.ktmp.interceptor.RequestCommonHeadersRewriter;
import com.kapelse.ktmp.interceptor.RequestForwardingInterceptor;
import com.kapelse.ktmp.interceptor.RequestServerNameRewriter;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
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

    private int order;
    private WebClientProvider webClientProvider;
    private RequestQueueSender requestQueueSender;
    private RequestMappingResolver requestMappingResolver;
    private HttpRequestMapper httpRequestMapper;
    private HttpResponseMapper httpResponseMapper;

    public ReverseProxyFilter(int order,
                              WebClientProvider webClientProvider,
                              RequestQueueSender requestQueueSender,
                              RequestMappingResolver requestMappingResolver) {
        this.order = order;
        this.webClientProvider = webClientProvider;
        this.requestQueueSender = requestQueueSender;
        this.requestMappingResolver = requestMappingResolver;
        httpRequestMapper = new HttpRequestMapper();
        httpResponseMapper = new HttpResponseMapper();

    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Assert.isInstanceOf(CachingServerHttpRequestDecorator.class, exchange.getRequest(),
                            "The request is not decorated :: check that cachingFilter is registered before ReverseProxyFilter ");
        CachingServerHttpRequestDecorator request = (CachingServerHttpRequestDecorator) exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String outgoingServerUri = requestMappingResolver.resolveMainMapping(request);
        WebClient webClient = webClientProvider.getWebClient(outgoingServerUri);

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

}
