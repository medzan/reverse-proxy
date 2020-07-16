package com.kapelse.ktmp.proxy.acutator;

import com.kapelse.ktmp.proxy.filter.RequestMappingResolver;
import com.kapelse.ktmp.proxy.filter.WebClientProvider;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ServersHealthIndicator implements ReactiveHealthIndicator {

    private WebClientProvider webClientProvider;
    private RequestMappingResolver requestMappingResolver;

    public ServersHealthIndicator(WebClientProvider webClientProvider, RequestMappingResolver requestMappingResolver) {
        this.webClientProvider = webClientProvider;
        this.requestMappingResolver = requestMappingResolver;
    }

    @Override
    public Mono<Health> health() {
        Health.Builder builder = new Health.Builder();
        builder.up();
        return checkPrimaryServers(builder);
    }

    private Mono<Health> checkPrimaryServers(Health.Builder builder) {
        HashMap<Pattern, String> primaryMapping = requestMappingResolver.getPrimaryMapping();
        HashMap<Pattern, String> backupMapping = requestMappingResolver.getBackupMapping();
        primaryMapping.putAll(backupMapping);
        Assert.notNull(primaryMapping, "Primary server cannot be empty");
        HashMap<Pattern, String> onPrimaryErrors = new HashMap<>();
        HashMap<Pattern, String> onPrimaryUp = new HashMap<>();
        List<Mono<?>> calls = new ArrayList<>();

        primaryMapping.forEach((pattern, uri) -> {
            calls.add(webClientProvider.getWebClient(uri)
                                       .method(HttpMethod.GET)
                                       .exchange()
                                       .onErrorContinue((t, v) -> builder.down().withDetail(pattern.toString()+" : "+uri, "DOWN"))
                                       .map(e -> builder.withDetail(pattern.toString()+" : "+uri, "UP"))
                                       );

        });
       return Mono.zip(calls, Arrays::asList).flatMap(arg -> Mono.just(builder.build()));



    }
}
