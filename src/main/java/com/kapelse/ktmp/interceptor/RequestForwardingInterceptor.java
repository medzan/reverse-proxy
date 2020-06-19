package com.kapelse.ktmp.interceptor;


import com.kapelse.ktmp.helpers.HttpRequest;
import com.kapelse.ktmp.helpers.HttpRequestExecution;
import com.kapelse.ktmp.helpers.HttpResponse;
import reactor.core.publisher.Mono;

public interface RequestForwardingInterceptor extends Comparable<RequestForwardingInterceptor> {

    Mono<HttpResponse> forward(HttpRequest request, HttpRequestExecution execution);

    int getOrder();

    @Override
    default int compareTo(RequestForwardingInterceptor requestForwardingInterceptor) {
        return Integer.compare(getOrder(), requestForwardingInterceptor.getOrder());
    }
}
