package com.kapelse.ktmp.proxy.filter;

import com.kapelse.ktmp.proxy.config.MappingProperties;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.*;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author ZANGUI Elmehdi
 */
@Component
public class RequestMappingResolver {
    private static final Logger log = getLogger(RequestMappingResolver.class);

    private MappingProperties mappingProperties;

    public RequestMappingResolver(MappingProperties mappingProperties) {
        this.mappingProperties = mappingProperties;
    }

    public String resolvePrimaryMapping(String requestUri) {
        Set<Endpoint> candidates = resolve(mappingProperties.getPrimary(), requestUri);
        String primaryServerUri = bestMatchingPattern(candidates);
        log.debug("Request uri : {} will redirect to the primary server : {}", requestUri, primaryServerUri);
        return primaryServerUri;

    }

    public List<String> resolveBackupMapping(String requestUri) {
        return resolve(mappingProperties.getBackup(), requestUri)
                .stream()
                .peek(e -> log.debug("Uri request {} match server : {} for the pattern {} ",requestUri,e.getUri(),e.getPattern()))
                .map(Endpoint::getUri).collect(toList());
    }

    private Set<Endpoint> resolve(HashMap<Pattern, String> mapping, String uri) {
        Set<Endpoint> candidates = new HashSet<>();
        if (mapping != null) {
            mapping.forEach((pathRegex, endpointProperty) -> {
                if (pathRegex.matcher(uri).matches()) {
                    candidates.add(new Endpoint(pathRegex, endpointProperty));
                }
            });
        }

        return candidates;
    }

    private String bestMatchingPattern(Set<Endpoint> candidates) {
        Assert.notEmpty(candidates, "At least one candidate must be provided as the primary server ");
        if(candidates.size()==1) {
            return candidates.iterator().next().getUri();
        }
        return candidates.stream().max(Comparator.comparingInt(e -> e.getPattern().toString().length()))
                         .orElseThrow(() -> new IllegalStateException("Failed to find the best match for the primary     server"))
                         .getUri();
    }


    static class Endpoint {
        private Pattern pattern;
        private String uri;

        public Endpoint(Pattern pattern, String uri) {
            this.pattern = pattern;
            this.uri = uri;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public String getUri() {
            return uri;
        }

        // Endpoint is defined by its uri,
        // this avoids any possible duplication if a uri is linked to several patterns
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Endpoint endpoint = (Endpoint) o;
            return Objects.equals(uri, endpoint.uri);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri);
        }
    }
}
