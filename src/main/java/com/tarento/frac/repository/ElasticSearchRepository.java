package com.tarento.frac.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.tomcat.util.codec.binary.Base64;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;

/**
 * This Repository Class is used to perform the transactions of storing the data
 * into the Elastic Search Repository
 * 
 * @author Darshan Nagesh
 *
 */
@Service
public class ElasticSearchRepository {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Autowired
	ApplicationProperties appProperties;

	public static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchRepository.class);

	/**
	 * Based on the Transaction Index Data Obtained and the URL with Headers, this
	 * method will put the Data obtained on the Elastic Search Database and returns
	 * the response in the form of Positive or Negative outcome (True Or False)
	 * 
	 * @param transactionIndex
	 * @param url
	 * @param headers
	 * @return
	 */
	public Boolean updateDataInElastic(Map<String, Object> jsonMap, String id, String esIndex, String docType)
			throws IOException {
		RestHighLevelClient rc = connectToElasticSearch();
		try {
			UpdateRequest updateRequest = new UpdateRequest(esIndex, docType, id).doc(jsonMap);
			rc.update(updateRequest);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			rc.close();
		}
		return Boolean.TRUE;
	}

	public Boolean writeDatatoElastic(Map<String, Object> jsonMap, String id, String esIndex, String docType)
			throws IOException {
		RestHighLevelClient rc = connectToElasticSearch();
		try {
			IndexRequest indexRequest = new IndexRequest(esIndex, docType, id).source(jsonMap);
			rc.index(indexRequest);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			rc.close();
		}
		return Boolean.TRUE;
	}

	public Boolean writeObjectToElastic(Object dataObject, String id, String esIndex, String docType)
			throws IOException {
		RestHighLevelClient rc = connectToElasticSearch();
		try {
			IndexRequest indexRequest = new IndexRequest(esIndex, docType, id).source(new Gson().toJson(dataObject),
					XContentType.JSON);
			rc.index(indexRequest);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			rc.close();
		}
		return Boolean.TRUE;
	}

	public Boolean updateObjectInElastic(Object dataObject, String id, String esIndex, String docType)
			throws IOException {
		RestHighLevelClient rc = connectToElasticSearch();
		try {
			UpdateRequest updateRequest = new UpdateRequest(esIndex, docType, id).doc(new Gson().toJson(dataObject),
					XContentType.JSON);
			rc.update(updateRequest);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			rc.close();
		}
		return Boolean.TRUE;
	}

	public Boolean writeBulkRequest(List<Map<String, Object>> toIndex, List<Map<String, Object>> toUpdate,
			List<Map<String, Object>> toDelete) throws IOException {
		RestHighLevelClient rc = connectToElasticSearch();
		try {
			BulkRequest bulkRequest = new BulkRequest();
			for (Map<String, Object> indexObj : toIndex) {
				bulkRequest.add(new IndexRequest(indexObj.get(Constants.Parameters.INDEX).toString(),
						indexObj.get(Constants.Parameters.TYPE).toString(),
						indexObj.get(Constants.Parameters.ID).toString()).source(
								new Gson().toJson(indexObj.get(Constants.Parameters.DATA_NODE)), XContentType.JSON));
			}
			for (Map<String, Object> updateObj : toUpdate) {
				bulkRequest.add(new UpdateRequest(updateObj.get(Constants.Parameters.INDEX).toString(),
						updateObj.get(Constants.Parameters.TYPE).toString(),
						updateObj.get(Constants.Parameters.ID).toString()).doc(
								new Gson().toJson(updateObj.get(Constants.Parameters.DATA_NODE)), XContentType.JSON));
			}
			for (Map<String, Object> deleteObj : toDelete) {
				bulkRequest.add(new DeleteRequest(deleteObj.get(Constants.Parameters.INDEX).toString(),
						deleteObj.get(Constants.Parameters.TYPE).toString(),
						deleteObj.get(Constants.Parameters.ID).toString()));
			}
			rc.bulk(bulkRequest);
		} catch (Exception e) {
			LOGGER.error(String.format("Exception in writeBulkRequest: %s", e.getMessage()));
		} finally {
			rc.close();
		}
		return Boolean.TRUE;
	}

	public RestHighLevelClient connectToElasticSearch() {
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(appProperties.getEsUsername(), appProperties.getEsPassword()));

		HttpClientConfigCallback httpClientConfigCallback = (httpClientBuilder -> httpClientBuilder
				.setDefaultCredentialsProvider(credentialsProvider));

		return new RestHighLevelClient(RestClient
				.builder(new HttpHost(appProperties.getElasticSearchHost(), appProperties.getElasticSearchPort())));
	}

	public Boolean saveMyDataObject(Object object, String url) {
		RestTemplate restTemplate = new RestTemplate();
		try {
			if (url != null && object != null) {
				restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(object, getHttpHeaders()), Map.class);
				return Boolean.TRUE;
			}
		} catch (Exception e) {
			LOGGER.error(String.format("Exception in saveMyDataObject : %s", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	HttpHeaders getHttpHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(Constants.Parameters.AUTHORIZATION,
				getBase64Value(appProperties.getEsUsername(), appProperties.getEsPassword()));
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setContentType(MediaType.APPLICATION_JSON);
		return headers;
	}

	String getBase64Value(String userName, String password) {
		String authString = String.format("%s:%s", userName, password);
		byte[] encodedAuthString = Base64.encodeBase64(authString.getBytes(StandardCharsets.US_ASCII));
		return String.format("Basic %s", new String(encodedAuthString));
	}

}