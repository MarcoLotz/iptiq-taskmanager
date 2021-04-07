package com.marcolotz.iptiq.core;

import com.marcolotz.iptiq.core.exceptions.MaximumCapacityReachedException;
import com.marcolotz.iptiq.core.exceptions.ProcessNotFoundException;
import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;
import com.marcolotz.iptiq.core.model.Priority;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TaskManager;
import com.marcolotz.iptiq.utils.SequentialTimeProvider;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.marcolotz.iptiq.core.model.Priority.LOW;
import static com.marcolotz.iptiq.core.model.Priority.MEDIUM;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@DisplayName("When performing operations on a simple task manager")
class SimpleTaskManagerTest {

    protected static final int EXPECTED_SIZE = 2;
    protected TaskManager taskManager;

    @BeforeEach
    void setUp() {
        taskManager = new SimpleTaskManager(EXPECTED_SIZE, new SequentialTimeProvider());
    }

    @DisplayName("then capacities below 1 are not accepted")
    @Test
    // TODO: This is actually from process container
    void whenCapacityIsBelow1_thenExceptionIsThrown() {
        // Given
        final int capacity = 0;

        // Expect
        assertThrows(IllegalArgumentException.class, () -> new SimpleTaskManager(capacity, new SequentialTimeProvider()));
    }

    @DisplayName("then processes can be added until capacity is reached")
    @Test
    void whenProcessesAreAdded_thenTaskManagerHandlesUntilCapacity() {
        // Given
        populateWithProcess(EXPECTED_SIZE, LOW);

        // Expect
        assertThrows(MaximumCapacityReachedException.class, () -> taskManager.addProcess(new Process(LOW)));
    }

    @SneakyThrows
    protected void populateWithProcess(final int numberOfProcesses, final Priority priority) {
        for (int i = 0; i < numberOfProcesses; i++) {
            var p = new Process(priority);
            taskManager.addProcess(p);
        }
    }

    @DisplayName("then a process can be killed")
    @Test
    @SneakyThrows
    void whenKillingProcess_thenItIsRemovedFromTaskManager() {
        // Given
        populateWithProcess(1, LOW);
        final Process killedProcess = spy(new Process(LOW));
        taskManager.addProcess(killedProcess);

        // When
        taskManager.killProcess(killedProcess.getPid().toString());

        // Expect
        verify(killedProcess).kill();
    }

    @DisplayName("then when processes are not found exception is thrown")
    @Test
    void whenKillingProcess_thenExceptionIsThrownWhenNoProcessIsFound() {
        // Given
        populateWithProcess(2, LOW);
        final Process killedProcess = new Process(LOW);

        // Expect
        assertThrows(ProcessNotFoundException.class, () -> taskManager.killProcess(killedProcess.getPid().toString()));
    }

    @DisplayName("then only processes in the expected group are killed")
    @Test
    @SneakyThrows
    void whenKillingProcessInAGroup_thenKillOnlyThatGroup() {
        // Given
        var lowProcess = new Process(LOW);
        taskManager.addProcess(lowProcess);
        var mediumProcess = new Process(MEDIUM);
        taskManager.addProcess(mediumProcess);

        // When
        taskManager.killGroup(LOW);

        // Expect
        List<Process> runningProcesses = taskManager.listRunningProcess(SortingMethod.ID);
        assertThat(runningProcesses).hasSize(1);
        assertEquals(mediumProcess, runningProcesses.get(0));
    }

    @DisplayName("Then kill all kills all processes")
    void whenKillingAllProcess_thenAllProcessesAreKilled() {
        // Given
        populateWithProcess(2, LOW);

        // When
        taskManager.killAll();

        // Then
        assertThat(taskManager.listRunningProcess(SortingMethod.ID)).hasSize(0);
    }

    @DisplayName("then listing running process by id sorts them by id")
    @Test
    void whenListingById_ThenItReturnsAllProcessesSortedById() {
        // Given
        populateWithProcess(2, LOW);

        // When
        List<Process> runningProcesses = taskManager.listRunningProcess(SortingMethod.ID);

        // Then
        assertThat(runningProcesses).hasSize(2);
        assertThat(runningProcesses.stream().map(p -> new AcceptedProcessDecorator(p, Instant.now())))
            .isSortedAccordingTo(SortingMethod.ID.comparator());
    }

    @DisplayName("then listing running process by id sorts them by priority")
    @Test
    @SneakyThrows
    void whenListingByPriority_ThenItReturnsAllProcessesSortedByPriority() {
        // Given
        var lowProcess = new Process(LOW);
        taskManager.addProcess(lowProcess);
        var mediumProcess = new Process(MEDIUM);
        taskManager.addProcess(mediumProcess);

        // When
        List<Process> runningProcesses = taskManager.listRunningProcess(SortingMethod.PRIORITY);

        // Then
        assertThat(runningProcesses).hasSize(2);
        assertThat(runningProcesses).containsExactly(mediumProcess, lowProcess);
    }

    @DisplayName("then listing running process by id sorts them by creatingTime")
    @Test
    @SneakyThrows
    void whenListingByCreationTime_ThenItReturnsAllProcessesSortedByCreationTime() {
        // Given
        var lowProcess = new Process(LOW);
        taskManager.addProcess(lowProcess);
        var mediumProcess = new Process(MEDIUM);
        taskManager.addProcess(mediumProcess);

        // When
        List<Process> runningProcesses = taskManager.listRunningProcess(SortingMethod.CREATION_TIME);

        // Then
        assertThat(runningProcesses).hasSize(2);
        assertThat(runningProcesses).containsExactly(lowProcess, mediumProcess);
    }
}