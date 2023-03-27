package com.tarento.frac.utils;

/**
 *
 * @author Abhishek
 *
 */
public enum ResponseCode {
	UNAUTHORIZED(Constants.ResponseCodes.UNAUTHORIZED_ID, Constants.ResponseCodes.UNAUTHORIZED), SUCCESS(Constants.ResponseCodes.SUCCESS_ID,
			Constants.ResponseCodes.SUCCESS), FAILURE(Constants.ResponseCodes.FAILURE_ID, Constants.ResponseCodes.PROCESS_FAIL);
	private static final String STRING = "";
	/**
	 * error code contains int value
	 */
	private int errorCode;
	/**
	 * errorMessage contains proper error message.
	 */
	private String errorMessage;

	/**
	 * @param errorCode
	 * @param errorMessage
	 */
	private ResponseCode(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	/**
	 *
	 * @param errorCode
	 * @return
	 */
	public String getMessage(int errorCode) {
		this.errorCode = errorCode;
		return STRING;
	}

	/**
	 * @return
	 */
	public int getErrorCode() {
		return errorCode;
	}

	/**
	 * @param errorCode
	 */
	void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * @param errorMessage
	 */
	void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * This method will provide status message based on code
	 *
	 * @param code
	 * @return String
	 */
	public static String getResponseMessage(int code) {
		String value = STRING;
		ResponseCode[] responseCodes = ResponseCode.values();
		for (ResponseCode actionState : responseCodes) {
			if (actionState.getErrorCode() == code) {
				value = actionState.getErrorMessage();
			}
		}
		return value;
	}
}
