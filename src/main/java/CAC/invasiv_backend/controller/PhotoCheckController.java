package CAC.invasiv_backend.controller;

import CAC.invasiv_backend.dto.PhotoCheckRequest;
import CAC.invasiv_backend.dto.IdentificationResult;
import CAC.invasiv_backend.dto.ChatRequest;
import CAC.invasiv_backend.dto.ChatResponse;
import CAC.invasiv_backend.repository.IdentificationResultRepository;
import CAC.invasiv_backend.service.IdentificationService;
import CAC.invasiv_backend.service.AiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class PhotoCheckController {

    private final IdentificationService identificationService;
    private final IdentificationResultRepository repository;
    private final AiService aiService;

    public PhotoCheckController(IdentificationService identificationService, IdentificationResultRepository repository, AiService aiService) {
        this.identificationService = identificationService;
        this.repository = repository;
        this.aiService = aiService;
    }
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        List<IdentificationResult> results = identificationService.getAllIdentificationResults();
        String context = formatIdentificationContext(results);
        String response = aiService.chatWithContext(request.getMessage(), context);
        return new ChatResponse(response);
    }

    private String formatIdentificationContext(List<IdentificationResult> results) {
        if (results.isEmpty()) {
            return "No previous identification results available.";
        }
        return results.stream()
            .map(result -> String.format(
                "Species: %s\nDescription: %s\nDate: %s",
                result.getSpeciesName(),
                result.getDescription(),
                result.getDate()
            ))
            .collect(Collectors.joining("\n\n"));
    }

    @PostMapping("/check")
    public ResponseEntity<IdentificationResult> checkPhoto(@RequestBody PhotoCheckRequest request) {
        try {
            IdentificationResult result = identificationService.identifySpecies(request);
            repository.save(result);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            IdentificationResult errorResult = new IdentificationResult();
            errorResult.Error(e);
            return ResponseEntity.badRequest().body(errorResult);
        }
    }

    @GetMapping("/results")
    public ResponseEntity<List<IdentificationResult>> getAllResults() {
        List<IdentificationResult> allResults = repository.findAll();

        return ResponseEntity.ok(allResults);
    }
}