package com.malik.earthquakeapi.repository;

import com.malik.earthquakeapi.entity.Earthquake;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EarthquakeRepository extends JpaRepository<Earthquake, Long> {
    Page<Earthquake> findAll(Pageable pageable);
}