package de.codecentric.spring.boot.chaos.monkey.assaults;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class ResponseModifyingRequestInterceptor implements ClientHttpRequestInterceptor {

  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    return new InternalServerErrorHttpResponse();
  }

  class InternalServerErrorHttpResponse extends AbstractClientHttpResponse {
    @Override
    public HttpHeaders getHeaders() {
      return new HttpHeaders();
    }

    @Override
    public InputStream getBody() throws IOException {
      return new ByteArrayInputStream(
          "{\"error\": \"Chaos Monkey for Spring Boot generated failure\"}"
              .getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getRawStatusCode() throws IOException {
      return 500;
    }

    @Override
    public String getStatusText() throws IOException {
      return HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase();
    }

    @Override
    public void close() {
    }
  }
}
