package com.tarento.frac.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.sunbird.cloud.storage.BaseStorageService;
import org.sunbird.cloud.storage.factory.StorageConfig;
import org.sunbird.cloud.storage.factory.StorageServiceFactory;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.models.StorageService;

import scala.Option;

public class CloudStore {

	public static final Logger LOGGER = LoggerFactory.getLogger(CloudStore.class);
	private static final String EXCEPTION = "Exception in %s: %s";

	@Autowired
	ApplicationProperties appProperties;

	private static BaseStorageService storageService = null;
	private static StorageService storageConfig = new StorageService();

	static {
		storageConfig = ConfigurationPanel.getStorageService();
		storageService = StorageServiceFactory.getStorageService(new StorageConfig(storageConfig.getProvider(),
				storageConfig.getIdentity(), storageConfig.getCredential(), null));

	}

	public static BaseStorageService getCloudStoreService() {
		return storageService;
	}

	public static Map<String, String> uploadFile(String folderName, File file) {
		try {
			String objectKey = "";
			if (StringUtils.isNotBlank(folderName)) {
				objectKey = folderName + "/" + DateUtils.getCurrentTimestamp() + "_" + file.getName();
			} else {
				objectKey = DateUtils.getCurrentTimestamp() + "_" + file.getName();
			}
			String url = storageService.upload(storageConfig.getContainer(), file.getAbsolutePath(), objectKey,
					Option.apply(false), Option.apply(1), Option.apply(5), Option.empty());
			Map<String, String> uploadedFile = new HashMap<>();
			uploadedFile.put(Constants.Parameters.NAME, objectKey);
			uploadedFile.put(Constants.Parameters.URL.toLowerCase(), url);
			return uploadedFile;
		} catch (Exception e) {
			LOGGER.error(String.format(EXCEPTION, "uploadFile", e.getMessage()));
			return null;
		}

	}

	public static String getSignedURL(String fileName) {
		try {
			return storageService.getSignedURL(storageConfig.getContainer(), fileName, Option.empty(), Option.empty());

		} catch (Exception e) {
			LOGGER.error(String.format(EXCEPTION, "getSignedURL", e.getMessage()));
			return null;
		}
	}

	public static Boolean deleteFile(String fileName) {
		try {
			storageService.deleteObject(storageConfig.getContainer(), fileName, Option.apply(Boolean.FALSE));
			return Boolean.TRUE;
		} catch (Exception e) {
			LOGGER.error(String.format(EXCEPTION, "deleteFile", e.getMessage()));
			return Boolean.FALSE;
		}
	}

}
