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

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.internal.mapper.processor.MapperProcessor;
import com.datastax.oss.driver.internal.mapper.processor.dao.DaoMethodGeneratorTest;
import com.datastax.oss.quarkus.runtime.api.reactive.MutinyMappedReactiveResultSet;
import com.datastax.oss.quarkus.runtime.internal.mapper.processor.QuarkusMapperProcessor;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.CompilationSubject;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;
import java.util.UUID;
import javax.lang.model.element.Modifier;
import javax.tools.StandardLocation;
import org.junit.Test;

public class QuarkusDaoImplementationGeneratorTest extends DaoMethodGeneratorTest {

  private static final ClassName MUTINY_MAPPED_REACTIVE_RESULT_CLASS_NAME =
      ClassName.get(MutinyMappedReactiveResultSet.class);

  private static final ParameterizedTypeName ENTITY_MAPPED_REACTIVE_RESULT_SET =
      ParameterizedTypeName.get(MUTINY_MAPPED_REACTIVE_RESULT_CLASS_NAME, ENTITY_CLASS_NAME);

  @NonNull
  @Override
  protected MapperProcessor getMapperProcessor() {
    return new QuarkusMapperProcessor();
  }

  @Test
  public void should_generate_findById_method_returning_MutinyMappedReactiveResultSet() {
    Compilation compilation =
        compileWithMapperProcessor(
            "test",
            Collections.emptyList(),
            ENTITY_SPEC,
            TypeSpec.interfaceBuilder(ClassName.get("test", "ProductDao"))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Dao.class)
                .addMethod(
                    MethodSpec.methodBuilder("findById")
                        .addAnnotation(Select.class)
                        .addParameter(ParameterSpec.builder(UUID.class, "pk").build())
                        .returns(ENTITY_MAPPED_REACTIVE_RESULT_SET)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .build());
    assertThat(compilation).succeededWithoutWarnings();
    assertGeneratedFileContains(
        compilation, "public MutinyMappedReactiveResultSet<Product> findById(UUID pk)");
    assertGeneratedFileContains(
        compilation, "return executeReactiveAndMap(boundStatement, productHelper);");
  }

  @Test
  public void should_generate_query_method_returning_MutinyMappedReactiveResultSet() {
    Compilation compilation =
        compileWithMapperProcessor(
            "test",
            Collections.emptyList(),
            ENTITY_SPEC,
            TypeSpec.interfaceBuilder(ClassName.get("test", "ProductDao"))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Dao.class)
                .addMethod(
                    MethodSpec.methodBuilder("queryReactiveMapped")
                        .addAnnotation(
                            AnnotationSpec.builder(Query.class)
                                .addMember("value", "$S", "SELECT * FROM whatever")
                                .build())
                        .returns(ENTITY_MAPPED_REACTIVE_RESULT_SET)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .build())
                .build());
    assertThat(compilation).succeededWithoutWarnings();
    assertGeneratedFileContains(
        compilation, "public MutinyMappedReactiveResultSet<Product> queryReactiveMapped()");
    assertGeneratedFileContains(
        compilation, "return executeReactiveAndMap(boundStatement, productHelper);");
  }

  protected void assertGeneratedFileContains(Compilation compilation, String string) {
    CompilationSubject.assertThat(compilation)
        .generatedFile(
            StandardLocation.SOURCE_OUTPUT, "test", "ProductDaoImpl__MapperGenerated.java")
        .contentsAsUtf8String()
        .contains(string);
  }
}
