/*
 * Copyright (c) 2018
 *       BSK (BlueSky Consultants Limited, Kenya)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.app.helaHub.config.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@Configuration
@PropertySource(value = {"file:${CONFIG_PATH}/organization.properties"})
public class OrganizationProperties {

    @Value("${jasypt.encryptor.bean}")
    private String bean;
    @Value("${jasypt.encryptor.password}")
    private String password;
    @Value("${jasypt.encryptor.algorithm}")
    private String algorithm;
    @Value("${jasypt.encryptor.keyObtentionIterations}")
    private String keyObtentionIterations;
    @Value("${jasypt.encryptor.poolSize}")
    private String poolSize;
    @Value("${jasypt.encryptor.providerName}")
    private String providerName;
    @Value("${org.serial}")
    private String organizationSerial;
    @Value("${org.validator}")
    private String organizationValidator;
    @Value("${org.muhia.app.zeus.hashing.config.keySize}")
    private Integer hashingConfigKeysize;
    @Value("${org.muhia.app.zeus.hashing.config.algorithm}")
    private String hashingConfigAlgorithm;

}
