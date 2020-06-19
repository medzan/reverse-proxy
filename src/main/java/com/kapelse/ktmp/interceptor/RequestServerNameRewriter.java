package com.kapelse.ktmp.interceptor;

import com.kapelse.ktmp.helpers.HttpRequest;
import com.kapelse.ktmp.helpers.HttpRequestExecution;
import com.kapelse.ktmp.helpers.HttpResponse;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

public class RequestServerNameRewriter  implements RequestForwardingInterceptor {

    private static final Logger log = getLogger(RequestServerNameRewriter.class);
    private int order;
    private URI outgoingServer;

    public RequestServerNameRewriter(int order)  {
        this.order = order;
        try {
            this.outgoingServer = new URI("http://127.0.0.1:8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Mono<HttpResponse> forward(HttpRequest request, HttpRequestExecution execution) {
        log.trace("[Start] Request server name rewriting ");
        rewriteServerName(request.url(), request::setUrl);
        return execution.execute(request)
                        .doOnSuccess(response -> log.trace("[End] Request server name rewriting "));
    }

    @Override
    public int getOrder() {
        return order;
    }

    void rewriteServerName(URI uri, Consumer<URI> rewrittenUriSetter) {
        String oldServerName = uri.getScheme() + "://" + uri.getAuthority();
        URI rewrittenServerName = outgoingServer;
        URI rewrittenUri = fromUri(uri)
                .scheme(rewrittenServerName.getScheme())
                .host(rewrittenServerName.getHost())
                .port(rewrittenServerName.getPort())
                .build(true)
                .toUri();
        rewrittenUriSetter.accept(rewrittenUri);
        log.debug("Request server name rewritten from {} to {}", oldServerName, rewrittenServerName);
    }


}
