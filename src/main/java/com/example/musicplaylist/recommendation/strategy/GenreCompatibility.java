package com.example.musicplaylist.recommendation.strategy;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GenreCompatibility {

    private static final Map<String, Map<String, Double>> COMPATIBILITY =
            Map.of(
                    "Rock", Map.of(
                            "Pop", 0.8,
                            "Jazz", 0.4,
                            "Electronic", 0.5
                    ),
                    "Pop", Map.of(
                            "Hip Hop", 0.6,
                            "Electronic", 0.8
                    ),
                    "Jazz", Map.of(
                            "Hip Hop", 0.5,
                            "Classical", 0.6
                    ),
                    "Hip Hop", Map.of(
                            "Electronic", 0.7
                    )
            );

    public double getCompatibility(String source, String target) {
        if (source.equals(target)) {
            return 1.0;
        }

        Double compatibility = COMPATIBILITY
                .getOrDefault(source, Map.of())
                .get(target);

        if (compatibility != null) {
            return compatibility;
        }

        return COMPATIBILITY
                .getOrDefault(target, Map.of())
                .getOrDefault(source, 0.0);
    }
}