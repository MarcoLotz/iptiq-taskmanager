package com.marcolotz.iptiq.adapters.inbound.rest;

import com.marcolotz.iptiq.ports.TaskManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TaskManagerControllerConfiguration {

    @Bean
    TaskManager asyncTaskManager(final TaskManager taskManager) {
        return new AsyncTaskManager(taskManager);
    }
}
