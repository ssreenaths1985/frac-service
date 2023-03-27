package com.tarento.frac.validation;

import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.utils.Constants;

public class KeycloakValidation {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakValidation.class);
	private static final Charset US_ASCII = Charset.forName("US-ASCII");
	private static ObjectMapper mapper = new ObjectMapper();

	// Token validation
	public static UserInfo verifyUserToken(String token, boolean checkActive) {
		try {
			Map<String, Object> payload = validateToken(token, checkActive);
			if (!CollectionUtils.isEmpty(payload) && checkIss((String) payload.get(Constants.Parameters.ISS))) {
				UserInfo userInfo = mapper.convertValue(payload, UserInfo.class);
				userInfo.setAuthToken(token);
				return userInfo;
			}
		} catch (Exception ex) {
			LOGGER.error(String.format("Exception in verifyUserToken : %s", ex.getMessage()));
		}
		return null;
	}

	/**
	 * Extracts, validates token, and checks expiry date if checkActive params is
	 * true
	 *
	 * @param token
	 *            String
	 * @param checkActive
	 *            Boolean
	 * @return Map<String, Object>
	 * @throws Exception
	 */
	private static Map<String, Object> validateToken(String token, boolean checkActive) throws Exception {
		String[] tokenElements = token.split("\\.");
		String header = tokenElements[0];
		String body = tokenElements[1];
		String signature = tokenElements[2];
		String payLoad = header + Constants.Parameters.DOT_SEPARATOR + body;
		Map<Object, Object> headerData = mapper.readValue(new String(decodeFromBase64(header)), Map.class);
		String keyId = headerData.get(Constants.Parameters.KID).toString();
		boolean isValid = verifyRSASign(payLoad, decodeFromBase64(signature),
				KeyManager.getPublicKey(keyId).getPublicKey(), Constants.Parameters.SHA_256_WITH_RSA);
		if (isValid) {
			Map<String, Object> tokenBody = mapper.readValue(new String(decodeFromBase64(body)), Map.class);
			if (checkActive && isExpired((Integer) tokenBody.get(Constants.Parameters.EXP))) {
				return Collections.emptyMap();
			}
			return tokenBody;
		}
		return Collections.emptyMap();
	}

	public static boolean verifyRSASign(String payLoad, byte[] signature, PublicKey key, String algorithm) {
		Signature sign;
		try {
			sign = Signature.getInstance(algorithm);
			sign.initVerify(key);
			sign.update(payLoad.getBytes(US_ASCII));
			return sign.verify(signature);
		} catch (NoSuchAlgorithmException e) {
			return false;
		} catch (InvalidKeyException e) {
			return false;
		} catch (SignatureException e) {
			return false;
		}
	}

	private static boolean checkIss(String iss) {
		return (KeyManager.getIssuer().equalsIgnoreCase(iss));
	}

	private static boolean isExpired(Integer expiration) {
		Date expiryDate = new Date(expiration);
		Date todaysDate = new Date();
		return todaysDate.before(expiryDate);
	}

	private static byte[] decodeFromBase64(String data) {
		return Base64Util.decode(data, 11);
	}

}
