package com.kapelse.ktmp.interceptor;

import com.kapelse.ktmp.RequestQueueSender;
import com.kapelse.ktmp.helpers.HttpRequest;
import com.kapelse.ktmp.helpers.HttpRequestExecution;
import com.kapelse.ktmp.helpers.HttpResponse;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import static org.slf4j.LoggerFactory.getLogger;

public class RequestDuplicateSenderInterceptor implements RequestForwardingInterceptor {

    private static final Logger log = getLogger(RequestDuplicateSenderInterceptor.class);
    private int order;
    private RequestQueueSender requestQueue;


    public RequestDuplicateSenderInterceptor(int order, RequestQueueSender requestQueue) {
        this.order = order;
        this.requestQueue = requestQueue;
    }

    @Override
    public Mono<HttpResponse> forward(HttpRequest request, HttpRequestExecution execution) {
        log.trace("[Start] Request duplication sending ");
        //TODO Its not working yet, cause read time out exception
//        requestQueue.registerRequestDuplication(request);
        return execution.execute(request)
                        .doOnSuccess(response -> log.trace("[End] Request duplication sending "));
    }

    @Override
    public int getOrder() {
        return order;
    }
}
