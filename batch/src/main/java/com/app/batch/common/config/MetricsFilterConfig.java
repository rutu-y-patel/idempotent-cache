package com.app.batch.common.config;

import com.app.batch.common.constant.BatchConstant;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsFilterConfig {

    @Bean
    public MeterFilter allowSome() {
        return MeterFilter.denyUnless(id ->
                        id.getName().startsWith(BatchConstant.IDEMPOTENCY)
                                || id.getName().startsWith(BatchConstant.HTTP) // Allow HTTP metrics
                                || id.getName().startsWith(BatchConstant.JVM)  // Allow JVM metrics
        );
    }
}