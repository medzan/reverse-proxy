package com.kapelse.ktmp.proxy.interceptor;

import com.kapelse.ktmp.proxy.handler.HttpRequest;
import com.kapelse.ktmp.proxy.handler.HttpRequestExecution;
import com.kapelse.ktmp.proxy.handler.HttpResponse;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.valueOf;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpHeaders.*;

/**
 * Prepare the next transmitted request by rewriting the headers
 * this main functionality is a duplicate of what it used by any reverse proxy project
 *
 * @author ZANGUI Elmehdi
 */
public class RequestCommonHeadersRewriter implements RequestForwardingInterceptor {

    private static final Logger log = getLogger(RequestCommonHeadersRewriter.class);
    private int order;
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String X_FORWARDED_PROTO = "X-Forwarded-Proto";
    private static final String X_FORWARDED_HOST = "X-Forwarded-Host";
    private static final String X_FORWARDED_PORT = "X-Forwarded-Port";
    private static final String PUBLIC_KEY_PINS = "Public-Key-Pins";
    private static final String STRICT_TRANSPORT_SECURITY = "Strict-Transport-Security";

    public RequestCommonHeadersRewriter(int order) {
        this.order = order;
    }

    @Override
    public Mono<HttpResponse> forward(HttpRequest request, HttpRequestExecution execution) {
        log.trace("[Start] Request Common headers rewriting ");
        rewriteHeaders(request.headers(), request.url(), request::setHeaders);
        return execution.execute(request)
                        .doOnSuccess(response -> log.trace("[End] Request Common headers rewriting "));
    }

    @Override
    public int getOrder() {
        return order;
    }

    void rewriteHeaders(HttpHeaders headers, URI uri, Consumer<HttpHeaders> headersSetter) {
        HttpHeaders rewrittenHeaders = copyHeaders(headers);
        rewrittenHeaders.set(CONNECTION, "close");
        rewrittenHeaders.remove(TE);

        List<String> forwardedFor = new ArrayList<>(emptyIfNull(rewrittenHeaders.get(X_FORWARDED_FOR)));
        forwardedFor.add(uri.getAuthority());
        rewrittenHeaders.put(X_FORWARDED_FOR, forwardedFor);
        rewrittenHeaders.set(X_FORWARDED_PROTO, uri.getScheme());
        rewrittenHeaders.set(X_FORWARDED_HOST, uri.getHost());
        rewrittenHeaders.set(X_FORWARDED_PORT, resolvePort(uri));

        rewrittenHeaders.remove(TRANSFER_ENCODING);
        rewrittenHeaders.remove(CONNECTION);
        rewrittenHeaders.remove(PUBLIC_KEY_PINS);
        rewrittenHeaders.remove(SERVER);
        rewrittenHeaders.remove(STRICT_TRANSPORT_SECURITY);

        headersSetter.accept(rewrittenHeaders);
        log.debug("Request headers rewritten from {} to {}", headers, rewrittenHeaders);
    }

    private String resolvePort(URI uri) {
        int port = uri.getPort();
        if (port < 0) {
            port = uri.getScheme().equals("https") ? 443 : 80;
        }
        return valueOf(port);
    }

    private HttpHeaders copyHeaders(HttpHeaders headers) {
        HttpHeaders copy = new HttpHeaders();
        copy.putAll(headers);
        return copy;
    }

}
