package com.example.musicplaylist.export.service;

import com.example.musicplaylist.common.exception.BusinessException;
import com.example.musicplaylist.common.exception.ErrorCode;
import com.example.musicplaylist.export.strategy.CsvExportStrategy;
import com.example.musicplaylist.export.strategy.ExportStrategy;
import com.example.musicplaylist.export.strategy.JsonExportStrategy;
import com.example.musicplaylist.export.strategy.M3uExportStrategy;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExportStrategyRegistryTest {

    private final ExportStrategy jsonStrategy = new JsonExportStrategy(new ObjectMapper());
    private final ExportStrategy m3uStrategy = new M3uExportStrategy();
    private final ExportStrategy csvStrategy = new CsvExportStrategy();

    private final ExportStrategyRegistry registry = new ExportStrategyRegistry(List.of(jsonStrategy, m3uStrategy, csvStrategy));

    @Test
    void shouldRegisterAndRetrieveExportStrategies() {
        assertThat(registry.getStrategy("json")).isSameAs(jsonStrategy);
        assertThat(registry.getStrategy("m3u")).isSameAs(m3uStrategy);
        assertThat(registry.getStrategy("csv")).isSameAs(csvStrategy);

        assertThat(registry.getAvailableFormats()).containsExactly("csv", "json", "m3u");
    }

    @Test
    void shouldRejectUnsupportedExportFormat() {
        assertThatThrownBy(() -> registry.getStrategy("xml"))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_EXPORT_FORMAT)
                );
    }
}