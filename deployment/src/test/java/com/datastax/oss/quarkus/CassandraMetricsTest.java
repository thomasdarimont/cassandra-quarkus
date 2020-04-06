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

import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CONNECTED_NODES;
import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.oss.driver.api.core.CqlSession;
import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;
import javax.inject.Inject;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@QuarkusTestResource(CassandraTestBase.class)
public class CassandraMetricsTest {

  @Inject CqlSession cqlSession;

  @Inject
  @RegistryType(type = MetricRegistry.Type.VENDOR)
  MetricRegistry registry;

  @RegisterExtension
  static final QuarkusUnitTest config =
      new QuarkusUnitTest()
          .setArchiveProducer(
              () -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestBase.class))
          .withConfigurationResource("application-metrics.properties");

  @Test
  void testMetricsInitialization() {
    assertThat(getGaugeValue(CONNECTED_NODES.getPath())).isEqualTo(1L);
  }

  @SuppressWarnings("unchecked")
  private Long getGaugeValue(String metricName) {
    MetricID metricID = new MetricID(metricName);
    Metric metric = registry.getMetrics().get(metricID);

    if (metric == null) {
      return null;
    }
    return ((Gauge<Long>) metric).getValue();
  }
}
