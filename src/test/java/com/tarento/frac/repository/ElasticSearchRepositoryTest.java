package com.tarento.frac.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tarento.frac.utils.ApplicationProperties;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class ElasticSearchRepositoryTest {

	@Mock
	RestHighLevelClient restHighLevelClient;

	@Mock
	ElasticSearchRepository fr;

	@InjectMocks
	ElasticSearchRepository elasticSearchRepository;

	@SpyBean
	ApplicationProperties appProperties;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void updateDataInElastic() throws IOException {
		Map<String, Object> mp = new HashMap<String, Object>();
		assertTrue(elasticSearchRepository.updateDataInElastic(mp, "124", "ASP", "doc"));
	}

	@Test
	public void writeDatatoElastic() throws IOException {
		Map<String, Object> mp = new HashMap<String, Object>();
		assertTrue(elasticSearchRepository.writeDatatoElastic(mp, "124", "ASP", "doc"));
	}

	@Test
	public void writeObjectToElastic() throws IOException {
		Map<String, Object> mp = new HashMap<String, Object>();
		assertTrue(elasticSearchRepository.writeObjectToElastic(mp, "124", "ASP", "doc"));
	}

	@Test
	public void updateObjectToElastic() throws IOException {
		Map<String, Object> mp = new HashMap<String, Object>();
		assertTrue(elasticSearchRepository.updateObjectInElastic(mp, "124", "ASP", "doc"));
	}

	@Test
	public void writeBulkRequest() throws IOException {
		List<Map<String, Object>> mp = new ArrayList<Map<String, Object>>();
		Map<String, Object> indexObj = new HashMap<>();
		indexObj.put("index", "frac-position");
		indexObj.put("type", "POSITION");
		indexObj.put("id", "PID078");
		indexObj.put("datanode", "frac");
		mp.add(indexObj);

		assertTrue(elasticSearchRepository.writeBulkRequest(mp, mp, mp));
	}

	@Test
	public void saveMyDataObject() throws IOException {
		assertFalse(elasticSearchRepository.saveMyDataObject(new Object(), "abc"));
	}

	@Test
	public void getHttpHeaders() throws IOException {
		assertEquals(new HttpHeaders().getClass(), elasticSearchRepository.getHttpHeaders().getClass());
	}

}