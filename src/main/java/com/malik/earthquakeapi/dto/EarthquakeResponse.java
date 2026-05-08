package com.malik.earthquakeapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarthquakeResponse implements Serializable {
    private Long id;
    private Double magnitude;
    private String location;
    private Double latitude;
    private Double longitude;
    private Double depth;
    private LocalDateTime time;
    private String tsunamiAlert;
    private String type;
}
