package com.kapelse.ktmp.configuration;

import com.kapelse.ktmp.CachingFilter;
import com.kapelse.ktmp.RequestQueueSender;
import com.kapelse.ktmp.ReverseProxyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class ProxyConfiguration {


    @Autowired
    private WebClientProvider webClientProvider;
    @Autowired
    private RequestQueueSender requestQueueSender;

    // Caching filter must have highest precedence over reverse proxy as is used to prepare the request
    @Bean
    CachingFilter cachingFilter() {
        return new CachingFilter(Ordered.HIGHEST_PRECEDENCE);
    }

    @Bean
    ReverseProxyFilter reverseProxyFilter() {
        return new ReverseProxyFilter(Ordered.HIGHEST_PRECEDENCE + 1, webClientProvider, requestQueueSender);
    }


}

