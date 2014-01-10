/*
 * These unit tests require the opentok Java SDK.
 * https://github.com/opentok/Opentok-Java-SDK.git
 * 
 */

package com.opentok.test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.Response;
import com.opentok.api.API_Config;
import com.opentok.api.OpenTokSDK;
import com.opentok.api.constants.RoleConstants;
import com.opentok.exception.OpenTokException;
import com.opentok.util.TokBoxXML;

public class UnitTest {

    private OpenTokSDK sdk;

    private int apiKey;
    private String apiSecret;
    private AsyncHttpClient client;

    public UnitTest() {
        apiKey = Integer.valueOf(System.getProperty("apiKey"));
        apiSecret = System.getProperty("apiSecret");
        sdk = new OpenTokSDK(apiKey, apiSecret);
        client = new AsyncHttpClient();
    }

    private TokBoxXML get_session_info(String session_id) throws OpenTokException {
        String token = sdk.generateToken(session_id);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-TB-TOKEN-AUTH", token );
        TokBoxXML xml;
        xml = new TokBoxXML(makePostRequest("/session/" + session_id + "?extended=true", headers, new HashMap<String, String>(), null));
        return xml;
    }

    private TokBoxXML get_token_info(String token) throws OpenTokException {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("X-TB-TOKEN-AUTH",token);
        TokBoxXML xml;
        xml = new TokBoxXML(makePostRequest("/token/validate", headers, new HashMap<String, String>(), null));
        return xml;
    }

    @Test
    public void testCreateSesionNoParams() throws OpenTokException {
        String session = sdk.createSession();
        TokBoxXML xml = get_session_info(session);
        String expected = session;
        String actual = xml.getElementValue("session_id", "Session");
        Assert.assertEquals("Java SDK tests: Session create with no params failed", expected, actual);
    }

    @Test
    public void testCreateSesionWithLocation() throws OpenTokException {
        String session = sdk.createSession("216.38.134.114");
        TokBoxXML xml = get_session_info(session);
        String expected = session;
        String actual = xml.getElementValue("session_id", "Session");
        Assert.assertEquals("Java SDK tests: Session create with location failed", expected, actual);
    }

    @Test
    public void testP2PPreferenceEnable() throws OpenTokException {
        String expected = "enabled";
        
        String s = sdk.createSession("216.38.134.114", true);
        TokBoxXML xml = get_session_info(s);
        String actual = xml.getElementValue("preference", "p2p");
        Assert.assertEquals("Java SDK tests: p2p not enabled", expected, actual);
    }

    @Test
    public void testP2PPreferenceDisable() throws OpenTokException {
        String expected = "disabled";
        String s = sdk.createSession("216.38.134.114", false);
        TokBoxXML xml = get_session_info(s);
        String actual = xml.getElementValue("preference", "p2p");
        Assert.assertEquals("Java SDK tests: p2p not disabled", expected, actual);
    }

