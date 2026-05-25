package com.harshkhatri.worker.executor.impl;

import com.harshkhatri.worker.enums.JobType;
import com.harshkhatri.worker.executor.JobExecutor;
import com.harshkhatri.worker.model.JobTriggerPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class HttpWebhookExecutor implements JobExecutor {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Override
    public String execute(JobTriggerPayload payload) throws Exception {
        Map<String, Object> config = payload.jobConfig();

        String url = (String) config.get("url");
        String method = ((String) config.getOrDefault("method", "POST")).toUpperCase();
        String body = (String) config.getOrDefault("body", "");

        log.info("Executing HTTP job={} method={} url={}", payload.jobId(), method, url);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(payload.timeoutSeconds()));

        if (config.containsKey("headers")) {
            Map<String, String> headers = (Map<String, String>) config.get("headers");
            headers.forEach(requestBuilder::header);
        }

        HttpRequest request = switch (method) {
            case "POST" -> requestBuilder.POST(
                    HttpRequest.BodyPublishers.ofString(body)).build();
            case "PUT" -> requestBuilder.PUT(
                    HttpRequest.BodyPublishers.ofString(body)).build();
            case "GET" -> requestBuilder.GET().build();
            case "DELETE" -> requestBuilder.DELETE().build();
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        };

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());

        log.info("HTTP job={} responded with status={}", payload.jobId(), response.statusCode());

        if (response.statusCode() >= 400) {
            throw new RuntimeException("HTTP error " + response.statusCode()
                    + ": " + response.body());
        }

        return response.body();
    }

    @Override
    public JobType supports() {
        return JobType.HTTP_WEBHOOK;
    }
}
