package CAC.invasiv_backend.service;

import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

@Service
public class InvasiveSpeciesService {

    private Set<String> invasiveIndex;

    @PostConstruct
    void init() {
        String invasiveSpeciesData = loadResourceAsString("mappings")
                .orElseGet(() -> loadResourceAsString("mappings.csv").orElse(""));
        if (invasiveSpeciesData.isEmpty()) {
            throw new IllegalStateException("Could not load invasive species data from classpath " +
                    "resources 'mappings' or 'mappings.csv'.");
        }
        
        this.invasiveIndex = invasiveSpeciesData.lines()
                .map(String::trim)
                .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                .map(line -> line.split(","))
                .filter(columns -> columns.length >= 4)
                .map(columns -> normalize(columns[3]))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public boolean isSpeciesInvasive(String speciesName) {
        if (invasiveIndex == null || invasiveIndex.isEmpty()) {
            return false;
        }
        return invasiveIndex.contains(normalize(speciesName));
    }

    private java.util.Optional<String> loadResourceAsString(String resourceName) {
        try {
            ClassPathResource resource = new ClassPathResource(resourceName);
            if (!resource.exists()) {
                return java.util.Optional.empty();
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return java.util.Optional.of(reader.lines().collect(Collectors.joining("\n")));
            }
        } catch (Exception e) {
            return java.util.Optional.empty();
        }
    }

    private static String normalize(String s) {
        if (s == null) {
            return "";
        }
        return s.trim().toLowerCase().replaceAll("^\"|\"$", "");
    }
}