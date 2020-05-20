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

import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.QuarkusMapperProcessor;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.UUID;
import java.util.stream.Stream;
import javax.lang.model.element.Modifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class QuarkusDaoQueryMethodGeneratorTest extends DaoMethodGeneratorTest {
  @NonNull
  @Override
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @ParameterizedTest
  @MethodSource("invalidSignatures")
  public void should_fail_with_expected_error(String expectedError, MethodSpec method) {
    super.should_fail_with_expected_error(expectedError, method);
  }

  private static Stream<Arguments> invalidSignatures() {
    return Stream.<Arguments>builder()
        .add(
            Arguments.arguments(
                "Invalid return type: Query methods must return one of [VOID, BOOLEAN, LONG, ROW, ENTITY, OPTIONAL_ENTITY, RESULT_SET, BOUND_STATEMENT, PAGING_ITERABLE, FUTURE_OF_VOID, FUTURE_OF_BOOLEAN, FUTURE_OF_LONG, FUTURE_OF_ROW, FUTURE_OF_ENTITY, FUTURE_OF_OPTIONAL_ENTITY, FUTURE_OF_ASYNC_RESULT_SET, FUTURE_OF_ASYNC_PAGING_ITERABLE, REACTIVE_RESULT_SET, MAPPED_REACTIVE_RESULT_SET, MUTINY_MAPPED_REACTIVE_RESULT_SET]",
                MethodSpec.methodBuilder("select")
                    .addAnnotation(
                        AnnotationSpec.builder(Query.class)
                            .addMember("value", "$S", "SELECT * FROM whatever")
                            .build())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(UUID.class)
                    .build()))
        .build();
  }
}
