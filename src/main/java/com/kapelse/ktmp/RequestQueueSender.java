package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.WebClientProvider;
import com.kapelse.ktmp.decorator.CachingServerHttpRequestDecorator;
import com.kapelse.ktmp.helpers.HttpRequestMapper;
import com.kapelse.ktmp.interceptor.HttpRequestInterceptor;
import com.kapelse.ktmp.interceptor.RequestCommonHeadersRewriter;
import com.kapelse.ktmp.interceptor.RequestForwardingInterceptor;
import com.kapelse.ktmp.interceptor.RequestServerNameRewriter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;
import static reactor.core.publisher.Mono.just;

@Component
public class RequestQueueSender {
    private static final Logger log = getLogger(RequestQueueSender.class);
    private final HttpRequestMapper httpRequestMapper;
    ExecutorService threadPool;
    WebClient webClient;

    public RequestQueueSender(WebClientProvider webClientProvider) {
        threadPool = Executors.newCachedThreadPool();
        this.webClient = webClientProvider.getDefaultWebClient()
                                          .mutate()
                                          .filters(filters -> filters.addAll(createHttpRequestInterceptors()))
                                          .build();
        httpRequestMapper = new HttpRequestMapper();
    }

    public void push(CachingServerHttpRequestDecorator request) {
        // TODO rethink the usefulness of using a custom pool because reactive native implementation uses a default http poll
        threadPool.execute(() -> {
            webClient.method(httpRequestMapper.extractMethod(request))
                     .uri(httpRequestMapper.extractUri(request))
                     .headers(headers -> headers.putAll(httpRequestMapper.extractHeaders(request)))
                     .body(just(request.getCachedResponseBody()), String.class)
                     .exchange()
                     .subscribe();

        });

    }
    //TODO refactor interceptors to use more clever server name rewriter
    private List<HttpRequestInterceptor> createHttpRequestInterceptors() {
        List<RequestForwardingInterceptor> requestForwardingInterceptors = new ArrayList<>();
        requestForwardingInterceptors.add(new RequestServerNameRewriter(100, "http://127.0.0.1:9999"));
        requestForwardingInterceptors.add(new RequestCommonHeadersRewriter(200));
        return requestForwardingInterceptors.stream()
                                            .map(HttpRequestInterceptor::new)
                                            .collect(toList());
    }

}
