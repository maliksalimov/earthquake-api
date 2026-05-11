package com.malik.earthquakeapi.controller;

import com.malik.earthquakeapi.dto.CreateEarthquakeRequest;
import com.malik.earthquakeapi.dto.EarthquakeResponse;
import com.malik.earthquakeapi.dto.RestPage;
import com.malik.earthquakeapi.dto.UpdateEarthquakeRequest;
import com.malik.earthquakeapi.service.EarthquakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/earthquake")
@RequiredArgsConstructor
public class EarthquakeController {
    private final EarthquakeService earthquakeService;

    @GetMapping
    public ResponseEntity<RestPage<EarthquakeResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        RestPage<EarthquakeResponse> earthquakes = earthquakeService.getAllEarthquakes(page, size, sort);
        return ResponseEntity.ok(earthquakes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EarthquakeResponse> getById(@PathVariable Long id) {
        EarthquakeResponse earthquake = earthquakeService.getEarthquakeById(id);
        return ResponseEntity.ok(earthquake);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EarthquakeResponse> create(@Valid @RequestBody CreateEarthquakeRequest request) {
        EarthquakeResponse earthquake = earthquakeService.create(request);
        return new ResponseEntity<>(earthquake, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EarthquakeResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateEarthquakeRequest request) {
        EarthquakeResponse earthquake = earthquakeService.update(id, request);
        return ResponseEntity.ok(earthquake);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        earthquakeService.deleteEarthquake(id);
        return ResponseEntity.noContent().build();
    }
}
