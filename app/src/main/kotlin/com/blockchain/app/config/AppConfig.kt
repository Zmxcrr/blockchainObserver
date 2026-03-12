package com.blockchain.app.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration

@Configuration
class AppConfig {

    private val exchangeStrategies = ExchangeStrategies.builder()
        .codecs { configurer ->
            configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
        }
        .build()

    @Bean("tronWebClient")
    fun tronWebClient(
        builder: WebClient.Builder,
        @Value("\${app.tron.grid-url:https://api.trongrid.io}") tronBaseUrl: String,
        @Value("\${app.tron.api-key:}") tronApiKey: String
    ): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
            .responseTimeout(Duration.ofSeconds(10))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(10))
                conn.addHandlerLast(WriteTimeoutHandler(10))
            }

        val clientBuilder = builder
            .exchangeStrategies(exchangeStrategies)
            .baseUrl(tronBaseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        if (tronApiKey.isNotBlank()) {
            clientBuilder.defaultHeader("TRON-PRO-API-KEY", tronApiKey)
        }
        return clientBuilder.build()
    }

    @Bean("ethereumWebClient")
    fun ethereumWebClient(
        builder: WebClient.Builder,
        @Value("\${app.etherscan.base-url:https://api.etherscan.io/api}") ethBaseUrl: String
    ): WebClient {
        val httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5_000)
            .responseTimeout(Duration.ofSeconds(10))
            .doOnConnected { conn ->
                conn.addHandlerLast(ReadTimeoutHandler(10))
                conn.addHandlerLast(WriteTimeoutHandler(10))
            }

        return builder
            .exchangeStrategies(exchangeStrategies)
            .baseUrl(ethBaseUrl)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }
}