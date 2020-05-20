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

import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.internal.mapper.processor.CodeGenerator;
import com.datastax.oss.driver.internal.mapper.processor.DefaultCodeGeneratorFactory;
import com.datastax.oss.driver.internal.mapper.processor.MethodGenerator;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoImplementationSharedCode;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoReturnTypeParser;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.dao.QuarkusDaoImplementationGenerator;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.dao.QuarkusDaoQueryMethodGenerator;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.dao.QuarkusDaoReturnTypeParser;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.dao.QuarkusDaoSelectMethodGenerator;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.mapper.QuarkusMapperBuilderGenerator;
import java.util.Map;
import java.util.Optional;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public class QuarkusCodeGeneratorFactory extends DefaultCodeGeneratorFactory {

  private final QuarkusDaoReturnTypeParser daoReturnTypeParser;

  public QuarkusCodeGeneratorFactory(QuarkusProcessorContext context) {
    super(context);
    this.daoReturnTypeParser = new QuarkusDaoReturnTypeParser(context);
  }

  @Override
  public CodeGenerator newMapperBuilder(TypeElement interfaceElement) {
    return new QuarkusMapperBuilderGenerator(interfaceElement, context);
  }

  @Override
  public DaoReturnTypeParser getDaoReturnTypeParser() {
    return daoReturnTypeParser;
  }

  @Override
  public CodeGenerator newDaoImplementation(TypeElement interfaceElement) {
    return new QuarkusDaoImplementationGenerator(interfaceElement, context);
  }

  @Override
  public Optional<MethodGenerator> newDaoImplementationMethod(
      ExecutableElement methodElement,
      Map<Name, TypeElement> typeParameters,
      TypeElement processedType,
      DaoImplementationSharedCode enclosingClass) {
    if (methodElement.getAnnotation(Select.class) != null) {
      return Optional.of(
          new QuarkusDaoSelectMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    }

    if (methodElement.getAnnotation(Query.class) != null) {
      return Optional.of(
          new QuarkusDaoQueryMethodGenerator(
              methodElement, typeParameters, processedType, enclosingClass, context));
    }
    return super.newDaoImplementationMethod(
        methodElement, typeParameters, processedType, enclosingClass);
  }
}
