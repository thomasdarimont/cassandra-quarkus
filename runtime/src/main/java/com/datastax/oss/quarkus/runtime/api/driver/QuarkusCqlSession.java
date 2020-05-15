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
package com.datastax.oss.quarkus.runtime.api.driver;

import com.datastax.dse.driver.api.core.cql.continuous.reactive.ContinuousReactiveSession;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveSession;
import com.datastax.dse.driver.api.core.graph.GraphStatement;
import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphSession;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Statement;
import edu.umd.cs.findbugs.annotations.NonNull;

public interface QuarkusCqlSession
    extends ReactiveSession, ContinuousReactiveSession, ReactiveGraphSession, CqlSession {
  @NonNull
  @Override
  MutinyReactiveResultSet executeReactive(@NonNull String query);

  @NonNull
  @Override
  MutinyReactiveResultSet executeReactive(@NonNull Statement<?> statement);

  @NonNull
  @Override
  MutinyGraphReactiveResultSet executeReactive(@NonNull GraphStatement<?> statement);

  @NonNull
  @Override
  MutinyContinuousReactiveResultSet executeContinuouslyReactive(@NonNull String query);

  @NonNull
  @Override
  MutinyContinuousReactiveResultSet executeContinuouslyReactive(@NonNull Statement<?> statement);
}
