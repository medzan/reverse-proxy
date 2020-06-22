package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.WebClientProvider;
import com.kapelse.ktmp.decorator.CachingServerHttpRequestDecorator;
import com.kapelse.ktmp.helpers.HttpRequestMapper;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Mono.just;

@Component
public class RequestQueueSender {
    private static final Logger log = getLogger(RequestQueueSender.class);
    private final HttpRequestMapper httpRequestMapper;
    ExecutorService threadPool;
    private WebClientProvider webClientProvider;
    private RequestMappingResolver requestMappingResolver;

    public RequestQueueSender(WebClientProvider webClientProvider, RequestMappingResolver requestMappingResolver) {
        threadPool = Executors.newCachedThreadPool();
        this.webClientProvider = webClientProvider;
        httpRequestMapper = new HttpRequestMapper();
        this.requestMappingResolver = requestMappingResolver;
    }

    public void push(CachingServerHttpRequestDecorator request) {
        threadPool.execute(() -> {
            Set<String> endpoints = requestMappingResolver.resolveBackupMapping(request);
            endpoints.forEach(uri -> {
                WebClient webClient = webClientProvider.getWebClient(uri);
                webClient.method(httpRequestMapper.extractMethod(request))
                         .uri(httpRequestMapper.extractUri(request))
                         .headers(headers -> headers.putAll(httpRequestMapper.extractHeaders(request)))
                         .body(just(request.getCachedResponseBody()), String.class)
                         .exchange()
                         .subscribe();
            });
        });
    }
}
