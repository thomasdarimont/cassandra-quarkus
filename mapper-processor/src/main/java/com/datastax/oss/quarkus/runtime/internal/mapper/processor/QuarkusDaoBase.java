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
package com.datastax.oss.quarkus.runtime.internal.mapper.processor;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.internal.mapper.reactive.DefaultMappedReactiveResultSet;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.mapper.MapperContext;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.entity.EntityHelper;
import com.datastax.oss.driver.internal.mapper.DaoBase;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.api.session.QuarkusCqlSession;
import com.datastax.oss.quarkus.runtime.internal.reactive.DefaultMutinyMappedReactiveResultSet;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Base class for generated implementations of {@link Dao}-annotated interfaces in the DSE driver.
 */
public class QuarkusDaoBase extends DaoBase {

  protected QuarkusDaoBase(MapperContext context) {
    super(context);
  }

  @NonNull
  protected <EntityT> MutinyMappedReactiveResultSet<EntityT> executeReactiveAndMap(
      Statement<?> statement, EntityHelper<EntityT> entityHelper) {
    ReactiveResultSet source = executeReactive(statement);
    return new DefaultMutinyMappedReactiveResultSet<>(
        new DefaultMappedReactiveResultSet<>(source, entityHelper::get));
  }

  @NonNull
  protected QuarkusCqlSession getSession() {
    return (QuarkusCqlSession) context.getSession();
  }
}
