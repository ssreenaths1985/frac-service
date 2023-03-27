package com.tarento.frac.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service(Constants.ServiceRepositories.AUTH_UTIL)
public class AuthUtil {

	public static final Logger LOGGER = LoggerFactory.getLogger(AuthUtil.class);

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ApplicationProperties appProperties;

	private static final String AUTHORIZATION = "Authorization";

	/**
	 * To get user role from userId
	 * 
	 * @param userId
	 *            String
	 * @param token
	 *            String
	 * @return List<String>
	 */
	public List<String> getUserRole(String userId, String token) {
		try {
			Map<String, Object> responseMap = getUserDetails(userId, token);
			List<String> rolesMap = new ObjectMapper().convertValue(responseMap.get("roles"), ArrayList.class);
			return rolesMap;
		} catch (Exception e) {
			LOGGER.error(String.format("Error in getUserRole method %s", e.getMessage()));
		}
		return null;
	}

	/**
	 * Returns user email id from user id
	 * 
	 * @param userId
	 *            String
	 * @param token
	 *            String
	 * @return String
	 */
	public String getUserEmail(String userId, String token) {
		try {
			Map<String, Object> responseMap = getUserDetails(userId, token);
			Map<String, Object> profileDetails = new ObjectMapper().convertValue(responseMap.get("profiledetails"),
					HashMap.class);
			Map<String, Object> personalDetails = new ObjectMapper().convertValue(profileDetails.get("personalDetails"),
					HashMap.class);
			return (String) personalDetails.get("primaryEmail");
		} catch (Exception e) {
			LOGGER.error(String.format("Error in getUserEmail method %s", e.getMessage()));
		}
		return null;
	}

	/**
	 * To get user details using userId
	 * 
	 * @param userId
	 *            String
	 * @param token
	 *            String
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getUserDetails(String userId, String token) {
		try {
			String[] subId = userId.split(":");
			String wid = subId.length == 3 ? subId[2] : subId[0];
			String url = appProperties.getRoleVerificationUrl() + wid;
			LOGGER.info("Fetching user details: " + url);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set(AUTHORIZATION, appProperties.getAuthAPIKey());
			headers.set(Constants.Parameters.X_USER_TOKEN, token.replace("bearer ", ""));
			ResponseEntity<Object> readResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers),
					Object.class);
			Map<String, Object> response = new ObjectMapper().convertValue(readResponse.getBody(), HashMap.class);
			Map<String, Object> resultMap = new ObjectMapper().convertValue(response.get("result"), HashMap.class);
			return new ObjectMapper().convertValue(resultMap.get("response"), HashMap.class);

		} catch (Exception e) {
			LOGGER.error(String.format("Error in getUserDetails method %s", e.getMessage()));
		}
		return null;
	}

	/**
	 * A helper method to create the headers for Rest Connection with UserName and
	 * Password
	 * 
	 * @return HttpHeaders
	 */
	private HttpHeaders getHttpHeaders(String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(AUTHORIZATION, token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}
}