package com.marcolotz.iptiq.adapters.inbound.rest;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DtoToDomainConfiguration {

    @Bean
    DtoToDomainMapper dtoToDomainMapper() {
        return Mappers.getMapper(DtoToDomainMapper.class);
    }
}
