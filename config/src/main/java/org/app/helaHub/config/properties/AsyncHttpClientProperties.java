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

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@EncryptablePropertySource(value = {"file:${CONFIG_PATH}/async-http-client.properties"})
@Getter
public class AsyncHttpClientProperties {
    @Value("${org.muhia.app.properties.ahc.max.total.connections}")
    private Integer maxTotalConnections;
    @Value("${org.muhia.app.properties.ahc.max.total.connections.per.host}")
    private Integer maxTotalConnectionsPerHost;
    @Value("${org.muhia.app.properties.ahc.max.idle.time}")
    private Integer maxIdleTime;
    @Value("${org.muhia.app.properties.ahc.request.timeout}")
    private Integer requestTimeout;
    @Value("${org.muhia.app.properties.ahc.compression.enforced}")
    private boolean compressionEnforced;
    @Value("${org.muhia.app.properties.ahc.pooledconnectionidletimeout}")
    private Integer pooledconnectionidletimeout;


}
