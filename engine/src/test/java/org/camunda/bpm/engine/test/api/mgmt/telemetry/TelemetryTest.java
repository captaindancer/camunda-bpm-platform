/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.mgmt.telemetry;


import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.commons.testing.ProcessEngineLoggingRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import static org.assertj.core.api.Assertions.assertThat;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

public class TelemetryTest {

  protected static final String TELEMETRY_ENDPOINT = "http://localhost:8083/pings";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public ProcessEngineLoggingRule loggingRule = new ProcessEngineLoggingRule();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8083);

  protected ProcessEngineConfigurationImpl configuration;
  protected ManagementService managementService;

  @Before
  public void setup() {
    // initialize
    configuration = engineRule.getProcessEngineConfiguration();
    managementService = engineRule.getManagementService();
  }

  @Test
  public void shouldReportData() {
    // given
    managementService.enableTelemetry(true);

    // when
    TelemetryReporter telemetryReporter = configuration.getTelemetryReporter();
    assertThat(telemetryReporter).isNotNull();
    assertThat(telemetryReporter.getTelemetrySendingTask()).isNotNull();

    // then
  }
}
