package com.marcolotz.iptiq.adapters.inbound.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcolotz.iptiq.core.SortingMethod;
import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.taskmanager.openapi.model.AddedProcessDTO;
import com.marcolotz.taskmanager.openapi.model.PriorityTypes;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskManagerController.class)
@DisplayName("When using the task manager REST api")
@Import(DtoToDomainConfiguration.class)
// Keep in mind that ComponentScan doesn't play well with WebMvcTest: https://tinyurl.com/27nn5yws
class TaskManagerControllerIT {

    private final ObjectMapper serializer = new ObjectMapper();
    @MockBean
    private TaskManager taskManager;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        reset(taskManager);
    }

    @Test
    @DisplayName("then all running processes will be provided with the required sorting method")
    @SneakyThrows
    void whenValidGetRequest_thenListProcesses() {
        // Given
        doReturn(Collections.emptyList()).when(taskManager).listRunningProcess(SortingMethod.CREATION_TIME);

        final MvcResult result = mockMvc.perform(get("/v1/processes").param("sortingMethod", "CREATION_TIME")
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        // When
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());

        // Then
        verify(taskManager).listRunningProcess(SortingMethod.CREATION_TIME);
    }

    @Test
    @DisplayName("then it will create processes if capacity allows")
    @SneakyThrows
    void whenValidPostRequest_thenCreateProcess() {
        // Given
        AddedProcessDTO addedProcessDTO = new AddedProcessDTO();
        addedProcessDTO.setPriority(PriorityTypes.HIGH);
        doNothing().when(taskManager).addProcess(any());

        final MvcResult result = mockMvc.perform(post("/v1/processes")
            .content(asJsonString(addedProcessDTO))
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        // When
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());

        // Expect
        verify(taskManager).addProcess(any());
    }

    @Test
    @DisplayName("then it will return a 503 if the capacity is exceeded")
    @SneakyThrows
        // Async tests require a different testing format:
        // https://stackoverflow.com/questions/46908906/spring-boot-returning-wrong-status-code-only-in-unit-test
    void whenCapacityIsFull_thenDenyProcessCreation() {
        // Given
        AddedProcessDTO addedProcessDTO = new AddedProcessDTO();
        addedProcessDTO.setPriority(PriorityTypes.HIGH);

        doThrow(new MaximumCapacityReachedException("boom")).when(taskManager).addProcess(any());

        final MvcResult result = mockMvc.perform(post("/v1/processes")
            .content(asJsonString(addedProcessDTO))
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        // Expect
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isServiceUnavailable());

        verify(taskManager).addProcess(any());
    }

    @Test
    @DisplayName("Then it will kill all process if requested")
    @SneakyThrows
    void whenKillAllIsEnabled_ThenAllProcessAreKilled() {
        // Given
        doNothing().when(taskManager).killAll();

        final MvcResult result = mockMvc.perform(delete("/v1/processes").param("killAll", "true")
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        // Expect
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());

        verify(taskManager).killAll();
    }

    @Test
    @DisplayName("Then it will kill only the pids in the list")
    @SneakyThrows
    void whenPidsAreListed_ThenProcessesWithThatPidsWillBeKilled() {
        // Given
        final String pid1 = "1";
        final String pid2 = "2";
        doNothing().when(taskManager).killProcess(pid1);
        doNothing().when(taskManager).killProcess(pid2);

        final MvcResult result = mockMvc.perform(delete("/v1/processes").param("pids", pid1 + "," + pid2)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        // Expect
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isOk());

        verify(taskManager).killProcess(pid1);
        verify(taskManager).killProcess(pid2);
    }

    @Test
    @DisplayName("Then it will return a 404 if a pid is not found")
    @SneakyThrows
    void whenNotFoundPidIsListed_ThenReturnNotFoundStatus() {
        // Given
        final String pid1 = "1";
        final String pid2 = "2";
        doNothing().when(taskManager).killProcess(pid1);
        doThrow(new ProcessNotFoundException("not found")).when(taskManager).killProcess(pid2);

        final MvcResult result = mockMvc.perform(delete("/v1/processes").param("pids", pid1 + "," + pid2)
            .contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        // Expect
        mockMvc.perform(asyncDispatch(result))
            .andExpect(status().isNotFound());

        verify(taskManager).killProcess(pid1);
        verify(taskManager).killProcess(pid2);
    }

    private String asJsonString(final Object obj) {
        try {
            return serializer.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}