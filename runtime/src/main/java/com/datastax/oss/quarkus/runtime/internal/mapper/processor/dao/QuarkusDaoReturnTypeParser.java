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
package com.datastax.oss.quarkus.runtime.internal.mapper.processor.dao;

import com.datastax.oss.driver.internal.mapper.processor.ProcessorContext;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnTypeKind;
import com.datastax.oss.driver.internal.mapper.processor.dao.DefaultDaoReturnTypeParser;
import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableMap;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyMappedReactiveResultSet;
import java.util.Map;

public class QuarkusDaoReturnTypeParser extends DefaultDaoReturnTypeParser {
  private static final Map<Class<?>, DaoReturnTypeKind> QUARKUS_ENTITY_CONTAINER_MATCHES =
      ImmutableMap.<Class<?>, DaoReturnTypeKind>builder()
          .putAll(DEFAULT_ENTITY_CONTAINER_MATCHES)
          .put(
              MutinyMappedReactiveResultSet.class,
              QuarkusDaoReturnTypeKind.MUTINY_MAPPED_REACTIVE_RESULT_SET)
          .build();

  public QuarkusDaoReturnTypeParser(ProcessorContext context) {
    super(
        context,
        DEFAULT_TYPE_KIND_MATCHES,
        DEFAULT_CLASS_MATCHES,
        QUARKUS_ENTITY_CONTAINER_MATCHES,
        DEFAULT_FUTURE_OF_CLASS_MATCHES,
        DEFAULT_FUTURE_OF_ENTITY_CONTAINER_MATCHES);
  }
}
