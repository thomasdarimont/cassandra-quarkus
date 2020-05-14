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
package com.datastax.oss.quarkus.runtime.reactive;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.dse.driver.api.core.graph.GraphStatement;
import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphNode;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PrepareRequest;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class QuarkusReactiveCqlSession {
  private CqlSession cqlSession;

  public QuarkusReactiveCqlSession(CqlSession cqlSession) {
    this.cqlSession = cqlSession;
  }

  @NonNull
  Multi<ReactiveRow> executeContinuouslyReactive(@NonNull String query) {
    return Wrappers.toMulti(cqlSession.executeContinuouslyReactive(query));
  }

  @NonNull
  Multi<ReactiveRow> executeContinuouslyReactive(@NonNull Statement<?> statement) {
    return Wrappers.toMulti(cqlSession.executeContinuouslyReactive(statement));
  }

  @NonNull
  Multi<ReactiveGraphNode> executeReactive(@NonNull GraphStatement<?> statement) {
    return Wrappers.toMulti(cqlSession.executeReactive(statement));
  }

  @NonNull
  Multi<ReactiveRow> executeReactive(@NonNull String query) {
    return Wrappers.toMulti(cqlSession.executeReactive(query));
  }

  @NonNull
  Multi<ReactiveRow> executeReactive(@NonNull Statement<?> statement) {
    return Wrappers.toMulti(cqlSession.executeReactive(statement));
  }

  @NonNull
  Uni<AsyncResultSet> executeAsync(@NonNull String query) {
    return Wrappers.toUni(cqlSession.executeAsync(query));
  }

  @NonNull
  Uni<AsyncResultSet> executeAsync(@NonNull Statement<?> statement) {
    return Wrappers.toUni(cqlSession.executeAsync(statement));
  }

  @NonNull
  Uni<PreparedStatement> prepareAsync(@NonNull String query) {
    return Wrappers.toUni(cqlSession.prepareAsync(query));
  }

  @NonNull
  Uni<PreparedStatement> prepareAsync(@NonNull SimpleStatement statement) {
    return Wrappers.toUni(cqlSession.prepareAsync(statement));
  }

  @NonNull
  Uni<PreparedStatement> prepareAsync(PrepareRequest request) {
    return Wrappers.toUni(cqlSession.prepareAsync(request));
  }
}
