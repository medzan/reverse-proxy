package com.kapelse.ktmp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.regex.Pattern;

@Configuration
@EnableConfigurationProperties(MappingProperties.class)
@ConfigurationProperties(prefix = "mapping")
public class MappingProperties {

    private HashMap<Pattern, String> main;
    private HashMap<Pattern, String> backup;


    public HashMap<Pattern, String> getMain() {
        return main;
    }

    public void setMain(HashMap<Pattern, String> main) {
        this.main = main;
    }

    public HashMap<Pattern, String> getBackup() {
        return backup;
    }

    public void setBackup(HashMap<Pattern, String> backup) {
        this.backup = backup;
    }
}
