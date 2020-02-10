/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.codecentric.spring.boot.chaos.monkey.assaults;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

/** @author Benjamin Wilms */
@ExtendWith(MockitoExtension.class)
class WebClientAssaultTest {

  ExchangeFilterFunction serverErrorFilterFunction =
      (clientRequest, nextFilter) -> {
        ClientResponse response = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();

        return Mono.just(response);
      };

  @Test
  void assaultWebClientCallWithFailureWhenBuildWithBuilder() {
    WebClient webClient = WebClient.builder().filter(serverErrorFilterFunction).build();

    Assertions.assertThatThrownBy(
        () ->
                webClient
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/todos/1")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block())
        .isInstanceOf(WebClientResponseException.class)
        .hasMessage("500 Internal Server Error");
  }

  @Test
  void assaultWebClientCallWithFailureWhenCreatedDirectly() {
    WebClient webClient = WebClient.create();

    WebClient mutatedWebClient = webClient.mutate().filter(serverErrorFilterFunction).build();

    Assertions.assertThatThrownBy(
        () ->
                mutatedWebClient
                    .get()
                    .uri("https://jsonplaceholder.typicode.com/todos/1")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block())
        .isInstanceOf(WebClientResponseException.class)
        .hasMessage("500 Internal Server Error");
  }

  ExchangeFilterFunction latencyAddingFilterFunction =
      (clientRequest, nextFilter) -> {
        return nextFilter.exchange(clientRequest).delayElement(Duration.ofSeconds(2));
      };

  @Test
  void assaultWebClientCallWithLatency() {
    WebClient webClient = WebClient.builder().filter(latencyAddingFilterFunction).build();

    Instant start = Instant.now();

    webClient
        .get()
        .uri("https://jsonplaceholder.typicode.com/todos/1")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    Instant end = Instant.now();

    Duration d = Duration.between(start, end);

    Assertions.assertThat(d.toMillis()).isCloseTo(2000L, Offset.offset(1000L));
  }
}
