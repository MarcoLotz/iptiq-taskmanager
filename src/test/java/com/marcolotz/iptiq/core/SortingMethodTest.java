package com.marcolotz.iptiq.core;

import static com.marcolotz.iptiq.core.model.Priority.HIGH;
import static com.marcolotz.iptiq.core.model.Priority.LOW;
import static com.marcolotz.iptiq.core.model.Priority.MEDIUM;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.marcolotz.iptiq.core.model.AcceptedProcessDecorator;
import com.marcolotz.iptiq.core.model.Process;
import com.marcolotz.iptiq.ports.TimeProvider;
import com.marcolotz.iptiq.utils.SequentialTimeProvider;
import java.util.List;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("When sorting collections")
class SortingMethodTest {

  final Process low = new Process(LOW);
  final Process medium = new Process(MEDIUM);
  final Process high = new Process(HIGH);

  final TimeProvider timeProvider = new SequentialTimeProvider();

  List<AcceptedProcessDecorator> inputList;

  @BeforeEach
  void setup() {
    inputList = Lists.newArrayList(new AcceptedProcessDecorator(low, timeProvider.getTime()),
        new AcceptedProcessDecorator(medium, timeProvider.getTime()),
        new AcceptedProcessDecorator(high, timeProvider.getTime()));
  }

  @DisplayName("then it should return a descending list of id number")
  @Test
  void whenSortingByIdThenReturnListWithDescendingIdNumber() {
    // Given
    inputList.sort(SortingMethod.ID.comparator());

    // Expect
    var first = inputList.get(0);
    var second = inputList.get(1);
    var third = inputList.get(2);

    assertThat(first.getPid()).isGreaterThan(second.getPid());
    assertThat(second.getPid()).isGreaterThan(third.getPid());
  }

  @DisplayName("then it should return a descending list of priorities")
  @Test
  void whenSortingByPriorityThenReturnListWithDescendingPriority() {
    // Given
    inputList.sort(SortingMethod.PRIORITY.comparator());

    // Expect
    var first = inputList.get(0);
    var second = inputList.get(1);
    var third = inputList.get(2);

    assertEquals(HIGH, first.getPriority());
    assertEquals(MEDIUM, second.getPriority());
    assertEquals(LOW, third.getPriority());
  }

  @DisplayName("then it should return a descending list of priorities")
  @Test
  void whenSortingByIngestionTimeThenReturnListWithDescendingIngestionTime() {
    // Given
    inputList = Lists.newArrayList(
        new AcceptedProcessDecorator(low, timeProvider.getTime()),
        new AcceptedProcessDecorator(medium, timeProvider.getTime()),
        new AcceptedProcessDecorator(high, timeProvider.getTime())
    );

    // When
    inputList.sort(SortingMethod.CREATION_TIME.comparator());

    // Expect
    assertThat(inputList.stream().map(AcceptedProcessDecorator::getProcess))
        .containsExactly(low, medium, high); // based on ingestion sequence
  }


}