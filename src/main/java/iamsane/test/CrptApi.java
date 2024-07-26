package iamsane.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;

public class CrptApi  implements Closeable {
    private static final String POST_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final BlockingQueue<Long> queue;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final ObjectMapper mapper;

    private volatile boolean isClosed;

    public CrptApi(TimeUnit timeUnit, int requestAmount) {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();

        this.queue = new ArrayBlockingQueue<>(requestAmount, true);
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(queue::clear, 1, 1, timeUnit);
    }

    public CompletableFuture<HttpResponse<Void>> doPost(DocumentDto documentDto) throws IOException, InterruptedException {
        if(isClosed) throw new IllegalStateException("object is closed");
        
        queue.put(System.currentTimeMillis());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(POST_URL))
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(documentDto)))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    public void close() {
        isClosed = true;
        scheduler.shutdownNow();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
class DocumentDto {
    private DescriptionDto description;
    private String docId;
    private String docStatus;
    private String doctype;
    private Boolean importRequest;
    private String ownerInn;
    private String participantInn;
    private String producerInn;
    private String productionDate;
    private String productionType;
    private CertificateDto[] products;
    private String regDate;
    private String regNumber;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class DescriptionDto {
    private String participantInn;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
class CertificateDto {
    private String certificateDocument;
    private String certificateDocumentDate;
    private String certificateDocumentNumber;
    private String ownerInn;
    private String producerInn;
    private String productionDate;
    private String tnvedCode;
    private String uitCode;
    private String uituCode;
}
