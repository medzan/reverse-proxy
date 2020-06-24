package com.kapelse.ktmp.proxy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @author ZANGUI Elmehdi
 */

@Configuration
@EnableConfigurationProperties(MappingProperties.class)
@ConfigurationProperties(prefix = "mapping")
public class MappingProperties {

    private HashMap<Pattern, String> primary;
    private HashMap<Pattern, String> backup;


    public HashMap<Pattern, String> getPrimary() {
        return primary;
    }

    public void setPrimary(HashMap<Pattern, String> primary) {
        this.primary = primary;
    }

    public HashMap<Pattern, String> getBackup() {
        return backup;
    }

    public void setBackup(HashMap<Pattern, String> backup) {
        this.backup = backup;
    }
}
