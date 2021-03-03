package com.marcolotz.iptiq.core;

import static com.marcolotz.iptiq.core.model.Priority.LOW;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.utils.SequentialTimeProvider;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("When performing operations on a fifo task manager")
class FifoTaskManagerTest extends SimpleTaskManagerTest {

  @BeforeEach
  void setUp() {
    taskManager = new FifoTaskManager(EXPECTED_SIZE, new SequentialTimeProvider());
  }

  @DisplayName("then new processes replace old ones when capacity is reached")
  @Test
  @SneakyThrows
  void whenProcessesAreAdded_thenTaskManagerHandlesUntilCapacity() {
    // Given
    var oldest = new Process(LOW);
    var medium = new Process(LOW);
    var newest = new Process(LOW);

    // When
    taskManager.addProcess(oldest);
    taskManager.addProcess(medium);
    taskManager.addProcess(newest);

    // Expect
    List<Process> processList = taskManager.listRunningProcess(SortingMethod.CREATION_TIME);
    assertThat(processList).hasSize(2);
    assertThat(processList).doesNotContain(oldest);
    assertThat(processList).contains(medium, newest);
  }
}