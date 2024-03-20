package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CrptApi {

    private Description description;
    private Product product;

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

    static class CrptApiService {
        private int requestCounter = 0;
        private int requestLimit;
        private static long timeLimitMillis;
        private long lastRequestTime = 0;

        public synchronized void createDocument(CrptApi crptApi, String signature) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastRequestTime >= timeLimitMillis) {
                requestCounter = 0;
                lastRequestTime = currentTime;
            }
            if (requestCounter >= requestLimit) {
                return; // Блокировать вызов, если достигнут лимит
            }

            requestCounter++;
            lastRequestTime = currentTime;
        }
    }
}

