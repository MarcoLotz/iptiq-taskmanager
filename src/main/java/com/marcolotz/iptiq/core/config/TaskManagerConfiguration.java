package com.marcolotz.iptiq.core.config;

import com.marcolotz.iptiq.core.PriorityQueueTaskManager;
import com.marcolotz.iptiq.core.SystemTimeProvider;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.iptiq.ports.TimeProvider;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class TaskManagerConfiguration {

    @Bean
    public TaskManager taskManager(final TaskManagerConfigurationProperties taskManagerConfigurationProperties,
                                   final TimeProvider timeProvider) {
        return new PriorityQueueTaskManager(taskManagerConfigurationProperties.getCapacity(), timeProvider);
    }

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }


    @Bean
    @ConfigurationProperties("taskmanager")
    public TaskManagerConfigurationProperties taskManagerConfigurationProperties() {
        return new TaskManagerConfigurationProperties();
    }
}
