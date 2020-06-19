package com.kapelse.ktmp.configuration;

import com.kapelse.ktmp.ReverseProxyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
public class ProxyConfiguration {

    @Autowired
    private WebClientProvider webClientProvider;

    @Bean
    ReverseProxyFilter reverseProxyFilter() {
        return new ReverseProxyFilter(Ordered.HIGHEST_PRECEDENCE, webClientProvider);
    }
}

