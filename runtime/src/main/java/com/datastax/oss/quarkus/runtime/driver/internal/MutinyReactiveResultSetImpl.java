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

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.quarkus.runtime.driver.api.MutinyContinuousReactiveResultSet;
import com.datastax.oss.quarkus.runtime.driver.api.MutinyReactiveResultSet;
import com.datastax.oss.quarkus.runtime.reactive.Wrappers;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.MultiBroadcast;
import io.smallrye.mutiny.groups.MultiCollect;
import io.smallrye.mutiny.groups.MultiConvert;
import io.smallrye.mutiny.groups.MultiGroup;
import io.smallrye.mutiny.groups.MultiOnCompletion;
import io.smallrye.mutiny.groups.MultiOnEvent;
import io.smallrye.mutiny.groups.MultiOnFailure;
import io.smallrye.mutiny.groups.MultiOnItem;
import io.smallrye.mutiny.groups.MultiOverflow;
import io.smallrye.mutiny.groups.MultiSubscribe;
import io.smallrye.mutiny.groups.MultiTransform;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Predicate;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

public class MutinyReactiveResultSetImpl
    implements MutinyReactiveResultSet, MutinyContinuousReactiveResultSet {

  private final ReactiveResultSet reactiveResultSet;
  private final Multi<ReactiveRow> multi;

  public MutinyReactiveResultSetImpl(ReactiveResultSet reactiveResultSet) {

    this.reactiveResultSet = reactiveResultSet;
    this.multi = Wrappers.toMulti(reactiveResultSet);
  }

  @NonNull
  @Override
  public Publisher<? extends ColumnDefinitions> getColumnDefinitions() {
    return reactiveResultSet.getColumnDefinitions();
  }

  @NonNull
  @Override
  public Publisher<? extends ExecutionInfo> getExecutionInfos() {
    return reactiveResultSet.getExecutionInfos();
  }

  @NonNull
  @Override
  public Publisher<Boolean> wasApplied() {
    return reactiveResultSet.wasApplied();
  }

  @Override
  public MultiSubscribe<ReactiveRow> subscribe() {
    return multi.subscribe();
  }

  @Override
  public MultiOnItem<ReactiveRow> onItem() {
    return multi.onItem();
  }

  @Override
  public <O> O then(Function<Multi<ReactiveRow>, O> stage) {
    return multi.then(stage);
  }

  @Override
  public Uni<ReactiveRow> toUni() {
    return multi.toUni();
  }

  @Override
  public MultiOnFailure<ReactiveRow> onFailure() {
    return multi.onFailure();
  }

  @Override
  public MultiOnFailure<ReactiveRow> onFailure(Predicate<? super Throwable> predicate) {
    return multi.onFailure(predicate);
  }

  @Override
  public MultiOnFailure<ReactiveRow> onFailure(Class<? extends Throwable> aClass) {
    return multi.onFailure(aClass);
  }

  @Override
  public MultiOnEvent<ReactiveRow> on() {
    return multi.on();
  }

  @Override
  public Multi<ReactiveRow> cache() {
    return multi.cache();
  }

  @Override
  public MultiCollect<ReactiveRow> collectItems() {
    return multi.collectItems();
  }

  @Override
  public MultiGroup<ReactiveRow> groupItems() {
    return multi.groupItems();
  }

  @Override
  public Multi<ReactiveRow> emitOn(Executor executor) {
    return multi.emitOn(executor);
  }

  @Override
  public Multi<ReactiveRow> subscribeOn(Executor executor) {
    return multi.subscribeOn(executor);
  }

  @Override
  public MultiOnCompletion<ReactiveRow> onCompletion() {
    return multi.onCompletion();
  }

  @Override
  public MultiTransform<ReactiveRow> transform() {
    return multi.transform();
  }

  @Override
  public MultiOverflow<ReactiveRow> onOverflow() {
    return multi.onOverflow();
  }

  @Override
  public MultiBroadcast<ReactiveRow> broadcast() {
    return multi.broadcast();
  }

  @Override
  public MultiConvert<ReactiveRow> convert() {
    return multi.convert();
  }

  @Override
  public void subscribe(Subscriber<? super ReactiveRow> subscriber) {
    multi.subscribe(subscriber);
  }
}
