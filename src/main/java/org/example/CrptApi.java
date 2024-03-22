package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private Document document;
    private Description description;
    private Product product;

    private int requestCounter = 0;
    private int requestLimit;
    private static long timeLimitMillis;
    private long lastRequestTime = 0;
    ObjectMapper objectMapper = new ObjectMapper();
    HttpClient httpClient = HttpClientBuilder.create().build();
    private static final Logger logger = LoggerFactory.getLogger(CrptApi.class);

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeLimitMillis = timeUnit.toMillis(1);
        this.requestLimit = requestLimit;
    }

    public synchronized void createDocument(CrptApi crptApi, String signature) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastRequestTime >= timeLimitMillis) {
            requestCounter = 0;
            lastRequestTime = currentTime;
        }
        if (requestCounter >= requestLimit) {
//           Блокировать вызов, если достигнут лимит.
            return;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(crptApi);
//          Лог перед отправкой запроса, чтобы зафиксировать данные документа, который будет отправлен.
            logger.info("Request body: {}", requestBody);

            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");

            httpPost.setHeader("Content-Type", "application/json");

            StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);

            httpPost.setEntity(entity);

            HttpResponse httpResponse = httpClient.execute(httpPost);

//            Получаем ответ и его содержимое.
            HttpEntity responseEntity = httpResponse.getEntity();

            int statusCode = httpResponse.getStatusLine().getStatusCode();

            if (statusCode == 200) {
//                Лог успешного запроса.
                logger.info("The request was successful. Status code: " + statusCode);
            } else {
//                 Лог ошибочного запроса.
                logger.error("The request was not successful. Status code: " + statusCode);
            }
        } catch (IOException e) {
            logger.error("An error occurred while creating the document", e);
        }

//         Обновление значения до текущего времени, чтобы отразить, что запрос уже был выполнен.
        lastRequestTime = currentTime;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Document {
        private String docId;
        private String docStatus;
        private String docType;
        @JsonProperty("importRequest")
        private Boolean importRequest;
        private String ownerInn;
        private String ownerStatus;
        private String participantInn;
        private String producerInn;
        private LocalDate productionDate;
        private String productionType;
        private List<Product> products;
        private LocalDate regDate;
        private String regNumber;
    }

    @Getter
    @Setter
    static class Description {
        private String participantInn;
    }

    @Getter
    @Setter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    static class Product {
        private String certificateDocument;
        private LocalDate certificateDocumentDate;
        private String certificateDocumentNumber;
        private String ownerInn;
        private String producerInn;
        private LocalDate productionDate;
        private String tnvedCode;
        private String uitCode;
        private String uituCode;
    }
}