    @Test
    public void testRoleDefault() throws OpenTokException {
        String s= sdk.createSession();
        String t = sdk.generateToken(s);
        TokBoxXML xml = get_token_info(t);

        String expectedRole = "publisher";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role default not default (publisher)", expectedRole, actualRole);

        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: default role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertTrue("Java SDK tests: default role does not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertTrue("Java SDK tests: default role does not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertFalse("Java SDK tests: default role should not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRolePublisher() throws OpenTokException {
        String s= sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.PUBLISHER);
        TokBoxXML xml = get_token_info(t);

        String expectedRole = "publisher";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role not publisher", expectedRole, actualRole);

        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: publisher role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertTrue("Java SDK tests: publisher role does not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertTrue("Java SDK tests: publisher role does not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertFalse("Java SDK tests: publisher role should not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRoleSubscriber() throws OpenTokException {
        String s= sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.SUBSCRIBER);
        TokBoxXML xml = get_token_info(t);

        String expectedRole = "subscriber";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role not subscriber", expectedRole, actualRole);

        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: subscriber role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertFalse("Java SDK tests: subscriber role should not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRoleModerator() throws OpenTokException {
        String s= sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.MODERATOR);
        TokBoxXML xml = get_token_info(t);

        String expectedRole = "moderator";
        String actualRole = xml.getElementValue("role", "token").trim();
        Assert.assertEquals("Java SDK tests: role not moderator", expectedRole, actualRole);

        // Permissions are set as an empty node in the xml
        // Verify that the expected permission node is there
        // Verify nodes for permissions not granted to the role are not there
        Assert.assertTrue("Java SDK tests: moderator role does not have subscriber permissions", xml.hasElement("subscribe", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have publisher permissions", xml.hasElement("publish", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have signal permissions", xml.hasElement("signal", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have forceunpublish permissions", xml.hasElement("forceunpublish", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have forcedisconnect permissions", xml.hasElement("forcedisconnect", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have record permissions", xml.hasElement("record", "permissions"));
        Assert.assertTrue("Java SDK tests: moderator role does not have playback permissions", xml.hasElement("playback", "permissions"));
    }

    @Test
    public void testRoleGarbageInput() {
        OpenTokException expected = null;
        try {
            String s= sdk.createSession();
            sdk.generateToken(s, "asdfasdf");
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for role asdfasdf", expected);
    }

    @Test
    public void testRoleNull() {
        OpenTokException expected = null;
        try {
            String s= sdk.createSession();
            sdk.generateToken(s, null);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for role null", expected);
    }

    @Test
    public void testTokenNullSessionId() throws OpenTokException {
        OpenTokException expected = null;
        try {
            sdk.generateToken(null);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for null sessionId", expected);
    }


    public void testTokenEmptySessionId() throws OpenTokException {
        OpenTokException expected = null;
        try {
            sdk.generateToken("");
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for empty sessionId", expected);
    }

    @Test
    public void testTokenIncompleteSessionId() throws OpenTokException {
        OpenTokException expected = null;
        try {
            sdk.generateToken("jkasjda2ndasd");
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for invalid sessionId", expected);
    }

    @Test
    public void testTokenExpireTimeDefault() throws OpenTokException {
        String s= sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.MODERATOR);
        TokBoxXML xml = get_token_info(t);
        Assert.assertFalse("Java SDK tests: expire_time should not exist for default", xml.hasElement("expire_time", "token"));
    }

    @Test
    public void testTokenExpireTimePast() {
        OpenTokException expected = null;
        try {
            String s= sdk.createSession();
            sdk.generateToken(s, RoleConstants.MODERATOR, new Date().getTime() / 1000 - 100);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time in past", expected);
    }

    @Test
    public void testTokenExpireTimeNow() throws OpenTokException {
        long expireTime = new Date().getTime() / 1000;
        String expected = "Token expired on " + expireTime;
        String s = sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.MODERATOR, expireTime);
        // Allow the token to expire.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }
        TokBoxXML xml = get_token_info(t);
        String actual = xml.getElementValue("invalid", "token");
        Assert.assertEquals("Java SDK tests: unexpected invalid token message", expected, actual);
    }

    @Test
    public void testTokenExpireTimeNearFuture() throws OpenTokException {
        long expected = new Date().getTime() / 1000 + 34200;
        String s= sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.MODERATOR, expected);
        TokBoxXML xml = get_token_info(t);
        long actual = new Long(xml.getElementValue("expire_time", "token").trim());
        Assert.assertEquals("Java SDK tests: expire time not set to expected time", expected, actual);
    }

    @Test
    public void testTokenExpireTimeFarFuture() {
        OpenTokException expected = null;
        try {
            String s= sdk.createSession();
            sdk.generateToken(s, RoleConstants.MODERATOR, new Date().getTime() + 604800000);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: exception should be thrown for expire time more than 7 days in future", expected);
    }

    @Test
    public void testConnectionData() throws OpenTokException {
        String expected = "test string";
        String actual = null;
        String s= sdk.createSession();
        String t = sdk.generateToken(s, RoleConstants.PUBLISHER, 0, expected);
        TokBoxXML xml = get_token_info(t);
        actual = xml.getElementValue("connection_data", "token").trim();
        Assert.assertEquals("Java SDK tests: connection data not set", expected, actual);
    }

    @Test
    public void testConnectionDataTooLarge() {
        OpenTokException expected = null;
        String test_string = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb" +
                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc" +
                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "gggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg" +
                "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh" +
                "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii" +
                "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj" +
                "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk" +
                "llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll" +
                "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm" +
                "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn" +
                "oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo";
        try {
            String s= sdk.createSession();
            sdk.generateToken(s, RoleConstants.PUBLISHER, new Date().getTime(), test_string);
        } catch (OpenTokException e) {
            expected = e;
        }
        Assert.assertNotNull("Java SDK tests: connection data over 1000 characters should not be accepted. Test String: " + test_string , expected);
    }

    private String makePostRequest(String resource, Map<String, String> headers, Map<String, String> params,
            String postData) throws OpenTokException {
        BoundRequestBuilder post = this.client.preparePost(API_Config.API_URL + resource);
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
