package com.malik.earthquakeapi.loader;

import com.malik.earthquakeapi.entity.Earthquake;
import com.malik.earthquakeapi.repository.EarthquakeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.env.Environment;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final EarthquakeRepository earthquakeRepository;
    private final Environment environment;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        boolean isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");

        if (isProd) {
            log.info("Production environment - skipping CSV data load");
            return;
        }


        long count = earthquakeRepository.count();
        if (count > 0) {
            log.info("There is already {} earthquakes in database", count);
            return;
        }


        ClassPathResource resource = new ClassPathResource("earthquakes.csv");

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, csvFormat)) {

            int successCount = 0;
            int errorCount = 0;

            for (CSVRecord record : csvParser) {
                try {
                    Earthquake earthquake = parseRecord(record);
                    if (earthquake != null) {
                        earthquakeRepository.save(earthquake);
                        successCount++;

                        if (successCount % 100 == 0) {
                            log.info("Loaded: {} earthquakes", successCount);
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    if (errorCount < 5) {
                        log.warn("Line couldn't be parsed: {}", e.getMessage());
                    }
                }
            }

            log.info("CSV load complete: {} success, {} error", successCount, errorCount);
        }
    }

    private Earthquake parseRecord(CSVRecord record) {
        try {
            String timeStr = record.get("time");
            double latitude = Double.parseDouble(record.get("latitude"));
            double longitude = Double.parseDouble(record.get("longitude"));
            double depth = Double.parseDouble(record.get("depth"));
            double magnitude = Double.parseDouble(record.get("mag"));
            String place = record.get("place");
            String type = record.get("type");

            int tsunami = 0;
            try {
                tsunami = Integer.parseInt(record.get("tsunami"));
            } catch (Exception ignored) {
            }

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime time = LocalDateTime.parse(timeStr, formatter);

            return Earthquake.builder()
                    .time(time)
                    .latitude(latitude)
                    .longitude(longitude)
                    .depth(depth)
                    .magnitude(magnitude)
                    .location(place != null && !place.isEmpty() ? place : "Unknown location")
                    .type(type)
                    .tsunamiAlert(tsunami == 1 ? "Yes" : "No")
                    .build();

        } catch (Exception e) {
            return null;
        }
    }
}