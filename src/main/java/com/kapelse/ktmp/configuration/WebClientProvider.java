package com.kapelse.ktmp.configuration;


import com.kapelse.ktmp.RequestQueueSender;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.client.reactive.ReactorResourceFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;

import static com.kapelse.ktmp.helpers.Utils.toMillis;
import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static java.time.Duration.ofSeconds;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.web.reactive.function.client.WebClient.builder;
import static reactor.netty.http.client.HttpClient.create;

@Component
public class WebClientProvider {

    private WebClient webClient;
    private Duration connection;
    private Duration read;
    private Duration write;

    WebClientProvider( ) {
        connection = ofSeconds(10);
        read = ofSeconds(10);
        write = ofSeconds(10);
        webClient = buildWebClient();
    }

    private WebClient buildWebClient() {
        return builder()
                .clientConnector(createConnector())
                .build();
    }


    public ClientHttpConnector createConnector() {
        return new ReactorClientHttpConnector(create() .followRedirect(false)
                                                      .tcpConfiguration(client -> client.option(CONNECT_TIMEOUT_MILLIS, toMillis(connection))
                                                        .doOnConnected(connection -> connection
                                                        .addHandlerLast(new ReadTimeoutHandler(read.toMillis(), MILLISECONDS))
                                                        .addHandlerLast(new WriteTimeoutHandler(write.toMillis(),
                                                        MILLISECONDS)))));
    }
    public WebClient getDefaultWebClient() {
        return webClient;
    }

}
