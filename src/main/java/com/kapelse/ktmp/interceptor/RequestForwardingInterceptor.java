package com.kapelse.ktmp.interceptor;


import com.kapelse.ktmp.handler.HttpRequest;
import com.kapelse.ktmp.handler.HttpRequestExecution;
import com.kapelse.ktmp.handler.HttpResponse;
import reactor.core.publisher.Mono;

/**
 * @author ZANGUI Elmehdi
 */
public interface RequestForwardingInterceptor extends Comparable<RequestForwardingInterceptor> {

    Mono<HttpResponse> forward(HttpRequest request, HttpRequestExecution execution);

    int getOrder();

    @Override
    default int compareTo(RequestForwardingInterceptor requestForwardingInterceptor) {
        return Integer.compare(getOrder(), requestForwardingInterceptor.getOrder());
    }
}
