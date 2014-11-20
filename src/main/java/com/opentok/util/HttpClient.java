package com.opentok.util;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.FluentStringsMap;
import com.ning.http.client.Response;
import com.opentok.StorageType;
import com.opentok.Partner.Status;
import com.opentok.constants.Config;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;

public class HttpClient extends AsyncHttpClient {

    private final String apiUrl;
    private final String apiSecret;
    private final int apiKey;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public enum IssuerType {
        user, partner
    }
    private final IssuerType ist;

    private HttpClient(Builder builder) {
        super(builder.config);
        this.apiSecret = builder.apiSecret;
        this.apiKey = builder.apiKey;
        this.apiUrl = builder.apiUrl;
        this.ist = builder.ist;
    }

    public String createSession(Map<String, Collection<String>> params) throws OpenTokException {
        Future<Response> request = null;
        Response response = null;
        FluentStringsMap paramsString = new FluentStringsMap().addAll(params);

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "session.create", ist.name());
            request = this.preparePost(this.apiUrl + "/session/create")
                    .setParameters(paramsString)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        }

        try {
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not create an OpenTok Session. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        } catch (IOException e) {
            throw new RequestException("Could not create an OpenTok Session", e);
        }
    }

    public String createPartner(String name) throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;

        BoundRequestBuilder builder = this.preparePost(this.apiUrl + "/v2/partner/");
        if (!StringUtils.isEmpty(name)) {
            HashMap<String, String> jsonBody = new HashMap<String, String>();
            jsonBody.put("name", name);

            try {
                String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "partner.create", ist.name());
                String requestBody = MAPPER.writeValueAsString(jsonBody);
                builder.setBody(requestBody)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .setHeader("Content-Type", "application/json");
            } catch (JsonProcessingException e) {
                throw new OpenTokException("Could not create an OpenTok partner. The JSON body encoding failed.", e);
            }
        }

        try {
            request = builder.execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not create an OpenTok partner. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not create an OpenTok partner", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not create an OpenTok partner", e);
        } catch (IOException e) {
            throw new RequestException("Could not create an OpenTok partner", e);
        }
    }

    public String updatePartner(int id, Status status) throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;
        BoundRequestBuilder builder = this.preparePut(this.apiUrl + "/v2/partner/" + id);

        HashMap<String, String> jsonBody = new HashMap<String, String>();
        jsonBody.put("status", status.name());

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "partner.update", ist.name());
            String requestBody = MAPPER.writeValueAsString(jsonBody);
            builder.setBody(requestBody)
                .addHeader("X-OPENTOK-AUTH", token)
                .setHeader("Content-Type", "application/json");
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not create an OpenTok partner. The JSON body encoding failed.", e);
        }

        try {
            request = builder.execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not create an OpenTok partner. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not create an OpenTok partner", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not create an OpenTok partner", e);
        } catch (IOException e) {
            throw new RequestException("Could not create an OpenTok partner", e);
        }
    }

    public String deletePartner(int partnerId) throws RequestException, OpenTokException {
        Future<Response> request = null;
        String url = this.apiUrl + "/v2/partner/" + partnerId;

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "partner.delete", ist.name());
            request = this.prepareDelete(url)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not delete an OpenTok Partner. partnerId = " + partnerId, e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not delete an OpenTok Partner. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not delete an OpenTok Partner. Partner not found. PartnerId = " + partnerId);
                case 500:
                    throw new RequestException("Could not delete an OpenTok Partner. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Partner. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not delete an OpenTok Partner. archiveId = " + partnerId, e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not delete an OpenTok Partner. archiveId = " + partnerId, e);
        } catch (IOException e) {
            throw new RequestException("Could not delete an OpenTok Partner. archiveId = " + partnerId, e);
        }
    }

    public String getPartner(int id) throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;
        BoundRequestBuilder builder = this.prepareGet(this.apiUrl + "/v2/partner/" + id);

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "partner.read", ist.name());

            request = builder.addHeader("X-OPENTOK-AUTH", token)
                    .execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                default:
                    throw new RequestException("Could not update an OpenTok partner. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not update an OpenTok partner", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not update an OpenTok partner", e);
        } catch (IOException e) {
            throw new RequestException("Could not update an OpenTok partner", e);
        }
    }

    public String listPartners() throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;
        BoundRequestBuilder builder = this.prepareGet(this.apiUrl + "/v2/partner/");

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "partner.list", ist.name());

            request = builder.addHeader("X-OPENTOK-AUTH", token)
                    .execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();

                default:
                    throw new RequestException("Could not list OpenTok partners. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not list OpenTok partners", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not list OpenTok partners", e);
        } catch (IOException e) {
            throw new RequestException("Could not list OpenTok partners", e);
        }
    }

    public String refreshSecret(int id) throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;
        BoundRequestBuilder builder = this.preparePost(this.apiUrl + "/v2/partner/" + id + "/refreshSecret");

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "partner.refreshSecret", ist.name());

            request = builder.addHeader("X-OPENTOK-AUTH", token)
                    .execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();

                default:
                    throw new RequestException("Could not refresh OpenTok partner secret. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not refresh OpenTok partner secret", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not refresh OpenTok partner secret", e);
        } catch (IOException e) {
            throw new RequestException("Could not refresh OpenTok partner secret", e);
        }
    }

    public String updateArchiveStorage(StorageType storageType, Map<String, String> config, Fallback fallback) throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;
        BoundRequestBuilder builder = this.preparePut(this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/storage");

        HashMap<String, Object> jsonBody = new HashMap<String, Object>();
        jsonBody.put("type", storageType.name());
        jsonBody.put("config", config);
        jsonBody.put("fallback", fallback);

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.storage.update", ist.name());
            String requestBody = MAPPER.writeValueAsString(jsonBody);
            builder.setBody(requestBody)
                .addHeader("X-OPENTOK-AUTH", token)
                .setHeader("Content-Type", "application/json");
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not update archive storage for an OpenTok partner. The JSON body encoding failed.", e);
        }

        try {
            request = builder.execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();

                default:
                    throw new RequestException("Could not update archive storage for an OpenTok partner. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not update archive storage for an OpenTok partner", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not update archive storage for an OpenTok partner", e);
        } catch (IOException e) {
            throw new RequestException("Could not update archive storage for an OpenTok partner", e);
        }
    }


    public String updateCallbackUrl(String group, String event, String url) throws OpenTokException, RequestException {
        Future<Response> request = null;
        Response response = null;
        BoundRequestBuilder builder = this.preparePost(this.apiUrl + "/v2/partner/" + this.apiKey + "/callback");

        HashMap<String, Object> jsonBody = new HashMap<String, Object>();
        jsonBody.put("group", group);
        jsonBody.put("event", event);
        jsonBody.put("url", url);

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "callback", ist.name());
            String requestBody = MAPPER.writeValueAsString(jsonBody);
            builder.setBody(requestBody)
                .addHeader("X-OPENTOK-AUTH", token)
                .setHeader("Content-Type", "application/json");
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not update callback url for an OpenTok partner. The JSON body encoding failed.", e);
        }

        try {
            request = builder.execute();
            response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();

                default:
                    throw new RequestException("Could not update callback url for an OpenTok partner. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not update callback url for an OpenTok partner", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not update callback url for an OpenTok partner", e);
        } catch (IOException e) {
            throw new RequestException("Could not update callback url for an OpenTok partner", e);
        }
    }

    public String deleteArchiveStorage() throws RequestException, OpenTokException {
        Future<Response> request = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/storage";

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.storage.delete", ist.name());
            request = this.prepareDelete(url)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not delete archive storage for an OpenTok Partner = " + this.apiKey, e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not delete archive storage for an OpenTok Partner. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not delete archive storage for an OpenTok Partner. No upload target exists. partnerId = "
                            + this.apiKey);
                case 500:
                    throw new RequestException("Could not delete archive storage for an OpenTok Partner. A server error occurred.");
                default:
                    throw new RequestException("Could not get archive storage for an OpenTok Partnere. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not delete archive storage for an OpenTok Partner = " + this.apiKey, e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not delete archive storage for an OpenTok Partner = " + this.apiKey, e);
        } catch (IOException e) {
            throw new RequestException("Could not delete archive storage for an OpenTok Partner = " + this.apiKey, e);
        }
    }

    public String getArchive(String archiveId) throws RequestException, OpenTokException {
        Future<Response> request = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId;

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.read", ist.name());
            request = this.prepareGet(url)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not get an OpenTok Archive", e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();

                case 400:
                    throw new RequestException("Could not get an OpenTok Archive. The archiveId was invalid. " +
                            "archiveId: " + archiveId);
                case 403:
                    throw new RequestException("Could not get an OpenTok Archive. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not get an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not get an OpenTok Archive", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not get an OpenTok Archive", e);
        } catch (IOException e) {
            throw new RequestException("Could not  get an OpenTok Archive", e);
        }
    }

    public String getArchives(int offset, int count) throws RequestException, OpenTokException {
        Future<Response> request = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive";
        if (offset != 0 || count != 1000) {
            url += "?";
            if (offset != 0) {
                url += ("offset=" + Integer.toString(offset) + '&');
            }
            if (count != 1000) {
                url += ("count=" + Integer.toString(count));
            }
        }

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.list", ist.name());
            request = this.prepareGet(url)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not get OpenTok Archives. The request was not authorized.");
                case 500:
                    throw new RequestException("Could not get OpenTok Archives. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        } catch (IOException e) {
            throw new RequestException("Could not get OpenTok Archives", e);
        }
    }

    public String startArchive(String sessionId, String name) throws OpenTokException, RequestException {
        Future<Response> request = null;
        String requestBody = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive";

        HashMap<String, String> jsonBody = new HashMap<String, String>();
        jsonBody.put("sessionId", sessionId);
        if (name != null) {
            jsonBody.put("name", name);
        }
        try {
            requestBody = MAPPER.writeValueAsString(jsonBody);
        } catch (JsonProcessingException e) {
            throw new OpenTokException("Could not start an OpenTok Archive. The JSON body encoding failed.", e);
        }
        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.create", ist.name());
            request = this.preparePost(url)
                    .setBody(requestBody)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .setHeader("Content-Type", "application/json")
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not start an OpenTok Archive. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not start an OpenTok Archive. The sessionId does not exist. " +
                            "sessionId = " + sessionId);
                case 409:
                    throw new RequestException("Could not start an OpenTok Archive. The session is either " +
                            "peer-to-peer or already recording. sessionId = " + sessionId);
                case 500:
                    throw new RequestException("Could not start an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not start an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        } catch (IOException e) {
            throw new RequestException("Could not start an OpenTok Archive.", e);
        }
    }

    public String stopArchive(String archiveId) throws RequestException, OpenTokException {
        Future<Response> request = null;
        // TODO: maybe use a StringBuilder?
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId + "/stop";

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.stop", ist.name());
            request = this.preparePost(url)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not stop an OpenTok Archive. archiveId = " + archiveId, e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 200:
                    return response.getResponseBody();

                case 400:
                    // NOTE: the REST api spec talks about sessionId and action, both of which aren't required.
                    //       see: https://github.com/opentok/OpenTok-2.0-archiving-samples/blob/master/REST-API.md#stop_archive
                    throw new RequestException("Could not stop an OpenTok Archive.");
                case 403:
                    throw new RequestException("Could not stop an OpenTok Archive. The request was not authorized.");
                case 404:
                    throw new RequestException("Could not stop an OpenTok Archive. The archiveId does not exist. " +
                            "archiveId = " + archiveId);
                case 409:
                    throw new RequestException("Could not stop an OpenTok Archive. The archive is not being recorded. " +
                            "archiveId = " + archiveId);
                case 500:
                    throw new RequestException("Could not stop an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not stop an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not stop an OpenTok Archive.", e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not stop an OpenTok Archive.", e);
        } catch (IOException e) {
            throw new RequestException("Could not stop an OpenTok Archive.", e);
        }
    }

    public String deleteArchive(String archiveId) throws RequestException, OpenTokException {
        Future<Response> request = null;
        String url = this.apiUrl + "/v2/partner/" + this.apiKey + "/archive/" + archiveId;

        try {
            String token = JWTGenerator.generateJWTToken(apiKey, apiSecret, "archive.delete", ist.name());
            request = this.prepareDelete(url)
                    .addHeader("X-OPENTOK-AUTH", token)
                    .execute();
        } catch (IOException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        }

        try {
            Response response = request.get();
            switch (response.getStatusCode()) {
                case 204:
                    return response.getResponseBody();
                case 403:
                    throw new RequestException("Could not delete an OpenTok Archive. The request was not authorized.");
                case 409:
                    throw new RequestException("Could not delete an OpenTok Archive. The status was not \"uploaded\"," +
                            " \"available\", or \"deleted\". archiveId = " + archiveId);
                case 500:
                    throw new RequestException("Could not delete an OpenTok Archive. A server error occurred.");
                default:
                    throw new RequestException("Could not get an OpenTok Archive. The server response was invalid." +
                            " response code: " + response.getStatusCode());
            }

        // if we only wanted Java 7 and above, we could DRY this into one catch clause
        } catch (InterruptedException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        } catch (ExecutionException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        } catch (IOException e) {
            throw new RequestException("Could not delete an OpenTok Archive. archiveId = " + archiveId, e);
        }
    }

    public static class Builder {
        private final int apiKey;
        private final String apiSecret;
        private String apiUrl;
        private IssuerType ist;

        private AsyncHttpClientConfig config;

        public Builder(int apiKey, String apiSecret) {
            this.apiKey = apiKey;
            this.apiSecret = apiSecret;
        }

        public Builder usePartnerCredentials() {
            this.ist = IssuerType.partner;
            return this;
        }

        public Builder useUserCredentials() {
            this.ist = IssuerType.user;
            return this;
        }

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public HttpClient build() {
            this.config = new AsyncHttpClientConfig.Builder()
                    .setUserAgent("Opentok-Java-SDK/"+ Config.VERSION)
                    .build();
            // NOTE: not thread-safe, config could be modified by another thread here?
            HttpClient client = new HttpClient(this);
            return client;
        }
    }
}
