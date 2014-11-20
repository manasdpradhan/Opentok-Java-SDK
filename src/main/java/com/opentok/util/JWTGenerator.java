package com.opentok.util;

import java.util.Calendar;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.opentok.exception.OpenTokException;

public class JWTGenerator {


    public static String generateJWTToken(int key, String secret, String scope, String issuerType) throws OpenTokException {

        // Create HMAC signer
        JWSSigner signer = new MACSigner(secret);

        // Prepare JWT with claims set

        Calendar now = Calendar.getInstance();

        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setIssueTime(now.getTime());
        now.add(Calendar.HOUR, 1);
        claimsSet.setExpirationTime(now.getTime());

        claimsSet.setIssuer(String.valueOf(key));
        claimsSet.setClaim("scope", scope);
        claimsSet.setClaim("ist", issuerType);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

        // Apply the HMAC
        try {
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new OpenTokException("Could not create JWT Token", e);
        }
    }
}
