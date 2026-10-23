package br.com.system.services;

import br.com.system.data.dto.request.AssistantRequestDTO;
import br.com.system.data.dto.response.AssistantResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.model.Product;
import br.com.system.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Service
public class AssistantService {
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public AssistantService(
            ProductRepository productRepository,
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model) {
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AssistantResponseDTO chat(AssistantRequestDTO request) {
        if (apiKey.isBlank()) {
            throw new BusinessException("OPENAI_API_KEY is not configured!");
        }

        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", instructions()),
                            Map.of("role", "user", "content", request.message())
                    )
            ));

            HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new BusinessException("The AI assistant is unavailable!");
            }

            JsonNode json = objectMapper.readTree(response.body());
            String answer = json.path("choices").path(0).path("message").path("content").asText();
            if (answer.isBlank()) {
                throw new BusinessException("The AI assistant returned an empty response!");
            }
            return new AssistantResponseDTO(answer);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("The AI assistant is unavailable!");
        }
    }

    private String instructions() throws Exception {
        List<Map<String, Object>> products = productRepository
                .findAll(PageRequest.of(0, 100, Sort.by("name")))
                .stream()
                .map(this::productData)
                .toList();
        return "You are a concise inventory assistant. Answer only inventory and sales questions using the provided data. Do not invent facts, execute actions, or claim to change data. Inventory data: "
                + objectMapper.writeValueAsString(products);
    }

    private Map<String, Object> productData(Product product) {
        return Map.of(
                "name", product.getName(),
                "quantity", product.getQuantity(),
                "salePrice", product.getSalePrice(),
                "active", product.getActive()
        );
    }
}
