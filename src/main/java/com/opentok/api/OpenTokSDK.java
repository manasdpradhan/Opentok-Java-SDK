
/*!
 * OpenTok Java Library
 * http://www.tokbox.com/
 *
 * Copyright 2010, TokBox, Inc.
 *
 * Last modified: @opentok.sdk.java.mod_time@
 */

package com.opentok.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.DatatypeConverter;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;
import com.opentok.util.Base64;
import com.opentok.util.GenerateMac;
import com.opentok.util.TokBoxXML;

public class OpenTokSDK {

    private int apiKey;
    private String apiSecret;
    private AsyncHttpClient client;

    
    private static String apiUrl = "https://api.tokbox.com";


    public OpenTokSDK(int apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret.trim();
        this.client = new AsyncHttpClient();
    }

    /**
     *
     * Generate a token which is passed to the JS API to enable widgets to connect to the Opentok api.
     *
     *    * @sessionId: Specify a sessionId to make this token only valid for that sessionId. Tokens generated without a valid sessionId will be rejected and the client might be disconnected.
     * @role: One of the constants defined in RoleConstants. Default is publisher, look in the documentation to learn more about roles.
     * @expireTime: Integer timestamp. You can override the default token expire time of 24h by choosing an explicit expire time. Can be up to 7d after create_time.
     */
    public String generateToken(String sessionId, String role, long expireTime, String connectionData) throws OpenTokException {

        if(sessionId == null || sessionId == "") {
            throw new OpenTokException("Null or empty session ID are not valid");   
        }
        String decodedSessionId = "";
        try { 
            String subSessionId = sessionId.substring(2);
            for (int i = 0; i<3; i++){
                String newSessionId = subSessionId.concat(repeatString("=",i));
                decodedSessionId = new String(DatatypeConverter.parseBase64Binary(
                        newSessionId.replace('-', '+').replace('_', '/')), "ISO8859_1");
                if (decodedSessionId.contains("~")){ 
                    break;
                }
            }
            if(!decodedSessionId.split("~")[1].equals(String.valueOf(apiKey))) {
                throw new OpenTokException("An invalid session ID was passed");
            }
        } catch (Exception e) {
            throw new OpenTokException("An invalid session ID was passed");
        }


        Long create_time = new Long(System.currentTimeMillis() / 1000).longValue();
        StringBuilder dataStringBuilder = new StringBuilder();
        //Build the string
        Random random = new Random();
        int nonce = random.nextInt();
        dataStringBuilder.append("session_id=");
        dataStringBuilder.append(sessionId);
        dataStringBuilder.append("&create_time=");
        dataStringBuilder.append(create_time);
        dataStringBuilder.append("&nonce=");
        dataStringBuilder.append(nonce);
        dataStringBuilder.append("&role=");
        dataStringBuilder.append(role);

        if(!RoleConstants.SUBSCRIBER.equals(role) &&
                !RoleConstants.PUBLISHER.equals(role) &&
                !RoleConstants.MODERATOR.equals(role) &&
                !"".equals(role))
            throw new OpenTokException(role + " is not a recognized role");

        if (expireTime != 0) {
            if(expireTime < (System.currentTimeMillis() / 1000)-1)
                throw new OpenTokException("Expire time must be in the future");
            if(expireTime > (System.currentTimeMillis() / 1000 + 2592000))
                throw new OpenTokException("Expire time must be in the next 30 days");
            dataStringBuilder.append("&expire_time=");
            dataStringBuilder.append(expireTime);
        }

        if (connectionData != null) {
            if(connectionData.length() > 1000)
                throw new OpenTokException("Connection data must be less than 1000 characters");
            dataStringBuilder.append("&connection_data=");
            try {
                dataStringBuilder.append(URLEncoder.encode(connectionData, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Error during URL encode of your connectionData.", e);
            };
        }


        StringBuilder tokenStringBuilder = new StringBuilder();
        try {
            tokenStringBuilder.append("T1==");

            StringBuilder innerBuilder = new StringBuilder();
            innerBuilder.append("partner_id=");
            innerBuilder.append(this.apiKey);

            innerBuilder.append("&sig=");

            innerBuilder.append(GenerateMac.calculateRFC2104HMAC(dataStringBuilder.toString(),
                    this.apiSecret));
            innerBuilder.append(":");
            innerBuilder.append(dataStringBuilder.toString());

            tokenStringBuilder.append(Base64.encode(innerBuilder.toString()));

        }catch (java.security.SignatureException e) {
            throw new OpenTokException(e.getMessage());
        }

        return tokenStringBuilder.toString();
    }

    /**
     * Overloaded functions
     * These work the same as those defined above, but with optional params filled in with defaults
     */

    public String generateToken(String sessionId) throws OpenTokException {
        return this.generateToken(sessionId, RoleConstants.PUBLISHER, 0, null);
    }


    public String generateToken(String sessionId, String role) throws OpenTokException {
        return this.generateToken(sessionId, role, 0, null);
    }

    public String generateToken(String sessionId, String role, Long expireTime) throws OpenTokException {
        return this.generateToken(sessionId, role, expireTime, null);
    }

    public String createSession() throws OpenTokException {
        return createSession(null, false);
    }

    public String createSession(String location) throws OpenTokException {
        return createSession(location, false);
    }

    private String createSession(String location, boolean p2pPreference) throws OpenTokException {
        Map<String, String> params = new HashMap<String, String>(); 
        if(null != location) {
            params.put("location", location);
        }
        if (p2pPreference) {
            params.put("p2p.preference", "enabled");
        }
        
        TokBoxXML xmlResponse = this.do_request("/session/create", params);
        if(xmlResponse.hasElement("error", "Errors")) {
            throw new OpenTokException("Unable to create session");
        }
        return xmlResponse.getElementValue("session_id", "Session");
    }

    private static String repeatString(String str, int times){
        StringBuilder ret = new StringBuilder();
        for(int i = 0;i < times;i++) ret.append(str);
        return ret.toString();
    }

    protected TokBoxXML do_request(String url, Map<String, String> params) throws OpenTokException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-TB-PARTNER-AUTH", this.apiKey + ":" + this.apiSecret);

        return new TokBoxXML(makePostRequest(url, headers, params, null));
    }
    
    private String makeGetRequest(String resource) throws IOException {
        BoundRequestBuilder get = this.client.prepareGet(apiUrl + resource);
        get.addHeader("X-TB-PARTNER-AUTH", String.format("%s:%s", apiKey, apiSecret));
        get.addHeader("X-TB-VERSION", "1");

        try {
            Response result = get.execute().get();
            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
                throw new IOException("Error response: " + result.getStatusCode() + " message: "
                        + result.getStatusText());
            }
            return result.getResponseBody();
        } catch (InterruptedException e) {
            throw new IOException(e);
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        }
    }

    private String makePostRequest(String resource, Map<String, String> headers, Map<String, String> params,
            String postData) throws OpenTokException {
        BoundRequestBuilder post = this.client.preparePost(apiUrl + resource);
        if (params != null) {
            for (Entry<String, String> pair : params.entrySet()) {
                post.addParameter(pair.getKey(), pair.getValue());
            }
        }

        if (headers != null) {
            for (Entry<String, String> pair : headers.entrySet()) {
                post.addHeader(pair.getKey(), pair.getValue());
            }
        }

        post.addHeader("X-TB-PARTNER-AUTH", String.format("%s:%s", apiKey, apiSecret));
        post.addHeader("X-TB-VERSION", "1");

        if (postData != null) {
            post.setBody(postData);
        }

        try {
            Response result = post.execute().get();

            if (result.getStatusCode() < 200 || result.getStatusCode() > 299) {
                throw new OpenTokException(result.getStatusText());
            }

            return result.getResponseBody();
        } catch (InterruptedException e) {
            throw new OpenTokException(e.getMessage());
        } catch (ExecutionException e) {
            throw new OpenTokException(e.getMessage());
        } catch (IOException e) {
            throw new OpenTokException(e.getMessage());
        }
    }
}
