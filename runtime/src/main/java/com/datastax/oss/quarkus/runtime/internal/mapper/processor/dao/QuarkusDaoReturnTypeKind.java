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

import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnTypeKind;
import com.datastax.oss.quarkus.runtime.internal.reactive.FailedMutinyMappedReactiveResultSet;
import com.squareup.javapoet.CodeBlock;

public enum QuarkusDaoReturnTypeKind implements DaoReturnTypeKind {
  MUTINY_MAPPED_REACTIVE_RESULT_SET {
    @Override
    public void addExecuteStatement(CodeBlock.Builder methodBuilder, String helperFieldName) {
      // Note that the executeReactive* methods in the generated code are defined in QuarkusDaoBase
      methodBuilder.addStatement(
          "return executeReactiveAndMap(boundStatement, $L)", helperFieldName);
    }

    @Override
    public CodeBlock wrapWithErrorHandling(CodeBlock innerBlock) {
      return CodeBlock.builder()
          .beginControlFlow("try")
          .add(innerBlock)
          .nextControlFlow("catch ($T t)", Throwable.class)
          .addStatement("return new $T(t)", FailedMutinyMappedReactiveResultSet.class)
          .endControlFlow()
          .build();
    }
  },
  ;

  @Override
  public String getDescription() {
    return name();
  }
}
