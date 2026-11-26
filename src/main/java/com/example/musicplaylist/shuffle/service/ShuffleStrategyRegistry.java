package com.example.musicplaylist.shuffle.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.shuffle.strategy.ShuffleStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ShuffleStrategyRegistry {

    private final Map<String, ShuffleStrategy> strategies;

    public ShuffleStrategyRegistry(List<ShuffleStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        ShuffleStrategy::getName,
                        strategy -> strategy
                ));
    }

    public ShuffleStrategy getStrategy(String name) {
        ShuffleStrategy strategy = strategies.get(name);

        if (strategy == null) {
            throw new BusinessException(ErrorCode.INVALID_SHUFFLE_STRATEGY);
        }

        return strategy;
    }

    public List<String> getAvailableStrategies() {
        return strategies.keySet().stream()
                .sorted()
                .toList();
    }
}