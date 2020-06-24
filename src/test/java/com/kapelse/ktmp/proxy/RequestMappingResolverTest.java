package com.kapelse.ktmp.proxy;

import com.kapelse.ktmp.proxy.config.MappingProperties;
import com.kapelse.ktmp.proxy.filter.RequestMappingResolver;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author ZANGUI Elmehdi
 */

public class RequestMappingResolverTest {

    @Test
    public void whenMultipleMainPattern_thenSelectLongest() {

        MappingProperties mappingProperties = new MappingProperties();
        HashMap<Pattern, String> primary = new HashMap<>();
        mappingProperties.setPrimary(primary);
        RequestMappingResolver  resolver = new RequestMappingResolver(mappingProperties);

        String defaultServerUri = "http://127.0.0.1:8080";
        String specificServerUri = "http://127.0.0.1:9080";

        primary.put(Pattern.compile("/.*"), defaultServerUri);
        primary.put(Pattern.compile("/iaas/.*"), specificServerUri);
        String specificRequest ="/iaas/device";
        String resolvedUri = resolver.resolvePrimaryMapping(specificRequest);
        assertThat(resolvedUri).isEqualTo(specificServerUri);

    }
    @Test
    public void whenSameUriForMultiplePattern_thenNoExceptionAndOnlyOneUriReturned(){
        MappingProperties mappingProperties = new MappingProperties();
        HashMap<Pattern, String> primary = new HashMap<>();
        mappingProperties.setPrimary(primary);
        RequestMappingResolver  resolver = new RequestMappingResolver(mappingProperties);

        String defaultServerUri = "http://127.0.0.1:8080";

        primary.put(Pattern.compile("/.*"), defaultServerUri);
        primary.put(Pattern.compile("/iaas/.*"), defaultServerUri);
        String specificRequest ="/iaas/device";
        String resolvedUri = resolver.resolvePrimaryMapping(specificRequest);

        assertThat(resolvedUri).isEqualTo(defaultServerUri);
    }
     @Test
    public void whenMultipleBackupUri_thenUriReturnedPerPattern(){
         MappingProperties mappingProperties = new MappingProperties();
         HashMap<Pattern, String> backup = new HashMap<>();
         mappingProperties.setBackup(backup);
         RequestMappingResolver  resolver = new RequestMappingResolver(mappingProperties);

         String iaasServer = "http://127.0.0.1:8080";
         String deviceServer = "http://127.0.0.1:8090";
         String soapServer = "http://127.0.0.1:8099";

         backup.put(Pattern.compile("/iaas/.*"), iaasServer);
         backup.put(Pattern.compile("/iaas/device/.*"), deviceServer);
         backup.put(Pattern.compile("/iaas/soap/.*"), soapServer);

         String deviceUri ="/iaas/device/";
         List<String> deviceUriResolved = resolver.resolveBackupMapping(deviceUri);
         assertThat(deviceUriResolved).hasSize(2);
         assertThat(deviceUriResolved).containsExactlyInAnyOrder(iaasServer, deviceServer);

         String soapUri ="/iaas/soap/";
         List<String> soapUriResolved = resolver.resolveBackupMapping(soapUri);
         assertThat(soapUriResolved).hasSize(2);
         assertThat(soapUriResolved).containsExactlyInAnyOrder(iaasServer, soapServer);


     }
    @Test
    public void whenMultipleBackupUriForSamePatten_thenNoDuplicateUriReturned(){
        MappingProperties mappingProperties = new MappingProperties();
        HashMap<Pattern, String> backup = new HashMap<>();
        mappingProperties.setBackup(backup);
        RequestMappingResolver  resolver = new RequestMappingResolver(mappingProperties);

        String iaasServer = "http://127.0.0.1:8080";

        backup.put(Pattern.compile("/iaas/.*"), iaasServer);
        backup.put(Pattern.compile("/iaas/device/.*"), iaasServer);

        String deviceUri ="/iaas/device/";
        List<String> deviceUriResolved = resolver.resolveBackupMapping(deviceUri);
        assertThat(deviceUriResolved).hasSize(1);
        assertThat(deviceUriResolved).containsExactlyInAnyOrder(iaasServer);
;


    }

}
