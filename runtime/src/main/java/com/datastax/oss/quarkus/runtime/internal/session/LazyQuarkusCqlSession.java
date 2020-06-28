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
package com.datastax.oss.quarkus.runtime.internal.session;

import com.datastax.dse.driver.api.core.cql.continuous.ContinuousAsyncResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.graph.AsyncGraphResultSet;
import com.datastax.dse.driver.api.core.graph.GraphStatement;
import com.datastax.dse.driver.api.core.graph.reactive.ReactiveGraphResultSet;
import com.datastax.dse.driver.internal.core.cql.continuous.reactive.ContinuousCqlRequestReactiveProcessor;
import com.datastax.dse.driver.internal.core.cql.reactive.CqlRequestReactiveProcessor;
import com.datastax.dse.driver.internal.core.graph.reactive.ReactiveGraphRequestProcessor;
import com.datastax.oss.driver.api.core.AsyncAutoCloseable;
import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PrepareRequest;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metrics.Metrics;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.reflect.GenericType;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyContinuousReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyGraphReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyGraphReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.LazyMutinyGraphReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.LazyMutinyReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.smallrye.mutiny.Uni;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * A lazy {@link QuarkusCqlSession} that avoids blocking until the session is initialized.
 *
 * <p>Instead, this implementation will defer all asynchronous and reactive queries until the
 * session is ready.
 *
 * <p>Calling any synchronous method on this instance will however block until the session is ready.
 */
public class LazyQuarkusCqlSession implements QuarkusCqlSession {

  @NonNull private final CompletionStage<QuarkusCqlSession> sessionFuture;

  public LazyQuarkusCqlSession(@NonNull CompletionStage<QuarkusCqlSession> sessionFuture) {
    this.sessionFuture = sessionFuture;
  }

  @NonNull
  @Override
  public String getName() {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.getName();
  }

  @NonNull
  @Override
  public Metadata getMetadata() {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.getMetadata();
  }

  @Override
  public boolean isSchemaMetadataEnabled() {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.isSchemaMetadataEnabled();
  }

  @NonNull
  @Override
  public CompletionStage<Metadata> setSchemaMetadataEnabled(@Nullable Boolean newValue) {
    return sessionFuture.thenCompose(session -> session.setSchemaMetadataEnabled(newValue));
  }

  @NonNull
  @Override
  public CompletionStage<Metadata> refreshSchemaAsync() {
    return sessionFuture.thenCompose(Session::refreshSchemaAsync);
  }

  @NonNull
  @Override
  public CompletionStage<Boolean> checkSchemaAgreementAsync() {
    return sessionFuture.thenCompose(Session::checkSchemaAgreementAsync);
  }

  @NonNull
  @Override
  public DriverContext getContext() {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.getContext();
  }

  @NonNull
  @Override
  public Optional<CqlIdentifier> getKeyspace() {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.getKeyspace();
  }

  @NonNull
  @Override
  public Optional<Metrics> getMetrics() {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.getMetrics();
  }

  @Nullable
  @Override
  public <RequestT extends Request, ResultT> ResultT execute(
      @NonNull RequestT request, @NonNull GenericType<ResultT> resultType) {
    CqlSession session = CompletableFutures.getUninterruptibly(sessionFuture);
    return session.execute(request, resultType);
  }

  @NonNull
  @Override
  public CompletionStage<Void> closeFuture() {
    return sessionFuture.thenCompose(AsyncAutoCloseable::closeFuture);
  }

  @NonNull
  @Override
  public CompletionStage<Void> closeAsync() {
    return sessionFuture.thenCompose(AsyncAutoCloseable::closeAsync);
  }

  @NonNull
  @Override
  public CompletionStage<Void> forceCloseAsync() {
    return sessionFuture.thenCompose(AsyncAutoCloseable::forceCloseAsync);
  }

  @NonNull
  @Override
  public CompletionStage<AsyncResultSet> executeAsync(@NonNull Statement<?> statement) {
    return sessionFuture.thenCompose(session -> session.executeAsync(statement));
  }

  @NonNull
  @Override
  public CompletionStage<AsyncGraphResultSet> executeAsync(
      @NonNull GraphStatement<?> graphStatement) {
    return sessionFuture.thenCompose(session -> session.executeAsync(graphStatement));
  }

  @NonNull
  @Override
  public CompletionStage<ContinuousAsyncResultSet> executeContinuouslyAsync(
      @NonNull Statement<?> statement) {
    return sessionFuture.thenCompose(session -> session.executeContinuouslyAsync(statement));
  }

  @NonNull
  @Override
  public CompletionStage<PreparedStatement> prepareAsync(@NonNull String query) {
    return sessionFuture.thenCompose(session -> session.prepareAsync(query));
  }

  @NonNull
  @Override
  public CompletionStage<PreparedStatement> prepareAsync(@NonNull SimpleStatement statement) {
    return sessionFuture.thenCompose(session -> session.prepareAsync(statement));
  }

  @NonNull
  @Override
  public CompletionStage<PreparedStatement> prepareAsync(PrepareRequest request) {
    return sessionFuture.thenCompose(session -> session.prepareAsync(request));
  }

  @NonNull
  @Override
  public MutinyReactiveResultSet executeReactive(@NonNull Statement<?> statement) {
    Uni<ReactiveResultSet> objectMulti =
        Uni.createFrom()
            .completionStage(sessionFuture)
            .onItem()
            .produceUni(
                session ->
                    Uni.createFrom()
                        .item(
                            new DefaultMutinyReactiveResultSet(
                                Objects.requireNonNull(
                                    execute(
                                        statement,
                                        CqlRequestReactiveProcessor.REACTIVE_RESULT_SET)))));
    return new LazyMutinyReactiveResultSet(objectMulti);
  }

  @NonNull
  @Override
  public MutinyGraphReactiveResultSet executeReactive(@NonNull GraphStatement<?> statement) {
    Uni<ReactiveGraphResultSet> objectMulti =
        Uni.createFrom()
            .completionStage(sessionFuture)
            .onItem()
            .produceUni(
                session ->
                    Uni.createFrom()
                        .item(
                            new DefaultMutinyGraphReactiveResultSet(
                                Objects.requireNonNull(
                                    execute(
                                        statement,
                                        ReactiveGraphRequestProcessor
                                            .REACTIVE_GRAPH_RESULT_SET)))));
    return new LazyMutinyGraphReactiveResultSet(objectMulti);
  }

  @NonNull
  @Override
  public MutinyContinuousReactiveResultSet executeContinuouslyReactive(
      @NonNull Statement<?> statement) {
    Uni<ReactiveResultSet> objectMulti =
        Uni.createFrom()
            .completionStage(sessionFuture)
            .onItem()
            .produceUni(
                session ->
                    Uni.createFrom()
                        .item(
                            new DefaultMutinyReactiveResultSet(
                                Objects.requireNonNull(
                                    execute(
                                        statement,
                                        ContinuousCqlRequestReactiveProcessor
                                            .CONTINUOUS_REACTIVE_RESULT_SET)))));
    return new LazyMutinyReactiveResultSet(objectMulti);
  }
}
