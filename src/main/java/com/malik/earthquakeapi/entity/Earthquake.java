package com.malik.earthquakeapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;


@Entity
@Table(name = "earthquakes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Earthquake implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private Double magnitude;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(nullable = false)
    private Double depth;

    @Column(nullable = false)
    private LocalDateTime time;

    private String tsunamiAlert;

    private String type;

}
