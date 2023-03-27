package com.tarento.frac.dao.impl;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Before;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.repository.ElasticSearchRepository;

public class CommonDaoImplTest {

	@Mock
	RestHighLevelClient restHighLevelClient;

	@Mock
	ConfigurationPanel configurationPanel;

	@Spy
	CommonDaoImpl cD;

	@InjectMocks
	CommonDaoImpl commonDao;

	@Mock
	ElasticSearchRepository elasticRepository;

	@Mock
	JdbcTemplate jdbcTemplate;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	// @Test
	// public void executeMultiSearchRequestTest() throws IOException {
	// SearchResponseSections searchResponseSections = new
	// SearchResponseSections(null, null, null, false, null, null,
	// 5);
	// SearchResponse searchResponse = new SearchResponse(searchResponseSections,
	// null, 8, 8, 0, 8,
	// new ShardSearchFailure[] {}, null);
	// Item items1 = new Item(searchResponse, new Exception());
	// Item[] items = { items1 };
	// RestHighLevelClient client = connectToElasticSearch();
	// Mockito.doReturn(client).when(elasticRepository).connectToElasticSearch();
	// MultiSearchResponse response = new MultiSearchResponse(items);
	// assertEquals(response.getClass(), commonDao.executeMultiSearchRequest(new
	// SearchRequest()).getClass());
	//
	// Mockito.doReturn(null).when(elasticRepository).connectToElasticSearch();
	// assertEquals(null, commonDao.executeMultiSearchRequest(new SearchRequest()));
	//
	// Mockito.doThrow(IOException.class).when(elasticRepository).connectToElasticSearch();
	// assertEquals(null, commonDao.executeMultiSearchRequest(new SearchRequest()));
	// }
	//
	// public RestHighLevelClient connectToElasticSearch() {
	// final CredentialsProvider credentialsProvider = new
	// BasicCredentialsProvider();
	// credentialsProvider.setCredentials(AuthScope.ANY, new
	// UsernamePasswordCredentials("elastic", "Elastic123"));
	//
	// HttpClientConfigCallback httpClientConfigCallback = (httpClientBuilder ->
	// httpClientBuilder
	// .setDefaultCredentialsProvider(credentialsProvider));
	//
	// return new RestHighLevelClient(RestClient.builder(new
	// HttpHost("es.rain.idc.tarento.com"))
	// .setHttpClientConfigCallback(httpClientConfigCallback));
	// }
	//
	// @Test
	// public void executeMultipleMultiSearchRequestTest() throws IOException {
	// SearchResponseSections searchResponseSections = new
	// SearchResponseSections(null, null, null, false, null, null,
	// 5);
	// SearchResponse searchResponse = new SearchResponse(searchResponseSections,
	// null, 8, 8, 0, 8,
	// new ShardSearchFailure[] {}, null);
	// Item items1 = new Item(searchResponse, new Exception());
	// Item[] items = { items1 };
	// RestHighLevelClient client = connectToElasticSearch();
	// Mockito.doReturn(client).when(elasticRepository).connectToElasticSearch();
	// MultiSearchResponse response = new MultiSearchResponse(items);
	// assertEquals(response.getClass(),
	// commonDao.executeMultipleMultiSearchRequest(Arrays.asList(new
	// SearchRequest())).getClass());
	// Mockito.doReturn(null).when(elasticRepository).connectToElasticSearch();
	// assertEquals(null,
	// commonDao.executeMultipleMultiSearchRequest(Arrays.asList(new
	// SearchRequest())));
	//
	// Mockito.doThrow(IOException.class).when(elasticRepository).connectToElasticSearch();
	// assertEquals(null,
	// commonDao.executeMultipleMultiSearchRequest(Arrays.asList(new
	// SearchRequest())));
	// }
	//
	// @Test
	// public void executeDeleteRequestTest() throws IOException {
	// // Success
	// DeleteResponse response = new DeleteResponse();
	// Failure f = new Failure(null, "", null, null, true);
	// ShardInfo shardInfo = new ShardInfo(1, 1, f);
	// response.setShardInfo(shardInfo);
	// RestHighLevelClient client = connectToElasticSearch();
	// Mockito.doReturn(client).when(elasticRepository).connectToElasticSearch();
	// assertEquals(true, commonDao.executeDeleteRequest(new
	// DeleteRequest("frac-positions", "doc", "PID001")));
	//
	// // Exception
	// assertEquals(false, commonDao.executeDeleteRequest(new DeleteRequest()));
	// // Failure response
	// Mockito.doReturn(null).when(elasticRepository).connectToElasticSearch();
	// assertEquals(false, commonDao.executeDeleteRequest(new
	// DeleteRequest("frac-positions", "doc", "PID001")));
	// }
	//
	// @Test
	// public void loadAllDataNodesTest() throws Exception {
	// DataNode dataNode1 = new DataNode();
	// dataNode1.setType(Entities.POSITION.name());
	// dataNode1.setId("PID001");
	//
	// DataNode dataNode2 = new DataNode();
	// dataNode2.setType(Entities.ROLE.name());
	// dataNode2.setId("RID001");
	//
	// DataNode dataNode3 = new DataNode();
	// dataNode3.setType(Entities.COMPETENCY.name());
	// dataNode3.setId("CID001");
	//
	// DataNode dataNode4 = new DataNode();
	// dataNode4.setType(Entities.ACTIVITY.name());
	// dataNode4.setId("AID001");
	//
	// DataNode dataNode5 = new DataNode();
	// dataNode5.setType(Entities.KNOWLEDGERESOURCE.name());
	// dataNode5.setId("KRID001");
	//
	// DataNode dataNode6 = new DataNode();
	// dataNode6.setType(Entities.COMPETENCYAREA.name());
	// dataNode6.setId("CAID001");
	//
	// DataNode dataNode7 = new DataNode();
	// dataNode7.setType(Entities.COMPETENCIESLEVEL.name());
	// dataNode7.setId("CLID001");
	//
	// List<Object> queryParams = new ArrayList<>();
	// queryParams.add("POSITION");
	// List<Map<String, Object>> result = new ArrayList<>();
	// List<DataNode> d = new ArrayList<>(
	// Arrays.asList(dataNode1, dataNode2, dataNode3, dataNode4, dataNode5,
	// dataNode6, dataNode7));
	// Map<String, Object> m = new HashMap<>();
	// m.put("prop_key", "URL");
	// m.put("prop_value", "www");
	// m.put("id", "abc");
	// result.add(m);
	// Mockito.doReturn(result).when(jdbcTemplate).queryForList(
	// Sql.DataNode.GET_ALL_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE +
	// Sql.DataNode.ACTIVE_CONDITION
	// + Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION,
	// queryParams.toArray(new Object[queryParams.size()]));
	// commonDao.loadAllDataNodes("");
	//
	// PowerMockito.when(cD, "nodeAndPropertyMappings", result).thenReturn(d);
	// commonDao.loadAllDataNodes("POSITION");
	// }
	//
	// @Test
	// public void loadAllDataNodesTestOne() throws Exception {
	// DataNode dataNode1 = new DataNode();
	// dataNode1.setType(Entities.POSITION.name());
	// dataNode1.setId("PID001");
	//
	// DataNode dataNode2 = new DataNode();
	// dataNode2.setType(Entities.ROLE.name());
	// dataNode2.setId("RID001");
	//
	// DataNode dataNode3 = new DataNode();
	// dataNode3.setType(Entities.COMPETENCY.name());
	// dataNode3.setId("CID001");
	//
	// DataNode dataNode4 = new DataNode();
	// dataNode4.setType(Entities.ACTIVITY.name());
	// dataNode4.setId("AID001");
	//
	// DataNode dataNode5 = new DataNode();
	// dataNode5.setType(Entities.KNOWLEDGERESOURCE.name());
	// dataNode5.setId("KRID001");
	//
	// DataNode dataNode6 = new DataNode();
	// dataNode6.setType(Entities.COMPETENCYAREA.name());
	// dataNode6.setId("CAID001");
	//
	// DataNode dataNode7 = new DataNode();
	// dataNode7.setType(Entities.COMPETENCIESLEVEL.name());
	// dataNode7.setId("CLID001");
	//
	// List<Object> queryParams = new ArrayList<>();
	// queryParams.add("POSITION");
	// List<Map<String, Object>> result = new ArrayList<>();
	// List<DataNode> d = new ArrayList<>(
	// Arrays.asList(dataNode1, dataNode2, dataNode3, dataNode4, dataNode5,
	// dataNode6, dataNode7));
	// Map<String, Object> m = new HashMap<>();
	// m.put("prop_key", "URL");
	// m.put("prop_value", "www");
	// m.put("id", "abc");
	//
	// Map<String, Object> n = new HashMap<>();
	// n.put("prop_key", "NAS");
	// n.put("prop_value", "www");
	// n.put("id", "abc");
	//
	// result.add(m);
	// result.add(n);
	// Mockito.doReturn(result).when(jdbcTemplate).queryForList(
	// Sql.DataNode.GET_ALL_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE +
	// Sql.DataNode.ACTIVE_CONDITION
	// + Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION,
	// queryParams.toArray(new Object[queryParams.size()]));
	// commonDao.loadAllDataNodes("");
	//
	// PowerMockito.when(cD, "nodeAndPropertyMappings", result).thenReturn(d);
	// commonDao.loadAllDataNodes("POSITION");
	// }

}