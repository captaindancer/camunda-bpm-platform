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
package org.camunda.bpm.engine.impl;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.camunda.bpm.engine.impl.telemetry.dto.Data;
import org.camunda.bpm.engine.impl.telemetry.dto.Database;
import org.camunda.bpm.engine.impl.telemetry.dto.Internals;
import org.camunda.bpm.engine.impl.telemetry.dto.Product;
import org.camunda.bpm.engine.impl.telemetry.reporter.TelemetryReporter;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;

public class InititalizeTelemetryReporterCmd implements Command<TelemetryReporter> {

  protected static final String TELEMETRY_PROPERTY_VALUE = "camunda.telemetry.enabled";
  protected static final String EDITION_ENTERPRISE = "enterprise";
  protected static final String EDITION_COMMUNITY = "community";
  protected static final String PRODUCT_NAME = "Camunda BPM";

  @Override
  public TelemetryReporter execute(CommandContext commandContext) {
    TelemetryReporter telemetryReporter = null;

    PropertyEntity telemetryEnabledProperty = commandContext.getPropertyManager().findPropertyById(TELEMETRY_PROPERTY_VALUE);

    // initialize reporter only if the telemetry is enabled
    if (telemetryEnabledProperty != null && Boolean.parseBoolean(telemetryEnabledProperty.getValue())) {

      ProcessEngineConfigurationImpl processEngineConfiguration = commandContext.getProcessEngineConfiguration();

      telemetryReporter = new TelemetryReporter(processEngineConfiguration.getCommandExecutorTxRequired(),
                                                processEngineConfiguration.getTelemetryEndpoint(),
                                                getTelemetryData(processEngineConfiguration),
                                                getTelemetryHttpClient(processEngineConfiguration));
    }

    return telemetryReporter;
  }

  protected PropertyEntity getTelemetryProperty(CommandContext commandContext) {
    return commandContext.getPropertyManager().findPropertyById(TELEMETRY_PROPERTY_VALUE);
  }

  protected HttpClient getTelemetryHttpClient(ProcessEngineConfigurationImpl processEngineConfiguration) {
    HttpClient telemetryHttpClient = processEngineConfiguration.getTelemetryHttpClient();
    if (telemetryHttpClient == null) {
      telemetryHttpClient = HttpClientBuilder.create().build();
      processEngineConfiguration.setTelemetryHttpClient(telemetryHttpClient);
    }
    return telemetryHttpClient;
  }

  protected Data getTelemetryData(ProcessEngineConfigurationImpl processEngineConfiguration) {
    Data telemetryData = processEngineConfiguration.getTelemetryData();
    if (telemetryData == null) {
      telemetryData = initTelemetryData(processEngineConfiguration);
      processEngineConfiguration.setTelemetryData(telemetryData);
    }
    return telemetryData;
  }

  protected Data initTelemetryData(ProcessEngineConfigurationImpl configuration) {
    Database database = new Database(configuration.getDatabaseVendor(), configuration.getDatabaseVersion());
    Internals internals = new Internals(database);

    String edition = EDITION_COMMUNITY;
    String version = InititalizeTelemetryReporterCmd.class.getPackage().getImplementationVersion();

    if (version != null && version.contains("-ee")) {
      version = version.split("-")[0]; // trim `-ee` suffix
      edition = EDITION_ENTERPRISE;
    }

    Product product = new Product(PRODUCT_NAME, version, edition, internals);
    return new Data(configuration.getInstallationId(), product);
  }

}
