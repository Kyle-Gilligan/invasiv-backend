package CAC.invasiv_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name = "identification_result")
public class IdentificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty("speciesName")
    @Column(name = "species_name")
    private String speciesName;

    @JsonProperty("description")
    @Lob
    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "date")
    private String date;

    @JsonProperty("isInvasive")
    @Column(name = "is_invasive")
    private boolean isInvasive;

    private Double latitude;
    private Double longitude;
    
    @ElementCollection
    private List<String> commonNames;

    public IdentificationResult() {}

    public IdentificationResult(String speciesName, String description, String date, Double latitude, Double longitude, List<String> commonNames, boolean isInvasive) {
        this.speciesName = speciesName;
        this.description = description;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.commonNames = commonNames;
        this.isInvasive = isInvasive;
    }

    public void Error(Exception e) {
        speciesName = "Error";
    }
}