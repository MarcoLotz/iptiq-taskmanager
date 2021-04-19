package com.marcolotz.iptiq.core.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;


@Data
@Validated
public class TaskManagerConfigurationProperties {
    private @Min(1) int capacity;
}
