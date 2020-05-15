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

import io.smallrye.mutiny.Multi;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.reactivestreams.Publisher;

public class Wrappers {
  public static <T> Multi<T> toMulti(Publisher<T> publisher) {
    Context context = Vertx.currentContext();
    if (context != null) {
      return Multi.createFrom()
          .publisher(publisher)
          .emitOn(command -> context.runOnContext(x -> command.run()));
    } else {
      return Multi.createFrom().publisher(publisher);
    }
  }
}
