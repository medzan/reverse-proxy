package com.kapelse.ktmp.filter;


import com.kapelse.ktmp.interceptor.HttpRequestInterceptor;
import com.kapelse.ktmp.interceptor.RequestCommonHeadersRewriter;
import com.kapelse.ktmp.interceptor.RequestForwardingInterceptor;
import com.kapelse.ktmp.interceptor.RequestServerNameRewriter;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.reactive.function.client.WebClient.builder;
import static reactor.netty.http.client.HttpClient.create;

/**
 * @author ZANGUI Elmehdi
 */

@Component
public class WebClientProvider {

    //webclient per uri
    private ConcurrentHashMap<String, WebClient> webClientCache;
    @Value("${webclient.timeout.connection}")
    private long connection;
    @Value("${webclient.timeout.read}")
    private long read;
    @Value("${webclient.timeout.write}")
    private long write;

    WebClientProvider() {
        webClientCache = new ConcurrentHashMap<>();
    }

    public ClientHttpConnector createConnector() {
        return new ReactorClientHttpConnector(create().followRedirect(false)
                                                      .tcpConfiguration(client -> client.option(CONNECT_TIMEOUT_MILLIS,
                                                                                                (int) ofSeconds(connection).toMillis())
                                                       .doOnConnected(connection -> connection
                                                       .addHandlerLast(new ReadTimeoutHandler(read, SECONDS))
                                                       .addHandlerLast(new WriteTimeoutHandler(write, SECONDS)))));
    }

    public WebClient getWebClient(String outgoingServer) {
        return webClientCache.computeIfAbsent(outgoingServer, (uri) -> builder()
                .filters(f -> f.addAll(createHttpRequestInterceptors(outgoingServer)))
                .clientConnector(createConnector())
                .build()
        );
    }

    private List<HttpRequestInterceptor> createHttpRequestInterceptors(String outgoingServerUri) {
        List<RequestForwardingInterceptor> requestForwardingInterceptors = new ArrayList<>();
        requestForwardingInterceptors.add(new RequestServerNameRewriter(100, outgoingServerUri));
        requestForwardingInterceptors.add(new RequestCommonHeadersRewriter(200));
        return requestForwardingInterceptors.stream()
                                            .map(HttpRequestInterceptor::new)
                                            .collect(toList());
    }
}
