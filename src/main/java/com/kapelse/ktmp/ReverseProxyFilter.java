package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.WebClientProvider;
import com.kapelse.ktmp.helpers.HttpRequestMapper;
import com.kapelse.ktmp.helpers.HttpResponseMapper;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;

public class ReverseProxyFilter implements OrderedWebFilter {

    private int order;
    private HttpRequestMapper httpRequestMapper;
    private HttpResponseMapper httpResponseMapper;
    private WebClientProvider webClientProvider;

    public ReverseProxyFilter(int order, WebClientProvider webClientProvider) {
        this.order = order;
        httpRequestMapper = new HttpRequestMapper();
        httpResponseMapper = new HttpResponseMapper();
        this.webClientProvider = webClientProvider;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        WebClient webClient = webClientProvider.getWebClientWithFilters();

        return webClient.method(httpRequestMapper.extractMethod(request))
                        .uri(httpRequestMapper.extractUri(request))
                        .headers(headers -> headers.putAll(httpRequestMapper.extractHeaders(request)))
                        .body(httpRequestMapper.extractBody(request))
                        .exchange()
                        .flatMap(clientResponse -> httpResponseMapper.map(clientResponse, response));
    }
}
