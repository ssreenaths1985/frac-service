package com.tarento.frac;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.tarento.frac.models.UserInfo;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.ResponseGenerator;
import com.tarento.frac.validation.KeycloakValidation;

@Component
public class RequestInterceptor implements HandlerInterceptor {

	public static final Logger LOGGER = LoggerFactory.getLogger(RequestInterceptor.class);

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object obj) throws Exception {
		// read auth token from header
		String authToken = request.getHeader(Constants.Parameters.AUTHORIZATION);
		if (StringUtils.isBlank(authToken)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter()
					.write(ResponseGenerator.failureResponse(Constants.ResponseMessages.AUTH_TOKEN_MISSING));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		authToken = authToken.replace(Constants.Parameters.BEARER, StringUtils.EMPTY);
		authToken = authToken.replace("Bearer ", StringUtils.EMPTY);
		// authentication
		UserInfo userInfo = KeycloakValidation.verifyUserToken(authToken, true);
		if (userInfo == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter()
					.write(ResponseGenerator.failureResponse(Constants.ResponseMessages.UNABLE_TO_AUTHENTICATE));
			response.setContentType(MediaType.APPLICATION_JSON);
			return Boolean.FALSE;
		}
		request.setAttribute(Constants.Parameters.USER_INFO, userInfo);
		return Boolean.TRUE;
	}

	@Override
	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
			throws Exception {
	}

	@Override
	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
			throws Exception {
	}

}
