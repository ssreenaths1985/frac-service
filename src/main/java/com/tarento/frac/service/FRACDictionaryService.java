package com.tarento.frac.service;

import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.UserInfo;

public interface FRACDictionaryService {

	public boolean initiateDictionaryPush(DataNode dataNode, UserInfo userInfo);

	/**
	 * Reloads the dictionary document in the elasticsearch index
	 * 
	 * @return Boolean
	 */
	public Boolean reloadDictionary();

}
