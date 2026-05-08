package com.malik.earthquakeapi.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEarthquakeRequest {
    @DecimalMin(value = "0", message = "Magnitude must be greater than 0")
    @DecimalMax(value = "10", message = "Magnitude must be less than 10")
    private Double magnitude;

    private String location;

    @DecimalMin(value = "-90", message = "Latitude must be greater than -90")
    @DecimalMax(value = "90", message = "Latitude must be less than 90")
    private Double latitude;

    @DecimalMin(value = "-180", message = "Longitude must be greater than -180")
    @DecimalMax(value = "180", message = "Longitude must be less than 180")
    private Double longitude;

    @DecimalMin(value = "0", message = "Depth must be greater than 0")
    private Double depth;

    private LocalDateTime time;

    private String tsunamiAlert;

    private String type;
}
