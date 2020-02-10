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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** @author Benjamin Wilms */
@ExtendWith(MockitoExtension.class)
class RestTemplateAssaultTest {

  private RestTemplate restTemplate = new RestTemplate();

  @Test
  void assaultRestTemplateCallWithFailure() {
    restTemplate.setInterceptors(
        Collections.singletonList(new ResponseModifyingRequestInterceptor()));
    Assertions.assertThatThrownBy(
        () ->
                restTemplate.getForEntity(
                    "https://jsonplaceholder.typicode.com/todos/1", String.class))
        .isInstanceOf(RestClientException.class)
        .hasMessage("500 Internal Server Error");
  }

  @Test
  void assaultRestTemplateCallWithLatency() {
    restTemplate.setInterceptors(
        Collections.singletonList(new LatencyInducingRequestInterceptor()));
    Instant start = Instant.now();
    restTemplate.getForEntity("https://jsonplaceholder.typicode.com/todos/1", String.class);
    Instant end = Instant.now();

    Duration d = Duration.between(start, end);

    Assertions.assertThat(d.toMillis()).isCloseTo(2000L, Offset.offset(500L));
  }
}
