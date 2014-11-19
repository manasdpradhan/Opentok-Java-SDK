package com.opentok;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentok.Partner.Status;
import com.opentok.constants.Config;
import com.opentok.exception.OpenTokException;
import com.opentok.exception.RequestException;
import com.opentok.util.HttpClient;

public class Provisioning {

    private final HttpClient client;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * This class is used to manage partner keys and requires user level credentials
     * @param userKey
     * @param userSecret
     */
    public Provisioning(int userKey, String userSecret) {
        this(userKey, userSecret, Config.API_URL);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Provisioning(int masterKey, String masterSecret, String apiUrl) {
        this.client = new HttpClient.Builder(masterKey, masterSecret)
                .useUserCredentials()
                .apiUrl(apiUrl)
                .build();
    }

    /**
     *  Creates an api key for the user account.
     * @param name Any identifier that is returned back in the partner object
     * @return Partner object
     *
     * @throws RequestException
     * @throws OpenTokException
     */
    public Partner createPartner(String name) throws RequestException, OpenTokException {
        String response = client.createPartner(name);
        return extractPartner(response);
    }

    /**
     * Delete an api key. The api key will not be accessible once deleted
     * @param partnerId
     * @throws RequestException
     * @throws OpenTokException
     */
    public void deletePartner(int partnerId) throws RequestException, OpenTokException {
        client.deletePartner(partnerId);
    }

    private Partner extractPartner(String response) throws OpenTokException {
        try {
            return MAPPER.readValue(response, Partner.class);
        } catch (JsonParseException e) {
            throw new OpenTokException("Could not map json to an OpenTok partner", e);
        } catch (JsonMappingException e) {
            throw new OpenTokException("Could not map json to an OpenTok partner", e);
        } catch (IOException e) {
            throw new OpenTokException("Could not map json to an OpenTok partner", e);
        }
    }

    /**
     * Retrieve a partner object
     * @param partnerId Api Key for the partner you wish to get details about
     * @return
     * @throws RequestException
     * @throws OpenTokException
     */
    public Partner getPartner(int partnerId) throws RequestException, OpenTokException {
        String response = client.getPartner(partnerId);
        return extractPartner(response);
    }

    /**
     *  Update existing partner
     * @param partnerId
     * @param status
     * @return
     * @throws RequestException
     * @throws OpenTokException
     */
    public Partner updatePartner(int partnerId, Status status) throws RequestException, OpenTokException {
        String response = client.updatePartner(partnerId, status);
        return extractPartner(response);
    }

    /**
     * Lists all the partners for the user account.
     * @return
     * @throws RequestException
     * @throws OpenTokException
     */
    public List<Partner> listPartners() throws RequestException, OpenTokException {
        String response = client.listPartners();
        try {
            return MAPPER.readValue(response, new TypeReference<List<Partner>>(){});
        } catch (JsonParseException e) {
            throw new OpenTokException("Could not map json to an OpenTok partner", e);
        } catch (JsonMappingException e) {
            throw new OpenTokException("Could not map json to an OpenTok partner", e);
        } catch (IOException e) {
            throw new OpenTokException("Could not map json to an OpenTok partner", e);
        }
    }

    /**
     * Refreshes the api secret for the api key passed.
     * @param partnerId
     * @return
     * @throws RequestException
     * @throws OpenTokException
     */
    public Partner refreshSecret(int partnerId) throws RequestException, OpenTokException {
        String response = client.refreshSecret(partnerId);
        return extractPartner(response);
    }


}
