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
package com.datastax.oss.quarkus;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.reactive.Wrappers;
import io.smallrye.mutiny.Uni;
import java.util.concurrent.CompletionStage;

@Dao
public interface FruitDaoReactive {

  @Update
  CompletionStage<Void> updateAsync(Fruit fruitDao);

  default Uni<Void> updateAsyncMutiny(Fruit fruitDao) {
    // TODO once JAVA-2792 will be done, make mapper support Uni in the updateAsync method and
    // remove this method
    return Wrappers.toUni(updateAsync(fruitDao));
  }

  @Select
  MutinyMappedReactiveResultSet<Fruit> findById(String id);
}
