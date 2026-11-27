package com.example.musicplaylist.export.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.export.strategy.ExportStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExportStrategyRegistry {

    private final Map<String, ExportStrategy> strategies;

    public ExportStrategyRegistry(List<ExportStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(ExportStrategy::getFormat, strategy -> strategy));
    }

    public ExportStrategy getStrategy(String format) {
        if (format == null) {
            throw new BusinessException(ErrorCode.INVALID_EXPORT_FORMAT);
        }

        ExportStrategy strategy = strategies.get(format);

        if (strategy == null) {
            throw new BusinessException(ErrorCode.INVALID_EXPORT_FORMAT);
        }

        return strategy;
    }

    public List<String> getAvailableFormats() {
        return strategies.keySet().stream()
                .sorted()
                .toList();
    }
}