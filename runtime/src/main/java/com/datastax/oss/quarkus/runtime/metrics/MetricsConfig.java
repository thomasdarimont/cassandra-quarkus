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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MetricsConfig {
  public final List<String> metricsNodeEnabled;
  public final List<String> metricsSessionEnabled;

  public MetricsConfig(
      Optional<List<String>> metricsNodeEnabled, Optional<List<String>> metricsSessionEnabled) {

    this(
        metricsNodeEnabled.orElse(Collections.emptyList()),
        metricsSessionEnabled.orElse(Collections.emptyList()));
  }

  public MetricsConfig() {
    this(Collections.emptyList(), Collections.emptyList());
  }

  private MetricsConfig(List<String> metricsNodeEnabled, List<String> metricsSessionEnabled) {
    this.metricsNodeEnabled = Collections.unmodifiableList(metricsNodeEnabled);
    this.metricsSessionEnabled = Collections.unmodifiableList(metricsSessionEnabled);
  }
}
