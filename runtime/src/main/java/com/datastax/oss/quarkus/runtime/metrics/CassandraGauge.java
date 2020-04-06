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
package com.datastax.oss.quarkus.runtime.metrics;

import com.codahale.metrics.Metric;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metrics.Metrics;
import com.datastax.oss.driver.api.core.metrics.SessionMetric;
import io.quarkus.arc.Arc;
import java.util.Optional;
import org.eclipse.microprofile.metrics.Gauge;

public class CassandraGauge implements Gauge<Long> {
  private SessionMetric sessionMetric;

  // no-args constructor needed for serialization
  public CassandraGauge() {}

  // node metrics not supported
  public CassandraGauge(SessionMetric sessionMetric) {
    this.sessionMetric = sessionMetric;
  }

  public SessionMetric getSessionMetric() {
    return sessionMetric;
  }

  public void setSessionMetric(SessionMetric sessionMetric) {
    this.sessionMetric = sessionMetric;
  }

  private Metric getMetrics() {
    CqlSession cqlSession = Arc.container().instance(CqlSession.class).get();
    Optional<Metrics> driverMetrics = cqlSession.getMetrics();
    if (!driverMetrics.isPresent()) {
      throw new IllegalArgumentException(
          "The Metrics returned from CqlSession must be present but is not.");
    }

    Optional<Metric> metric = driverMetrics.get().getSessionMetric(this.sessionMetric);

    if (!metric.isPresent()) {
      throw new IllegalArgumentException(
          String.format(
              "Session metric for name: %s is not present in the driver metrics, but should be.",
              metric));
    }
    return metric.get();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Long getValue() {
    Metric metrics = getMetrics();
    if (!(metrics instanceof com.codahale.metrics.Gauge)) {
      throw new IllegalArgumentException(
          String.format(
              "The metric for metric name: %s should be of %s type, but is: %s.",
              sessionMetric,
              com.codahale.metrics.Gauge.class.getName(),
              metrics.getClass().getName()));
    }

    return ((com.codahale.metrics.Gauge<Number>) metrics).getValue().longValue();
  }
}
