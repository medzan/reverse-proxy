package com.kapelse.ktmp.proxy.config;

import com.kapelse.ktmp.proxy.filter.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;

/**
 * @author ZANGUI Elmehdi
 */

@Configuration
@DependsOn("externalPropertySourcesConfig")
public class ProxyConfiguration {


    @Autowired
    private WebClientProvider webClientProvider;
    @Autowired
    private RequestQueueSender requestQueueSender;
    @Autowired
    private RequestMappingResolver requestMappingResolver;

    // Caching filter must have highest precedence over reverse proxy as is used to prepare the request
    @Bean
    CachingFilter cachingFilter() {
        return new CachingFilter(Ordered.HIGHEST_PRECEDENCE);
    }

    @Bean
    ReverseProxyFilter reverseProxyFilter() {

        return new ReverseProxyFilter(Ordered.HIGHEST_PRECEDENCE + 1, webClientProvider, requestQueueSender, requestMappingResolver);
    }


}

