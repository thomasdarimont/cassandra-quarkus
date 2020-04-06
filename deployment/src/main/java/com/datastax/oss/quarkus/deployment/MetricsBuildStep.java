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
package com.datastax.oss.quarkus.deployment;

import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CONNECTED_NODES;

import com.datastax.oss.driver.api.core.metrics.SessionMetric;
import com.datastax.oss.quarkus.runtime.metrics.CassandraGauge;
import io.quarkus.deployment.Capabilities;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.smallrye.metrics.deployment.spi.MetricBuildItem;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.map.HashedMap;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricType;

public class MetricsBuildStep {
  private static final String CONFIG_ROOT = "datastax.driver.session";

  @BuildStep
  void registerMetrics(
      CassandraClientBuildTimeConfig buildTimeConfig,
      BuildProducer<MetricBuildItem> metrics,
      Capabilities capabilities) {
    Map<SessionMetric, Metadata> supportedSessionMetrics = new HashedMap();

    supportedSessionMetrics.put(
        CONNECTED_NODES,
        Metadata.builder()
            .withName(CONNECTED_NODES.getPath())
            .withDescription(
                "The number of nodes to which the driver has at least one active connection")
            .withType(MetricType.GAUGE)
            .build());

    if (buildTimeConfig.metricsEnabled && capabilities.isCapabilityPresent(Capabilities.METRICS)) {
      produceMetrics(metrics, supportedSessionMetrics, buildTimeConfig.metricsSessionEnabled);
    }
  }

  private void produceMetrics(
      BuildProducer<MetricBuildItem> metrics,
      Map<SessionMetric, Metadata> supportedSessionMetrics,
      Optional<List<String>> enabledMetrics) {
    if (enabledMetrics.isPresent()) {
      enabledMetrics
          .get()
          .forEach(
              metricName ->
                  supportedSessionMetrics.entrySet().stream()
                      .filter((entry) -> entry.getValue().getName().equals(metricName))
                      .forEach(entry -> produceMetric(entry.getKey(), entry.getValue(), metrics)));
    }
  }

  private void produceMetric(
      SessionMetric sessionMetric, Metadata metadata, BuildProducer<MetricBuildItem> metrics) {
    MetricType typeRaw = metadata.getTypeRaw();
    if (typeRaw.equals(MetricType.GAUGE)) {
      metrics.produce(
          new MetricBuildItem(metadata, new CassandraGauge(sessionMetric), true, CONFIG_ROOT));
    }
  }
}
