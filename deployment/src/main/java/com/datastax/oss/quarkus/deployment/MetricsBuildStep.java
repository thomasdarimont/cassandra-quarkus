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

import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.BYTES_RECEIVED;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.BYTES_SENT;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CONNECTED_NODES;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CQL_CLIENT_TIMEOUTS;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CQL_PREPARED_CACHE_SIZE;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.CQL_REQUESTS;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.THROTTLING_DELAY;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.THROTTLING_ERRORS;
import static com.datastax.oss.driver.api.core.metrics.DefaultSessionMetric.THROTTLING_QUEUE_SIZE;

import com.datastax.oss.driver.api.core.metrics.SessionMetric;
import com.datastax.oss.quarkus.runtime.metrics.CassandraCounter;
import com.datastax.oss.quarkus.runtime.metrics.CassandraGauge;
import com.datastax.oss.quarkus.runtime.metrics.CassandraMetered;
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
        BYTES_SENT,
        Metadata.builder()
            .withName(BYTES_SENT.getPath())
            .withDescription("The number and rate of bytes sent for the entire session.")
            .withType(MetricType.METERED)
            .build());

    supportedSessionMetrics.put(
        BYTES_RECEIVED,
        Metadata.builder()
            .withName(BYTES_RECEIVED.getPath())
            .withDescription("The number and rate of bytes received for the entire session.")
            .withType(MetricType.METERED)
            .build());

    supportedSessionMetrics.put(
        CONNECTED_NODES,
        Metadata.builder()
            .withName(CONNECTED_NODES.getPath())
            .withDescription(
                "The number of nodes to which the driver has at least one active connection")
            .withType(MetricType.GAUGE)
            .build());

    supportedSessionMetrics.put(
        CQL_REQUESTS,
        Metadata.builder()
            .withName(CQL_REQUESTS.getPath())
            .withDescription("The throughput and latency percentiles of CQL requests.")
            .withType(MetricType.METERED)
            .build());

    supportedSessionMetrics.put(
        THROTTLING_DELAY,
        Metadata.builder()
            .withName(THROTTLING_DELAY.getPath())
            .withDescription("How long requests are being throttled.")
            .withType(MetricType.METERED)
            .build());

    supportedSessionMetrics.put(
        THROTTLING_QUEUE_SIZE,
        Metadata.builder()
            .withName(THROTTLING_QUEUE_SIZE.getPath())
            .withDescription("The size of the throttling queue.")
            .withType(MetricType.GAUGE)
            .build());

    supportedSessionMetrics.put(
        CQL_CLIENT_TIMEOUTS,
        Metadata.builder()
            .withName(CQL_CLIENT_TIMEOUTS.getPath())
            .withDescription("The number of CQL requests that timed out.")
            .withType(MetricType.COUNTER)
            .build());

    supportedSessionMetrics.put(
        THROTTLING_ERRORS,
        Metadata.builder()
            .withName(THROTTLING_ERRORS.getPath())
            .withDescription(
                "The number of times a request was rejected with a RequestThrottlingException.")
            .withType(MetricType.COUNTER)
            .build());

    supportedSessionMetrics.put(
        CQL_PREPARED_CACHE_SIZE,
        Metadata.builder()
            .withName(CQL_PREPARED_CACHE_SIZE.getPath())
            .withDescription("The size of the driver-side cache of CQL prepared statements.")
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
    } else if (typeRaw.equals(MetricType.METERED)) {
      metrics.produce(
          new MetricBuildItem(metadata, new CassandraMetered(sessionMetric), true, CONFIG_ROOT));
    } else if (typeRaw.equals(MetricType.COUNTER)) {
      metrics.produce(
          new MetricBuildItem(metadata, new CassandraCounter(sessionMetric), true, CONFIG_ROOT));
    }
  }
}
