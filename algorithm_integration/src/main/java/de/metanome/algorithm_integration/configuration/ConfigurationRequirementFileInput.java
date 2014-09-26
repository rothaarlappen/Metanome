/*
 * Copyright 2014 by the Metanome project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package de.metanome.algorithm_integration.configuration;

import com.google.common.annotations.GwtIncompatible;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;

/**
 * Concrete {@link ConfigurationRequirement} for file inputs.
 *
 * @author Jakob Zwiener
 * @see ConfigurationRequirement
 */
public class ConfigurationRequirementFileInput extends ConfigurationRequirement {

  private static final long serialVersionUID = 8842139128248338302L;

  private ConfigurationSettingFileInput[] settings;

  /**
   * Exists for GWT serialization.
   */
  public ConfigurationRequirementFileInput() {
  }

  /**
   * Constructs a {@link ConfigurationRequirementFileInput}, requesting 1 value.
   *
   * @param identifier the specification's identifier
   */
  public ConfigurationRequirementFileInput(String identifier) {
    super(identifier);
  }

  /**
   * Constructs a {@link ConfigurationRequirementFileInput}, potentially requesting several values.
   *
   * @param identifier       the specification's identifier
   * @param numberOfSettings the number of settings expected
   */
  public ConfigurationRequirementFileInput(String identifier,
                                           int numberOfSettings) {

    super(identifier, numberOfSettings);
  }

  @Override
  public ConfigurationSettingFileInput[] getSettings() {
    return this.settings;
  }

  /**
   * Sets the actual values on the specification if the number of settings is correct.
   *
   * @param settings the settings
   * @throws de.metanome.algorithm_integration.AlgorithmConfigurationException if the number of
   * values does not match the expected number of values
   */
  public void setSettings(ConfigurationSettingFileInput... settings)
      throws AlgorithmConfigurationException {
    if (getNumberOfSettings() != ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES &&
        settings.length != getNumberOfSettings()) {
      throw new AlgorithmConfigurationException("The number of settings does not match the expected number!");
    }
    this.settings = settings;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @GwtIncompatible("ConfigurationValues cannot be build on client side.")
  public ConfigurationValue build(ConfigurationFactory factory)
      throws AlgorithmConfigurationException {
    return factory.build(this);
  }
}
