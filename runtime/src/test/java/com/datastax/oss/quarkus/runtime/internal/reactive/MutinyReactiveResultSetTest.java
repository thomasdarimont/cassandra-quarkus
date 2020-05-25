/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.quarkus.runtime.internal.reactive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datastax.dse.driver.internal.core.cql.reactive.DefaultReactiveResultSet;
import com.datastax.dse.driver.internal.mapper.reactive.DefaultMappedReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import io.reactivex.Flowable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

class MutinyReactiveResultSetTest {

  @Test
  public void should_validate_reactive_result_set() {
    // given
    int numberOfElements = 20;
    List<Integer> items = new ArrayList<>();

    // when
    DefaultMutinyMappedReactiveResultSet<Integer> resultSet =
        new DefaultMutinyMappedReactiveResultSet<>(
            new DefaultMappedReactiveResultSet<>(
                new DefaultReactiveResultSet(() -> createResults(numberOfElements)),
                row -> row.getInt(0)));

    // then
    resultSet.subscribe().with(items::add);
    assertThat(items.size()).isEqualTo(numberOfElements);
  }

  private static CompletableFuture<AsyncResultSet> createResults(int elements) {
    CompletableFuture<AsyncResultSet> previous = null;
    if (elements > 0) {
      // create pages of 5 elements each to exercise pagination
      List<Integer> pages =
          Flowable.range(0, elements).buffer(5).map(List::size).toList().blockingGet();
      Collections.reverse(pages);
      for (Integer size : pages) {
        List<Row> rows =
            Flowable.range(0, size)
                .map(
                    i -> {
                      Row row = mock(Row.class);
                      when(row.getInt(0)).thenReturn(i);
                      return row;
                    })
                .toList()
                .blockingGet();
        CompletableFuture<AsyncResultSet> future = new CompletableFuture<>();
        future.complete(new MockAsyncResultSet(rows, previous));
        previous = future;
      }
    } else {
      previous = new CompletableFuture<>();
      previous.complete(new MockAsyncResultSet(0, null));
    }
    return previous;
  }
}
