package com.kapelse.ktmp.proxy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class ExternalPropertySourcesConfig {

    public final static String CONFIG_RESOURCE_PATH = "config.resource.path";

    private ConfigurableEnvironment environment;

    @PostConstruct
    public void loadCustomPropertyFiles() throws IOException {
        MutablePropertySources propertySources = environment.getPropertySources();
        Resource[] resources = loadExternalResourceFiles();
        YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
        yamlFactory.setResources(resources);
        Properties properties = yamlFactory.getObject();
        propertySources.addFirst(new PropertiesPropertySource("application.yml", properties));
    }


    @Autowired
    public void setEnvironment(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    private static Resource[] loadExternalResourceFiles() {
        String file = System.getProperty(ExternalPropertySourcesConfig.CONFIG_RESOURCE_PATH);
        if(file == null || file.length() ==0 ){
            throw new IllegalStateException("Config resource path not found ");
        }
        File[] resourceFiles = new File(file).listFiles();

        if (resourceFiles == null || resourceFiles.length == 0) {
            throw new IllegalStateException("Resource files not found in " + ExternalPropertySourcesConfig.CONFIG_RESOURCE_PATH);
        }
        return Stream.of(resourceFiles)
                     .map(FileSystemResource::new)
                     .collect(Collectors.toList())
                     .toArray(new Resource[]{});
    }


}
