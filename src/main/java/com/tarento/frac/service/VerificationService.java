package com.tarento.frac.service;

import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.UserInfo;

public interface VerificationService {

	Boolean verifyDataNode(DataNodeVerification verification, UserInfo userInfo);

	public Boolean verifyAllDataNode(DataNodeVerification verification, UserInfo userInfo);

}
