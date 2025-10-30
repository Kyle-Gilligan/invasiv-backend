package CAC.invasiv_backend.service;

import CAC.invasiv_backend.dto.PhotoCheckRequest;
import CAC.invasiv_backend.dto.IdentificationResult;
import CAC.invasiv_backend.repository.IdentificationResultRepository;
import CAC.invasiv_backend.utils.Base64Utils;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class IdentificationService {
    private final IdentificationResultRepository repository;

    @Value("${plantid.api.key}")
    private String apiKey;

    @Value("${plantid.api.base-url:https://plant.id/api/v3}")
    private String apiBaseUrl;

    @Value("${plantid.api.details:description,common_names}")
    private String detailsConfig;

    private final WebClient webClient;
    private final InvasiveSpeciesService invasiveSpeciesService;

    public IdentificationService(WebClient.Builder webClientBuilder,
                                 IdentificationResultRepository repository,
                                 InvasiveSpeciesService invasiveSpeciesService) {
        this.webClient = webClientBuilder.build();
        this.repository = repository;
        this.invasiveSpeciesService = invasiveSpeciesService;
    }

    public List<IdentificationResult> getAllIdentificationResults() {
        return repository.findAll();
    }

    public IdentificationResult identifySpecies(PhotoCheckRequest request) {
        String cleanedBase64Image = Base64Utils.cleanBase64String(request.getImageBase64());
        String currentDate = request.getDate();

        String detailsParam = List.of(detailsConfig.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(","));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("images", Collections.singletonList(cleanedBase64Image));
        requestBody.put("latitude", request.getLatitude());
        requestBody.put("longitude", request.getLongitude());

        String uri = UriComponentsBuilder.fromHttpUrl(apiBaseUrl)
                .path("/identification")
                .queryParam("details", detailsParam)
                .toUriString();

        try {
            JsonNode response = this.webClient.post()
                    .uri(uri)
                    .header("Api-Key", apiKey)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("result") && response.get("result").has("classification")) {
                JsonNode classification = response.get("result").get("classification");
                if (classification.has("suggestions") && classification.get("suggestions").isArray() && classification.get("suggestions").size() > 0) {
                    JsonNode firstSuggestion = classification.get("suggestions").get(0);
                    JsonNode detailsNode = firstSuggestion.get("details");

                    String speciesName = firstSuggestion.get("name").asText("Unknown species");
                    String description = "N/A";
                    List<String> commonNames = Collections.emptyList();

                    if (detailsNode != null) {
                        if (detailsNode.has("description") && detailsNode.get("description").isObject()) {
                            JsonNode descriptionNode = detailsNode.get("description");
                            if (descriptionNode.has("value")) {
                                description = descriptionNode.get("value").asText();
                            }
                        } else if (detailsNode.has("description_gpt")) {
                            description = detailsNode.get("description_gpt").asText();
                        }

                        if (detailsNode.has("common_names") && detailsNode.get("common_names").isArray()) {
                            commonNames = StreamSupport.stream(detailsNode.get("common_names").spliterator(), false)
                                    .map(JsonNode::asText)
                                    .collect(Collectors.toList());
                        }
                    }

                    boolean isInvasive = invasiveSpeciesService.isSpeciesInvasive(speciesName);

                    return new IdentificationResult(
                            speciesName,
                            description,
                            currentDate,
                            request.getLatitude(),
                            request.getLongitude(),
                            commonNames,
                            isInvasive
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error calling Plant.id API: " + e.getMessage());
            e.printStackTrace();
            return new IdentificationResult(
                    "Error",
                    "Error during species identification.",
                    currentDate,
                    request.getLatitude(),
                    request.getLongitude(),
                    Collections.emptyList(),
                    false
            );
        }

        return new IdentificationResult(
                "Unknown species",
                "N/A",
                currentDate,
                request.getLatitude(), 
                request.getLongitude(), 
                Collections.emptyList(), 
                false
        );
    }
}