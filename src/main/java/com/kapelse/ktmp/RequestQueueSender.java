package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.WebClientProvider;
import com.kapelse.ktmp.helpers.HttpRequest;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.valueOf;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

@Component
public class RequestQueueSender {
    private static final Logger log = getLogger(RequestQueueSender.class);

    ExecutorService threadPool;
    WebClient webClient;

    public RequestQueueSender(WebClientProvider webClientProvider) {
        threadPool = Executors.newCachedThreadPool();
        this.webClient = webClientProvider.getDefaultWebClient()
                                          .mutate()
                                          .filters(filters -> new ArrayList<>())
                                          .build();
    }

    @Async
    public void registerRequestDuplication(HttpRequest request) {
        // TODO testing method to validate remote request

        URI rewrittenServerName = null;
        try {
            rewrittenServerName = new URI("http://127.0.0.1:9999");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        URI uri = fromUri(request.url())
                .scheme(rewrittenServerName.getScheme())
                .host(rewrittenServerName.getHost())
                .port(rewrittenServerName.getPort())
                .build(true)
                .toUri();
        HttpRequest duplicatedRequest = request.clone(uri);

        threadPool.execute(() -> {
            webClient.method(duplicatedRequest.method())
                     .uri(duplicatedRequest.url())
                     .headers(headers -> headers.putAll(duplicatedRequest.headers()))
                     .body(duplicatedRequest.body())
                     .exchange()
                     .doOnError(e -> log.error("Error ", e))
                     .subscribe(result -> log.info("Result " + request));

        });

    }

    private String resolvePort(URI uri) {
        int port = uri.getPort();
        if (port < 0) {
            port = uri.getScheme().equals("https") ? 443 : 80;
        }
        return valueOf(port);
    }
}
