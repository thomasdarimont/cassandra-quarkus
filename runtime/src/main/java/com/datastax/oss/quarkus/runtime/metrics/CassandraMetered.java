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

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.datastax.oss.driver.api.core.metrics.SessionMetric;
import org.eclipse.microprofile.metrics.Metered;

public class CassandraMetered implements Metered {
  private SessionMetric sessionMetric;

  public CassandraMetered() {}

  public CassandraMetered(SessionMetric sessionMetric) {

    this.sessionMetric = sessionMetric;
  }

  @Override
  public long getCount() {
    return getMeter().getCount();
  }

  @Override
  public double getFifteenMinuteRate() {
    return getMeter().getFifteenMinuteRate();
  }

  @Override
  public double getFiveMinuteRate() {
    return getMeter().getFiveMinuteRate();
  }

  @Override
  public double getMeanRate() {
    return getMeter().getMeanRate();
  }

  @Override
  public double getOneMinuteRate() {
    return getMeter().getOneMinuteRate();
  }

  private Meter getMeter() {
    Metric metrics = MetricsFinder.getMetrics(sessionMetric);
    if (!(metrics instanceof Meter)) {
      throw new IllegalArgumentException(
          String.format(
              "The metric for metric name: %s should be of %s type, but is: %s.",
              sessionMetric, Meter.class.getName(), metrics.getClass().getName()));
    }
    return (Meter) metrics;
  }

  public SessionMetric getSessionMetric() {
    return sessionMetric;
  }

  public void setSessionMetric(SessionMetric sessionMetric) {
    this.sessionMetric = sessionMetric;
  }
}
