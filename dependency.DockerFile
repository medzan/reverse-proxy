
FROM openjdk:8-jre-alpine

RUN addgroup -S kapelse && adduser -S kapelse -G kapelse
USER kapelse:kapelse

ARG WORKSPACE=/home/kapelse/workspace
WORKDIR ${WORKSPACE}

ARG LOCAL_DEPENDENCY=build/libs/dependency
COPY ${LOCAL_DEPENDENCY}/BOOT-INF/lib ${WORKSPACE}/app/lib
COPY ${LOCAL_DEPENDENCY}/META-INF ${WORKSPACE}/app/META-INF
COPY ${LOCAL_DEPENDENCY}/BOOT-INF/classes ${WORKSPACE}/app


VOLUME /var/lib/ktmp/config

## The flag -XX:+UseCGroupMemoryLimitForHeap lets the JVM detect what the max heap size in a container should be
## java.security.egd=file:/dev/./urandom : recomended approach is to use /dev/urandom for random number generation,
## it will never block as opposit to the default /dev/random
ENTRYPOINT ["java","-Dconfig.resource.path=/var/lib/ktmp/config","-XX:+UnlockExperimentalVMOptions","-XX:+UseCGroupMemoryLimitForHeap","-Djava.security.egd=file:/dev/./urandom","-cp","/home/kapelse/workspace/app:/home/kapelse/workspace/app/lib/*","com.kapelse.ktmp.proxy.Application"]
