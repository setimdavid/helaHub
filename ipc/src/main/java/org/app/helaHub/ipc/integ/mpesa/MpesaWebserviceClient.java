package org.app.helaHub.ipc.integ.mpesa;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.java.Log;
import org.apache.commons.codec.binary.Base64;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.AsyncHttpClientConfig;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.RequestBuilder;
import org.json.JSONObject;
import org.muhia.app.leto.config.properties.AsyncHttpClientProperties;
import org.muhia.app.leto.config.properties.integ.MpesaProperties;
import org.muhia.app.leto.store.model.MpesaB2CRequest;
import org.muhia.app.leto.store.model.MpesaCheckoutRequest;
import org.muhia.app.leto.store.model.MpesaDetails;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.asynchttpclient.Dsl.asyncHttpClient;

@Service
@Log
public class MpesaWebserviceClient {
    private final AsyncHttpClientProperties httpClientProperties;
    private final MpesaProperties gatewayProperties;
    private AsyncHttpClientConfig cf;

    public MpesaWebserviceClient(AsyncHttpClientProperties httpClientProperties, MpesaProperties gatewayProperties) {
        this.httpClientProperties = httpClientProperties;
        this.gatewayProperties = gatewayProperties;
        io.netty.handler.ssl.SslContext sc = null;
        try {
            sc = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            this.cf = new DefaultAsyncHttpClientConfig.Builder()
                    .setCompressionEnforced(httpClientProperties.isCompressionEnforced())
                    .setMaxConnections(httpClientProperties.getMaxTotalConnections())
                    .setPooledConnectionIdleTimeout(httpClientProperties.getMaxIdleTime())
                    .setRequestTimeout(httpClientProperties.getRequestTimeout())
                    .setMaxConnectionsPerHost(httpClientProperties.getMaxTotalConnectionsPerHost())
                    .setRequestTimeout(httpClientProperties.getRequestTimeout())
                    .setSslContext(sc)
                    .build();
        } catch (SSLException e) {
            e.printStackTrace();
        }
    }

