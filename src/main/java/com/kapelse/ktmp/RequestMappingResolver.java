package com.kapelse.ktmp;

import com.kapelse.ktmp.configuration.MappingProperties;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class RequestMappingResolver {


    private MappingProperties mappingProperties;

    public RequestMappingResolver(MappingProperties mappingProperties) {
        this.mappingProperties = mappingProperties;
    }

    public String resolveMainMapping(ServerHttpRequestDecorator requestUri) {
        Set<String> endpoint = resolve(mappingProperties.getMain(), requestUri);
        Assert.state(endpoint.size() == 1, "There is none or more then one endpoint configured as main," +
                "Only one endpoint processing responsible for the response should be selected as the main ");
        return endpoint.iterator().next();

    }

    public Set<String> resolveBackupMapping(ServerHttpRequestDecorator requestUri) {
        return resolve(mappingProperties.getBackup(), requestUri);
    }

    private Set<String> resolve(HashMap<Pattern, String> mapping, ServerHttpRequestDecorator uri) {
        Set<String> endpoint = new HashSet<>();
        mapping.forEach((pathRegex, endpointProperty) -> {
            if (pathRegex.matcher(uri.getPath().value()).matches()) {
                endpoint.add(endpointProperty);
            }
        });
        return endpoint;
    }

}
