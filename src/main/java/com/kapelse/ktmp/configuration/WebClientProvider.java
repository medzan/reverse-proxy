package com.kapelse.ktmp.configuration;


import com.kapelse.ktmp.RequestQueueSender;
import com.kapelse.ktmp.interceptor.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.core.Ordered;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.kapelse.ktmp.helpers.Utils.toMillis;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.client.WebClient.builder;
import static reactor.netty.http.client.HttpClient.create;

@Component
public class WebClientProvider {

    private WebClient webClientWithFilters;
    private WebClient basicWebClient;
    private RequestQueueSender requestQueueSender;
    private Duration connection;
    private Duration read;
    private Duration write;

    WebClientProvider() {
        connection = ofSeconds(3);
        read = ofSeconds(20);
        write = ofSeconds(20);
        basicWebClient = builder().clientConnector(createConnector()).build();
        this.requestQueueSender = new RequestQueueSender(basicWebClient);
        webClientWithFilters = buildWebClient();
    }

    private WebClient buildWebClient() {
        ClientHttpConnector connector = createConnector();
        List<ExchangeFilterFunction> interceptors = new ArrayList<>(createHttpRequestInterceptors());
        return builder()
                .clientConnector(connector)
                .filters(filters -> filters.addAll(interceptors))
                .build();
    }

    private List<HttpRequestInterceptor> createHttpRequestInterceptors() {
        List<RequestForwardingInterceptor> requestForwardingInterceptors = new ArrayList<>();
        requestForwardingInterceptors.add(new RequestServerNameRewriter(100));
        requestForwardingInterceptors.add(new RequestCommonHeadersRewriter(200));
        requestForwardingInterceptors.add(new RequestDuplicateSenderInterceptor(Ordered.LOWEST_PRECEDENCE,
                                                                                requestQueueSender));

        return requestForwardingInterceptors.stream()
                                            .map(HttpRequestInterceptor::new)
                                            .collect(toList());
    }

    public ClientHttpConnector createConnector() {
        return new ReactorClientHttpConnector(create().followRedirect(false) .tcpConfiguration(client ->
                       client.option(CONNECT_TIMEOUT_MILLIS, toMillis(connection))
                             .doOnConnected(connection -> connection
                             .addHandlerLast(new ReadTimeoutHandler(read.toMillis(), MILLISECONDS))
                              .addHandlerLast(new WriteTimeoutHandler(write.toMillis(), MILLISECONDS)))));
    }

    public WebClient getWebClientWithFilters() {
        return webClientWithFilters;
    }

    public WebClient getBasicWebClient() {
        return basicWebClient;
    }
}
