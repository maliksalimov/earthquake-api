package com.malik.earthquakeapi.service;

import com.malik.earthquakeapi.dto.CreateEarthquakeRequest;
import com.malik.earthquakeapi.dto.EarthquakeResponse;
import com.malik.earthquakeapi.dto.UpdateEarthquakeRequest;
import com.malik.earthquakeapi.entity.Earthquake;
import com.malik.earthquakeapi.exception.ResourceNotFoundException;
import com.malik.earthquakeapi.repository.EarthquakeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EarthquakeService {

    private final EarthquakeRepository earthquakeRepository;

    private static final int MAX_PAGE_SIZE = 20;


    @Cacheable(value = "earthquakes", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()")
    public Page<EarthquakeResponse> getAllEarthquakes(int page, int size, String sort) {

        if(size > MAX_PAGE_SIZE){
            size = MAX_PAGE_SIZE;
        }

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        Page<Earthquake> earthquakes = earthquakeRepository.findAll(pageable);

        return earthquakes.map(this::mapToResponse);
    }

    @Cacheable(value = "earthquake", key = "#id")
    public EarthquakeResponse getEarthquakeById(Long id){
        Earthquake earthquake = earthquakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Earthquake not found for id: " + id));

        return mapToResponse(earthquake);
    }

    @CacheEvict(value = "earthquakes", allEntries = true)
    @Transactional
    public EarthquakeResponse create(CreateEarthquakeRequest request){
        Earthquake earthquake = Earthquake.builder()
                .magnitude(request.getMagnitude())
                .location(request.getLocation())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .depth(request.getDepth())
                .time(request.getTime())
                .tsunamiAlert(request.getTsunamiAlert())
                .type(request.getType())
                .build();

        Earthquake savedEarthquake = earthquakeRepository.save(earthquake);
        return mapToResponse(savedEarthquake);
    }

    @CachePut(value = "earthquake", key = "#id")
    @CacheEvict(value = "earthquakes", allEntries = true)
    @Transactional
    public EarthquakeResponse update(Long id, UpdateEarthquakeRequest request){
        Earthquake earthquake = earthquakeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Earthquake not found for id: " + id));

        if(request.getMagnitude() != null) {
            earthquake.setMagnitude(request.getMagnitude());
        }
        if(request.getLocation() != null) {
            earthquake.setLocation(request.getLocation());
        }
        if(request.getLatitude() != null) {
            earthquake.setLatitude(request.getLatitude());
        }
        if(request.getLongitude() != null) {
            earthquake.setLongitude(request.getLongitude());
        }
        if(request.getDepth() != null) {
            earthquake.setDepth(request.getDepth());
        }
        if(request.getTime() != null) {
            earthquake.setTime(request.getTime());
        }
        if(request.getTsunamiAlert() != null) {
            earthquake.setTsunamiAlert(request.getTsunamiAlert());
        }
        if(request.getType() != null) {
            earthquake.setType(request.getType());
        }

        Earthquake updatedEarthquake = earthquakeRepository.save(earthquake);

        return mapToResponse(updatedEarthquake);
    }

    @Transactional
    @CacheEvict(value = {"earthquakes", "earthquake"}, allEntries = true)
    public void deleteEarthquake(Long id){
        if(!earthquakeRepository.existsById(id)){
            throw new ResourceNotFoundException("Earthquake not found for id: " + id);
        }

        earthquakeRepository.deleteById(id);
    }


    private EarthquakeResponse mapToResponse(Earthquake earthquake){
        return EarthquakeResponse.builder()
                .id(earthquake.getId())
                .magnitude(earthquake.getMagnitude())
                .location(earthquake.getLocation())
                .latitude(earthquake.getLatitude())
                .longitude(earthquake.getLongitude())
                .depth(earthquake.getDepth())
                .time(earthquake.getTime())
                .tsunamiAlert(earthquake.getTsunamiAlert())
                .type(earthquake.getType())
                .build();
    }
}