    private JSONObject mpesaIntegration(RequestBuilder builder){
        AtomicReference<JSONObject> responseObject = new AtomicReference<>(new JSONObject());

        try (AsyncHttpClient client = asyncHttpClient(cf)) {
            client
                .executeRequest(builder)
                .toCompletableFuture()
                .thenApply(resp -> {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(resp.getResponseBody());
                    } catch (Exception e) {

                    }
                    responseObject.set(jsonObject);
                    return resp.toString();
                }).thenAccept(u -> Logger.getLogger(this.getClass().getName()).log(Level.FINE, u)).join();

        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("Mpesa Sending Failed,[Type= %s,Message= %s]","", e.getMessage()));
        }
        return responseObject.get();
    }


    private JSONObject processToMpesa(MpesaDetails mpesaDetails, JSONObject jsonPayload, String transactionType){
        AtomicReference<JSONObject> responseObject = new AtomicReference<>(new JSONObject());
        Map<String, String> accessResults = getAccessToken(mpesaDetails);
        String accessToken = "";

        if (accessResults.get("STATUS") == "SUCCESS"){
            accessToken = accessResults.get("TOKEN");
        }else {
            responseObject.get().put("status", "99");
            responseObject.get().put("message", "Unable to Retrieve the Token");
            return responseObject.get();
        }

        String endPointUrl = transactionType == "B2C" ? gatewayProperties.getB2cApiUrl() : gatewayProperties.getCheckoutRequestEndPoint();

        try {
            RequestBuilder builder = new RequestBuilder("POST");
            builder
                .setBody(jsonPayload.toString())
                .addHeader("authorization", "Bearer " + accessToken)
                .addHeader("content-type", "application/json")
                .setUrl(endPointUrl)
                .build();

            return mpesaIntegration(builder);

        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("Mpesa Sending Failed,[Type= %s,Message= %s, URL= %s]",transactionType, e.getMessage(), endPointUrl));
        }
        return responseObject.get();
    }

    public JSONObject processCheckoutTransactions(MpesaDetails mpesaDetails, MpesaCheckoutRequest checkoutRequest) {
        JSONObject responseObject = new JSONObject();
        String transactionType = "CHECKOUT";

        try {
            JSONObject jsonPayload = new JSONObject();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            String timestamp = formatter.format(zonedDateTime);

            String str = mpesaDetails.getShortCode() + mpesaDetails.getPassKey() + timestamp;
            String password = Base64.encodeBase64String(str.getBytes());

            jsonPayload.put("BusinessShortCode", mpesaDetails.getShortCode());
            jsonPayload.put("Password", password);
            jsonPayload.put("Timestamp", timestamp);
            jsonPayload.put("TransactionType", "CustomerPayBillOnline");
            jsonPayload.put("Amount", checkoutRequest.getAmount());
            jsonPayload.put("PartyA", checkoutRequest.getPhoneNumber());
            jsonPayload.put("PartyB", mpesaDetails.getShortCode());
            jsonPayload.put("PhoneNumber", checkoutRequest.getPhoneNumber());
            jsonPayload.put("CallBackURL", gatewayProperties.getCheckoutResponseCallbackUrl());
            jsonPayload.put("AccountReference", "Trans");
            jsonPayload.put("TransactionDesc", "Trans");

            return processToMpesa(mpesaDetails,jsonPayload,transactionType);

        } catch (Exception e) {
            responseObject.put("status", "99");
            responseObject.put("message", "Unable to create request payload");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("SMS Sending failed failed, [Message= %s, URL= %s]", e.getMessage(), gatewayProperties.getB2cApiUrl()));
        }
        return responseObject;
    }


    public JSONObject processB2CTransactions(MpesaDetails mpesaDetails, MpesaB2CRequest mpesaB2CRequest) {
        JSONObject responseObject = new JSONObject();
        String transactionType = "B2C";

        try {
            JSONObject jsonPayload = new JSONObject();
            jsonPayload.put("InitiatorName",  mpesaDetails.getInitiatorName());
            jsonPayload.put("SecurityCredential", mpesaDetails.getSecurityCredetials());
            jsonPayload.put("CommandID", mpesaDetails.getCommandId());
            jsonPayload.put("Amount", mpesaB2CRequest.getAmount());
            jsonPayload.put("PartyA",mpesaDetails.getShortCode());
            jsonPayload.put("PartyB", mpesaB2CRequest.getPhoneNumber());
            jsonPayload.put("Remarks", "Send Transaction");
            jsonPayload.put("QueueTimeOutURL", gatewayProperties.getB2cQueueTimeOutURL());
            jsonPayload.put("ResultURL", gatewayProperties.getB2cResultUrl());
            jsonPayload.put("Occassion", "");

            return processToMpesa(mpesaDetails,jsonPayload,transactionType);

        } catch (Exception e) {
            responseObject.put("status", "99");
            responseObject.put("message", "Unable to create request payload");
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("SMS Sending failed failed, [Message= %s, URL= %s]", e.getMessage(), gatewayProperties.getB2cApiUrl()));
        }
        return responseObject;
    }

    private Map<String, String> getAccessToken(MpesaDetails mpesaDetails){
        Map<String, String> tokenDetails = new HashMap<>();
        String app_key = mpesaDetails.getConsumerKey();
        String app_secret = mpesaDetails.getConsumerSecret();
        String appKeySecret = app_key + ":" + app_secret;
        tokenDetails.put("STATUS" , "FAILED");

        try {
            byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
            String auth = Base64.encodeBase64String(bytes);

            RequestBuilder builder = new RequestBuilder("GET");
            builder
                    .setUrl("https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials")
                    .addHeader("Authorization", "Basic " + auth)
                    .addHeader("cache-control", "no-cache")
                    .build();

            JSONObject jsonObject = mpesaIntegration(builder);
            tokenDetails.put("TOKEN" , jsonObject.getString("access_token"));
            tokenDetails.put("STATUS" , "SUCCESS");

        } catch (Exception e) {
            //LOGGER.info("Exception After'{}'", e);
        }
        return tokenDetails;
    }
}
