package com.kapelse.ktmp;

import com.kapelse.ktmp.helpers.HttpRequest;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

public class RequestQueueSender {
    private static final Logger log = getLogger(RequestQueueSender.class);
    ExecutorService threadPool;
    WebClient webClient;


    public RequestQueueSender(WebClient webClient) {
        threadPool = Executors.newCachedThreadPool();
        this.webClient = webClient;
    }

    @Async
    public void registerRequestDuplication(HttpRequest request) {

        threadPool.execute(() -> {
        // TODO
            URI rewrittenServerName = null;
            try {
                rewrittenServerName = new URI("http://localhost:9999");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            URI rewrittenUri = fromUri(request.url())
                    .scheme(rewrittenServerName.getScheme())
                    .host(rewrittenServerName.getHost())
                    .port(rewrittenServerName.getPort())
                    .build(true)
                    .toUri();
            request.setUrl(rewrittenUri);

            webClient.method(request.method())
                     .uri(rewrittenUri)
                     .headers(headers -> headers.putAll(request.headers()))
                     .body(request.body())
                     .exchange()
                     .subscribe();

        });
    }

}
