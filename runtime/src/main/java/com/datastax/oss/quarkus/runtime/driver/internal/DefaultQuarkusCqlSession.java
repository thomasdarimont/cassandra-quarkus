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
package com.datastax.oss.quarkus.runtime.driver.internal;

import com.datastax.dse.driver.api.core.graph.GraphStatement;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.internal.core.session.SessionWrapper;
import com.datastax.oss.quarkus.runtime.driver.api.MutinyContinuousReactiveResultSet;
import com.datastax.oss.quarkus.runtime.driver.api.MutinyGraphReactiveResultSet;
import com.datastax.oss.quarkus.runtime.driver.api.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.driver.api.QuarkusCqlSession;
import edu.umd.cs.findbugs.annotations.NonNull;

public class DefaultQuarkusCqlSession extends SessionWrapper implements QuarkusCqlSession {
  private CqlSession delegate;

  public DefaultQuarkusCqlSession(@NonNull CqlSession delegate) {
    super(delegate);
    this.delegate = delegate;
  }

  @NonNull
  @Override
  public MutinyContinuousReactiveResultSet executeContinuouslyReactive(@NonNull String query) {
    return new MutinyReactiveResultSetImpl(delegate.executeContinuouslyReactive(query));
  }

  @NonNull
  public MutinyContinuousReactiveResultSet executeContinuouslyReactive(
      @NonNull Statement<?> statement) {
    return new MutinyReactiveResultSetImpl(delegate.executeContinuouslyReactive(statement));
  }

  @NonNull
  public MutinyGraphReactiveResultSet executeReactive(@NonNull GraphStatement<?> statement) {
    return new MutinyGraphReactiveResultSetImpl(delegate.executeReactive(statement));
  }

  @NonNull
  @Override
  public MutinyReactiveResultSet executeReactive(@NonNull String query) {
    return new MutinyReactiveResultSetImpl(delegate.executeReactive(query));
  }

  @NonNull
  @Override
  public MutinyReactiveResultSet executeReactive(@NonNull Statement<?> statement) {
    return new MutinyReactiveResultSetImpl(delegate.executeReactive(statement));
  }
}
