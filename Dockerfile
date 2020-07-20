FROM openjdk:8-jre-alpine
RUN addgroup -S kapelse && adduser -S kapelse -G kapelse
USER kapelse:kapelse

VOLUME /var/lib/ktmp/config

COPY build/libs/*.jar ktmp-proxy.jar

## The flag -XX:+UseCGroupMemoryLimitForHeap lets the JVM detect what the max heap size in a container should be
## java.security.egd=file:/dev/./urandom : recomended approach is to use /dev/urandom for random number generation,
## it will never block as opposit to the default /dev/random
ENTRYPOINT ["java","-Dconfig.resource.path=/var/lib/ktmp/config","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-Djava.security.egd=file:/dev/./urandom","-jar","/ktmp-proxy.jar"]