package com.countryinfo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class ObservabilityConfig
{
    @Bean
    CommonsRequestLoggingFilter requestLoggingFilter()
    {
        var filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setIncludeHeaders(false);
        filter.setMaxPayloadLength(1000);
        return filter;
    }
}