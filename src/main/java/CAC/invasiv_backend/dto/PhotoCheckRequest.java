package CAC.invasiv_backend.dto;

import lombok.Data;

@Data
public class PhotoCheckRequest {
    private String imageBase64;
    private double latitude;
    private double longitude;
    private String date;
}