package de.codecentric.spring.boot.chaos.monkey.assaults;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class LatencyInducingRequestInterceptor implements ClientHttpRequestInterceptor {

  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    // to be precise, you could start a timer here and then sleep the desired latency minus the
    // time consumed by the real request.

    ClientHttpResponse response = execution.execute(request, body);

    try {
      Thread.sleep(2000);
    } catch (InterruptedException e) {
      // do nothing
    }

    return response;
  }
}
