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

package org.app.helaHub.ipc.integ.sms;


import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.RequestBuilder;
import org.jasypt.encryption.StringEncryptor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.muhia.app.leto.config.properties.AsyncHttpClientProperties;
import org.muhia.app.leto.config.properties.sms.gateway.AfricasTalkingProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.asynchttpclient.Dsl.asyncHttpClient;

/**
 * Created by KennethKZMMuhia
 * Project: zeus
 * Package: org.muhia.app.zeus.integ.sms
 * Generated on: 13-Apr-17, 20:34
 */
@Component
public class AfricasTalkingGateway {
    private final AfricasTalkingProperties properties;
    private AsyncHttpClientConfig cf;
    private final StringEncryptor jasyptStringEncryptor;
//    private final SslContextFactory sslContextFactory;

    @Autowired
    public AfricasTalkingGateway(AfricasTalkingProperties properties, AsyncHttpClientProperties asyncHttpClientConfigProperties, StringEncryptor jasyptStringEncryptor) {
        this.properties = properties;
        this.jasyptStringEncryptor = jasyptStringEncryptor;
        io.netty.handler.ssl.SslContext sc = null;
        try {
            sc = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        } catch (SSLException e) {
            e.printStackTrace();
        }
        cf = new DefaultAsyncHttpClientConfig.Builder()
                .setCompressionEnforced(asyncHttpClientConfigProperties.isCompressionEnforced())
                .setMaxConnections(asyncHttpClientConfigProperties.getMaxTotalConnections())
                .setPooledConnectionIdleTimeout(asyncHttpClientConfigProperties.getMaxIdleTime())
                .setRequestTimeout(asyncHttpClientConfigProperties.getRequestTimeout())
                .setMaxConnectionsPerHost(asyncHttpClientConfigProperties.getMaxTotalConnectionsPerHost())
                .setRequestTimeout(asyncHttpClientConfigProperties.getRequestTimeout())
                .setSslContext(sc)
                .build();

    }

    private JSONArray sendPostSmsRequest(HashMap<String, String> dataMap, String urlString, String httpMethod) throws IOException {
        final JSONArray[] jsonArray = {new JSONArray()};

        try (AsyncHttpClient client = asyncHttpClient(cf)) {
            RequestBuilder builder = new RequestBuilder(httpMethod);
            dataMap.forEach((p, v) -> {
                try {
                    //builder.addFormParam(p, URLEncoder.encode(v, properties.getIntegUrlEncodeEncoding()));
                    //removed the encoding as it was sending sms with +
                    builder.addFormParam(p, v);
                } catch (Exception e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
                }
            });

            builder
                    .setUrl(urlString)
                    .addHeader(properties.getUrlHeaderAcceptKeyword(), properties.getUrlHeaderAcceptData())
                    .addHeader(properties.getUrlHeaderApiKeyword(), jasyptStringEncryptor.decrypt(properties.getUrlHeaderApiData()))
                    .build();

            client
                    .executeRequest(builder)
                    .toCompletableFuture()
                    .thenApply(response -> {
                        Logger.getLogger(this.getClass().getName()).log(Level.FINE, response.getResponseBody());
                        JSONObject jsObject = new JSONObject();
                        try {
                            if (response.getStatusCode() != properties.getResponseCodeHttpCreated()) {
                                Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.format("Received [HttpStatus = %d, Text = %s, URL = %s] processing halted!", response.getStatusCode(), response.getStatusText(), urlString));
                            } else {
                                jsObject = new JSONObject(response.getResponseBody());
                                Logger.getLogger(this.getClass().getName()).log(Level.INFO, String.format("Received [HttpStatus = %d, Text = %s, URL = %s] processing halted!", response.getStatusCode(), response.getStatusText(), jsObject.toString()));
                            }

                        } catch (Exception e) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("JSON Processing failed, [Message= %s, URL= %s]", e.getMessage(), urlString));

                        }
                        return jsObject;

                    })
                    .thenAccept(object -> {
                        try {
                            if (object.length() > 0) {

                                JSONArray recipients = object.getJSONObject(properties.getSmsMessageDataKeyword()).getJSONArray(properties.getIntegResponseRecipientsKeyword());
                                if (recipients.length() > 0) {
                                    jsonArray[0] = recipients;
                                } else {
                                    throw new Exception(object.getJSONObject(properties.getSmsMessageDataKeyword()).getString(properties.getIntegResponseMessageKeyword()));
                                }
                            }

                        } catch (Exception e) {

                            try {
                                jsonArray[0] = new JSONArray(e.getMessage());
                            } catch (JSONException ex) {
                                try {
                                    jsonArray[0] = new JSONArray(String.format("[%s]", ex.getMessage()));
                                } catch (JSONException e1) {
                                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e1.getMessage(), e1);
                                }

                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
                            }
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("SMS Sending failed failed, [Message= %s, URL= %s]", e.getMessage(), urlString));
                        }
                    })
                    .join();

        }

        return jsonArray[0];
    }

    public JSONArray sendMessage(String to_, String message_, String from_) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        data.put(properties.getIntegUsernameKeyword(), jasyptStringEncryptor.decrypt(properties.getIntegUsernameData()));
        data.put(properties.getIntegToKeyword(), to_);
        data.put(properties.getIntegSmsMessageKeyword(), message_);

        if (from_ == null) data.put(properties.getIntegFromKeyword(), "");
        else if (from_.length() > 0) data.put(properties.getIntegFromKeyword(), from_);

        return sendPostSmsRequest(data, properties.getProdApiHost() + properties.getSmsUrl(), properties.getIntegHttpMethodPost());
    }
}
