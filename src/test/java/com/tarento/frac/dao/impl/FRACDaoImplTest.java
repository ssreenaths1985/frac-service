package com.tarento.frac.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchResponseSections;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.CommonDao;
import com.tarento.frac.models.Association;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.ExploreNodesMapper;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.FilterMappings;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeFeedback;
import com.tarento.frac.models.NodeFilter;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.SearchBox;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.repository.ElasticSearchRepository;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.DateUtils;
import com.tarento.frac.utils.Sql;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class FRACDaoImplTest {

	@Mock
	RestHighLevelClient restHighLevelClient;

	@Mock
	ConfigurationPanel configurationPanel;

	@Mock
	RestTemplate restTemplate;

	@Mock
	CommonDao commonDao;

	@Mock
	FRACDaoImpl fr;

	@Spy
	@InjectMocks
	FRACDaoImpl fracDao;

	@SpyBean
	ApplicationProperties appProperties;

	@Mock
	ElasticSearchRepository elasticRepository;

	@Mock
	JdbcTemplate jdbcTemplate;

	private static UserInfo userInfo;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		userInfo = new UserInfo("f:923bdc18-520d-48d4-a84e-3cde1e655ebd:5574b3c5-16ca-49d8-8059-705304f2c7fb",
				"igot demo1", "igotdemo1", "igot", "demo1", "", null, "");
	}

	@Test
	public void addDataNodeTest() {
		DataNode dataNode = new DataNode();
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("department", "ISTM");
		dataNode.setAdditionalProperties(additionalProperties);

		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.ADD_DATA_NODE,
				new Object[] { dataNode.getId(), dataNode.getType(), dataNode.getName(), dataNode.getDescription(),
						dataNode.getStatus(), dataNode.getSource(), dataNode.getLevel(),
						DateUtils.getCurrentDateTimeInUTC(), userInfo.getSub() });

		List<Object[]> property = new ArrayList<>();
		Object[] property1 = { dataNode.getId(), "department", "ISTM" };
		property.add(property1);
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.ADD_NODE_PROPERTY, property);
		assertEquals(true, fracDao.addDataNode(dataNode));

		dataNode.setAdditionalProperties(null);
		assertEquals(true, fracDao.addDataNode(dataNode));

		// Mockito.doThrow(NullPointerException.class).when(jdbcTemplate).update(Sql.DataNode.ADD_DATA_NODE,
		// new Object[] { dataNode.getId(), dataNode.getType(), dataNode.getName(),
		// dataNode.getDescription(),
		// dataNode.getStatus(), dataNode.getSource(), dataNode.getLevel(),
		// DateUtils.getCurrentDateTimeInUTC(), userInfo.getSub() });
		// assertEquals(false, fracDao.addDataNode(dataNode));
	}

	@Test
	public void updateDataNodeTest() {
		DataNode dataNode = new DataNode();
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("department", "ISTM");
		dataNode.setAdditionalProperties(additionalProperties);

		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.UPDATE_DATA_NODE,
				new Object[] { dataNode.getName(), dataNode.getDescription(), dataNode.getStatus(),
						dataNode.getSource(), dataNode.getLevel(), DateUtils.getCurrentDateTimeInUTC(),
						userInfo.getSub(), dataNode.getId() });
		List<Object[]> property = new ArrayList<>();
		Object[] property1 = { dataNode.getId(), "department", "ISTM" };
		property.add(property1);
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.ADD_NODE_PROPERTY, property);
		assertEquals(true, fracDao.updateDataNode(dataNode));

		dataNode.setAdditionalProperties(null);
		assertEquals(true, fracDao.updateDataNode(dataNode));

		Mockito.when(jdbcTemplate).thenThrow(NullPointerException.class);
		assertEquals(false, fracDao.updateDataNode(dataNode));
	}

	@Test
	public void addDataNodeBulkTest() {
		List<DataNode> dataNodes = new ArrayList<>();
		DataNode dataNode1 = new DataNode();
		dataNodes.add(dataNode1);
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("department", "ISTM");
		dataNode1.setAdditionalProperties(additionalProperties);

		List<Object[]> nodes = new ArrayList<>();
		List<Object[]> property = new ArrayList<>();
		nodes.add(new Object[] { dataNode1.getId(), dataNode1.getType(), dataNode1.getName(),
				dataNode1.getDescription(), dataNode1.getStatus(), dataNode1.getSource(), dataNode1.getLevel(),
				DateUtils.getCurrentDateTimeInUTC(), userInfo.getSub() });
		property.add(new Object[] { dataNode1.getId(), "department", "ISTM" });
		int[] batchResult = { 1 };

		Mockito.doReturn(batchResult).when(jdbcTemplate).batchUpdate(Sql.DataNode.ADD_DATA_NODE, nodes);
		Mockito.doReturn(batchResult).when(jdbcTemplate).batchUpdate(Sql.DataNode.ADD_NODE_PROPERTY, property);

		assertEquals(true, fracDao.addDataNodeBulk(dataNodes));
		dataNode1.setAdditionalProperties(null);
		assertEquals(true, fracDao.addDataNodeBulk(dataNodes));

		Mockito.when(jdbcTemplate).thenThrow(NullPointerException.class);
		assertEquals(false, fracDao.addDataNodeBulk(dataNodes));
	}

	@Test
	public void updateDataNodeBulkTest() {
		List<DataNode> dataNodes = new ArrayList<>();
		DataNode dataNode1 = new DataNode();
		dataNodes.add(dataNode1);
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("department", "ISTM");
		dataNode1.setAdditionalProperties(additionalProperties);

		List<Object[]> nodes = new ArrayList<>();
		List<Object[]> property = new ArrayList<>();
		nodes.add(new Object[] { dataNode1.getName(), dataNode1.getDescription(), dataNode1.getStatus(),
				dataNode1.getSource(), dataNode1.getLevel(), DateUtils.getCurrentDateTimeInUTC(), userInfo.getSub(),
				dataNode1.getId() });
		property.add(new Object[] { "ISTM", dataNode1.getId(), "department" });
		int[] batchResult = { 1 };

		Mockito.doReturn(batchResult).when(jdbcTemplate).batchUpdate(Sql.DataNode.UPDATE_DATA_NODE, nodes);
		Mockito.doReturn(batchResult).when(jdbcTemplate).batchUpdate(Sql.DataNode.ADD_NODE_PROPERTY, property);

		assertEquals(true, fracDao.updateDataNodeBulk(dataNodes));
		dataNode1.setAdditionalProperties(null);
		assertEquals(true, fracDao.updateDataNodeBulk(dataNodes));

		Mockito.when(jdbcTemplate).thenThrow(NullPointerException.class);
		assertEquals(false, fracDao.updateDataNodeBulk(dataNodes));
	}

	@Test
	public void setnodeMapping() {

		NodeMapping nodeMapping = new NodeMapping(null, "POSITION", "PID001", "ROLE", "RID001", Arrays.asList("RID001"),
				NodeStatus.UNVERIFIED.name());
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.DELETE_MAPPINGS,
				new Object[] { nodeMapping.getParentId(), nodeMapping.getChild() });

		List<Object[]> mappings = new ArrayList<>();
		mappings.add(new Object[] { nodeMapping.getParent(), nodeMapping.getParentId(), nodeMapping.getChild(),
				nodeMapping.getChildId() });
		Mockito.doReturn(new int[] { 1 }).when(jdbcTemplate).batchUpdate(Sql.DataNode.ADD_MAPPINGS, mappings);
		assertEquals(true, fracDao.setnodeMapping(nodeMapping));

		Mockito.doThrow(NullPointerException.class).when(jdbcTemplate).update(Sql.DataNode.DELETE_MAPPINGS,
				new Object[] { nodeMapping.getParentId(), nodeMapping.getChild() });
		assertEquals(false, fracDao.setnodeMapping(nodeMapping));

	}

	// @Test
	@SuppressWarnings("static-access")
	public void getNodeById() throws IOException {
		List<DataNode> dataNodes = new ArrayList<>();
		DataNode similarNode = new DataNode();
		similarNode.setId("RID036");
		dataNodes.add(similarNode);

		DataNode dataNode = new DataNode();
		dataNode.setId("PID001");
		dataNode.setType(Entities.POSITION.name());
		dataNode.setName("Manager");
		List<DataNode> node = new ArrayList<>(Arrays.asList(dataNode));
		Mockito.doReturn(node).when(jdbcTemplate).query(
				Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE + Sql.DataNode.ID_CONDITION,
				new Object[] { dataNode.getId() }, configurationPanel.rowMapDataNode);
		Map<String, NodeMapping> nodeMap = new HashMap<>();
		NodeMapping nodeMapping1 = new NodeMapping();
		nodeMapping1.setChild(Entities.ROLE.name());
		List<String> childIds = new ArrayList<>();
		childIds.add("RID001");
		nodeMapping1.setChildIds(childIds);
		nodeMap.put("PID001-ROLE", nodeMapping1);
		nodeMap.put("PID001-ROLES", nodeMapping1);
		Mockito.when(configurationPanel.getNodeMapping()).thenReturn(nodeMap);
		Mockito.doReturn(new ArrayList<>()).when(jdbcTemplate).query(Sql.Bookmark.GET_BOOKMARK,
				new Object[] { userInfo.getSub() }, configurationPanel.rowMapDataNode);
		assertEquals(DataNode.class, fracDao.getNodeById(dataNode.getId(), true, true, true, "userId").getClass());
	}

	@Test
	public void getParentNodesTest() throws IOException {
		DataNode d = new DataNode();
		d.setId("CJJK");
		List<DataNode> dn = new ArrayList<>();
		dn.add(d);
		Mockito.doReturn(dn).when(jdbcTemplate).query(Sql.DataNode.GET_ALL_PARENT_NODE_BY_TYPE,
				new Object[] { "CID001", "ISTM" }, configurationPanel.rowMapDataNode);
		assertEquals(1, fracDao.getParentNodes("CID001", "ISTM").size());
	}

	// @Test
	public void getCountOfNodes() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(jdbcTemplate).query(Sql.DataNode.GET_NODE_ID_STATUS_FROM_DEPARTMENT,
				new Object[] { "ISTM" }, configurationPanel.rowMapDataNode);

		// assertEquals(0, fracDao.getCountOfNodes(null, "ISTM", null));

		DataNode dataNode1 = new DataNode();
		dataNode1.setStatus(NodeStatus.UNVERIFIED.name());
		DataNode dataNode2 = new DataNode();
		Mockito.doReturn(Arrays.asList(dataNode1, dataNode2)).when(jdbcTemplate).query(
				Sql.DataNode.GET_NODE_ID_STATUS_FROM_DEPARTMENT, new Object[] { "ISTM" },
				configurationPanel.rowMapDataNode);
		// assertEquals(0, fracDao.getCountOfNodes(null, "ISTM",
		// NodeStatus.UNVERIFIED.name()));
	}

	@Test
	public void searchNodes() throws IOException {
		List<SearchBox> searches = new ArrayList<>();
		SearchBox e = new SearchBox("status", "POSITION", "POSITION");
		searches.add(e);
		SearchBox e1 = new SearchBox("name", "POSITION", "POSITION");
		List<SearchBox> searches1 = new ArrayList<>();
		searches1.add(e1);
		MultiSearch multiSearch = new MultiSearch(searches, null, false, null, "userType", null, null);
		MultiSearch multiSearch1 = new MultiSearch(searches, null, null, null, "", "FRAC", null);
		assertEquals(0, fracDao.searchNodes(multiSearch).size());
		assertEquals(0, fracDao.searchNodes(multiSearch1).size());
	}

	// @Test
	@SuppressWarnings("static-access")
	public void searchNodesTestOne() throws CloneNotSupportedException {
		List<SearchBox> searches = new ArrayList<>();
		SearchBox searchBox = new SearchBox("name", "Manager", "POSITION");
		searches.add(searchBox);
		MultiSearch multisearch = new MultiSearch(searches, null, Boolean.TRUE, null, "", "", null);

		List<DataNode> dataNodes = new ArrayList<>();
		DataNode dataNode1 = new DataNode();
		dataNode1.setId("PID001");
		DataNode dataNode2 = new DataNode();
		dataNode2.setId("PID002");
		dataNodes.add(dataNode1);
		dataNodes.add(dataNode2);
		Mockito.when(configurationPanel.searchKeyword(Matchers.anyString(), Matchers.anyString(), Matchers.anyString()))
				.thenReturn(dataNodes);

		Map<String, NodeMapping> nodeMap = new HashMap<>();
		NodeMapping nodeMapping1 = new NodeMapping();
		nodeMapping1.setChild(Entities.ROLE.name());
		List<String> childIds = new ArrayList<>();
		childIds.add("RID001");
		nodeMapping1.setChildIds(childIds);
		nodeMap.put("PID001-ROLE", nodeMapping1);
		nodeMap.put("PID001-ROLES", nodeMapping1);
		Mockito.doReturn(nodeMap).when(configurationPanel).getNodeMapping();

		assertEquals(false, fracDao.searchNodes(multisearch).isEmpty());
	}

	@Test
	public void getBookmarkTest() {
		Mockito.doReturn(new ArrayList<>()).when(jdbcTemplate).query(Sql.Bookmark.GET_BOOKMARK,
				new Object[] { userInfo.getSub() }, configurationPanel.rowMapDataNode);

		assertEquals(0, fracDao.getBookmark(userInfo.getSub(), null).size());
		Mockito.doReturn(new ArrayList<>()).when(jdbcTemplate).query(
				Sql.Bookmark.GET_BOOKMARK + Sql.Common.AND_CLAUSE + Sql.DataNode.TYPE_CONDITION,
				new Object[] { userInfo.getSub(), Entities.KNOWLEDGERESOURCE.name() },
				configurationPanel.rowMapDataNode);

		assertEquals(0, fracDao.getBookmark(userInfo.getSub(), Entities.KNOWLEDGERESOURCE.name()).size());

		Mockito.when(jdbcTemplate).thenThrow(NullPointerException.class);
		assertEquals(new ArrayList<>(), fracDao.getBookmark(userInfo.getSub(), Entities.KNOWLEDGERESOURCE.name()));
	}

	@Test
	public void bookmarkDataNodeTest() {
		DataNode bookmarkNode = new DataNode();
		bookmarkNode.setId("KRID001");
		bookmarkNode.setType(Entities.KNOWLEDGERESOURCE.name());
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.Bookmark.ADD_BOOKMARK,
				new Object[] { bookmarkNode.getId(), bookmarkNode.getType(), userInfo.getSub() });
		assertEquals(true, fracDao.bookmarkDataNode(bookmarkNode, userInfo.getSub()));

		bookmarkNode.setBookmark(Boolean.TRUE);
		assertEquals(true, fracDao.bookmarkDataNode(bookmarkNode, userInfo.getSub()));

		bookmarkNode.setBookmark(Boolean.FALSE);
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.Bookmark.DELETE_BOOKMARK,
				new Object[] { bookmarkNode.getId(), userInfo.getSub() });
		assertEquals(true, fracDao.bookmarkDataNode(bookmarkNode, userInfo.getSub()));

		Mockito.when(jdbcTemplate).thenThrow(NullPointerException.class);
		assertEquals(false, fracDao.bookmarkDataNode(bookmarkNode, userInfo.getSub()));
	}

	@SuppressWarnings("unchecked")
	// @Test
	public void filterDataNodesTest() {
		FilterList filterList = new FilterList();
		filterList.setType(Entities.COMPETENCY.name());
		filterList.setDepartment("ISTM");
		NodeFilter nodeFilter1 = new NodeFilter();
		nodeFilter1.setField("cod");
		nodeFilter1.setValues(Arrays.asList("ISTM", "NACIN"));
		filterList.setFilters(Arrays.asList(nodeFilter1));

		Map<String, Object> response = new HashMap<>();
		response.put(Constants.Parameters.ID, "CID001");
		response.put(Constants.SqlParams.PROP_KEY, "cod");
		response.put(Constants.SqlParams.PROP_VALUE, "ISTM");
		Map<String, Object> response1 = new HashMap<>();
		response1.put(Constants.Parameters.ID, "CID001");
		response1.put(Constants.SqlParams.PROP_KEY, "cod");
		response1.put(Constants.SqlParams.PROP_VALUE, "NACIN");

		Mockito.doReturn(Arrays.asList(response, response1)).when(jdbcTemplate).queryForList(
				Sql.DataNode.GET_NODE_AND_PROPERTY + Sql.Common.WHERE_CLAUSE + Sql.DataNode.FILTER_BY_USERS_DRAFT
						+ " AND department = ?  AND ((prop_key = ?  AND prop_value = ? ) OR (prop_key = ?  AND prop_value = ? ))",
				new Object[] { userInfo.getSub(), "cod", "ISTM", "cod", "NACIN" });
		assertEquals(1, fracDao.filterDataNodes(filterList, userInfo.getSub()).size());

		Mockito.when(jdbcTemplate).thenThrow(NullPointerException.class);
		assertEquals(null, fracDao.filterDataNodes(filterList, userInfo.getSub()));

	}

	@Test
	public void getIdQueryTest() throws IOException {
		List<String> idList = new ArrayList<>(Arrays.asList("PID011", "PID012"));
		assertEquals("(\'PID011\', \'PID012\')", fracDao.getIdQuery(idList));

	}

	@Test
	public void getFilterLabelsTest() {
		assertEquals(Constants.Parameters.COMPETENCIES_AREA, fracDao.getFilterLabel("area"));
		assertEquals(Constants.Parameters.COMPETENCY_TYPE, fracDao.getFilterLabel("type"));
		assertEquals(Constants.Parameters.COD, fracDao.getFilterLabel(Constants.Parameters.COD));
		assertEquals(Constants.Parameters.COMPETENCYSECTOR,
				fracDao.getFilterLabel(Constants.Parameters.COMPETENCYSECTOR));
		assertEquals(Constants.Parameters.STATUS, fracDao.getFilterLabel(Constants.Parameters.STATUS));
		assertEquals("abcd", "abcd");
	}

	@Test
	public void getAllNodesOfTest() throws IOException {
		List<String> allIds = new ArrayList<>();
		List<DataNode> dataNodes = new ArrayList<>();
		DataNode dn = new DataNode();
		dn.setId("PID001");
		dataNodes.add(dn);
		Mockito.doReturn(dataNodes).when(jdbcTemplate).query(Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE,
				new Object[] {}, configurationPanel.rowMapDataNode);
		Mockito.doReturn(dataNodes).when(jdbcTemplate).query(
				Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE + " name like '%" + "john" + "%' ", new Object[] {},
				configurationPanel.rowMapDataNode);
		Mockito.doReturn(dataNodes).when(jdbcTemplate).query(
				Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE + Sql.DataNode.DEPARTMENT_CONDITION,
				new Object[] { "frac" }, configurationPanel.rowMapDataNode);
		assertEquals("PID001", fracDao.getAllNodesOf(allIds, "", "").get("PID001").getId());
		assertEquals("PID001", fracDao.getAllNodesOf(allIds, "john", "").get("PID001").getId());
		assertEquals("PID001", fracDao.getAllNodesOf(allIds, "", "frac").get("PID001").getId());

		allIds.add("PID001");

		Mockito.doReturn(dataNodes).when(jdbcTemplate).query(
				Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE + " id in " + "(\'PID001\')", new Object[] {},
				configurationPanel.rowMapDataNode);

		assertEquals("PID001", fracDao.getAllNodesOf(allIds, "", "").get("PID001").getId());

		Mockito.doReturn(dataNodes).when(jdbcTemplate).query(
				Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE + " id in " + "(\'PID001\')" + Sql.Common.AND_CLAUSE
						+ " name like '%" + "john" + "%' " + Sql.Common.AND_CLAUSE + Sql.DataNode.DEPARTMENT_CONDITION,
				new Object[] { "frac" }, configurationPanel.rowMapDataNode);

		Mockito.doReturn(dataNodes).when(jdbcTemplate)
				.query(Sql.DataNode.GET_NODE + Sql.Common.WHERE_CLAUSE + " id in " + "(\'PID001\')"
						+ Sql.Common.AND_CLAUSE + " name like '%" + "john" + "%' ", new Object[] {},
						configurationPanel.rowMapDataNode);

		assertEquals("PID001", fracDao.getAllNodesOf(allIds, "john", "frac").get("PID001").getId());

		assertEquals("PID001", fracDao.getAllNodesOf(allIds, "john", "All departments").get("PID001").getId());
	}

	@Test
	public void exploreAllNodesV3Test() throws IOException {
		NodeMapping nm = new NodeMapping(null, "parent", "PID001", "child", "CID001", null, "VERIFIED");
		List<NodeMapping> nodeMap = new ArrayList<>(Arrays.asList(nm));
		final BeanPropertyRowMapper<NodeMapping> rowMapNodeMapping = new BeanPropertyRowMapper<>(NodeMapping.class);
		Mockito.doReturn(nodeMap).when(jdbcTemplate).query(
				"select parent_id, parent, child_id, child from node_mapping where status = 'verified'",
				new Object[] {}, rowMapNodeMapping);
		DataNode dn = new DataNode();
		dn.setId("PID001");
		DataNode dn1 = new DataNode();
		dn1.setId("CID001");
		Map<String, DataNode> mp = new HashMap<>();
		mp.put("PID001", dn);
		mp.put("CID001", dn1);

		Mockito.doReturn(mp).when(fracDao).getAllNodesOf(Matchers.anyListOf(String.class), Matchers.anyString(),
				Matchers.anyString());
		assertEquals(new Overview().getClass(), fracDao.exploreAllNodes("POSITION", "frac").getClass());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void exploreSearchTest() throws IOException {
		List<NodeMapping> nodeMap = new ArrayList<>();
		NodeMapping n = new NodeMapping(null, "parent", "PID011", "child", "PID012", new ArrayList<>(), "verified");
		NodeMapping n1 = new NodeMapping(null, "parent", "PID012", "child", "PID013", new ArrayList<>(), "verified");
		NodeMapping n2 = new NodeMapping(null, "parent", "PID012", "child", "PID014", new ArrayList<>(), "verified");
		NodeMapping n7 = new NodeMapping(null, "parent", "PID012", "POSITION", "PID014", new ArrayList<>(), "verified");
		NodeMapping n3 = new NodeMapping(null, "parent", "PID012", Entities.ROLE.name(), "PID015", new ArrayList<>(),
				"verified");
		NodeMapping n4 = new NodeMapping(null, "parent", "PID012", Entities.COMPETENCY.name(), "PID016",
				new ArrayList<>(), "verified");
		NodeMapping n5 = new NodeMapping(null, "parent", "PID012", Entities.ACTIVITY.name(), "PID017",
				new ArrayList<>(), "verified");
		NodeMapping n6 = new NodeMapping(null, "parent", "PID012", Entities.KNOWLEDGERESOURCE.name(), "PID018",
				new ArrayList<>(), "verified");
		nodeMap.add(n);
		nodeMap.add(n1);
		nodeMap.add(n2);
		nodeMap.add(n3);
		nodeMap.add(n4);
		nodeMap.add(n5);
		nodeMap.add(n6);
		nodeMap.add(n7);

		Mockito.doReturn(nodeMap).when(jdbcTemplate).query(
				"select parent_id as parentId, parent, child_id as childId, child from node_mapping where status = 'verified'",
				new Object[] {}, configurationPanel.rowMapNodeMapping);
		Map<String, DataNode> nodes = new HashMap<>();
		DataNode dn = new DataNode();
		dn.setType("POSITION");
		nodes.put("PID017", dn);

		DataNode dn1 = new DataNode();
		dn1.setType(Entities.ROLE.name());
		nodes.put("PID012", dn1);

		DataNode dn2 = new DataNode();
		dn2.setType(Entities.COMPETENCY.name());
		nodes.put("PID013", dn2);

		DataNode dn3 = new DataNode();
		dn3.setId("PID011");
		dn3.setType(Entities.ACTIVITY.name());
		nodes.put("PID011", dn3);

		DataNode dn4 = new DataNode();
		dn4.setType(Entities.KNOWLEDGERESOURCE.name());
		nodes.put("PID015", dn4);

		Mockito.doReturn(nodes).when(fracDao).getAllNodesOf(Matchers.anyListOf(String.class), Matchers.anyString(),
				Matchers.anyString());

		Mockito.doNothing().when(fr).collectEdgesAndVertices(Matchers.anyObject(), Matchers.anyListOf(DataNode.class),
				Matchers.anyMap(), Matchers.anyListOf(String.class));

		Mockito.doNothing().when(fr).resolveAssociations(Matchers.anyMap(), Matchers.anyListOf(String.class),
				Matchers.anyListOf(Association.class));

		assertEquals(false, fracDao.exploreSearch("dev", "iGOT").getDataNodes().isEmpty());
	}

	@Test
	public void nodeFeedbackTest() throws IOException {
		Mockito.doReturn(true).when(elasticRepository).writeObjectToElastic(Matchers.anyObject(), Matchers.anyString(),
				Matchers.anyString(), Matchers.anyString());
		BytesReference source = new BytesArray("{\"userId\":\"PID083\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());

		Extensions extension = new Extensions();
		extension.setId("PID013");
		extension.setType("COMPETENCIESLEVEL");
		extension.setUserId("PID083");
		NodeFeedback feedback = new NodeFeedback(5.0, "abc", "5-06-2010", "Avam");
		List<NodeFeedback> nodeComments = new ArrayList<>();
		nodeComments.add(feedback);
		extension.setFeedbacks(nodeComments);
		extension.setFeedback(feedback);
		assertEquals(true, fracDao.nodeFeedback(extension, "Avam"));
	}

	@Test
	public void addChildNodeToRespectiveList() {
		ExploreNodesMapper mapper = new ExploreNodesMapper();
		DataNode newNode = new DataNode();
		newNode.setId("abc");
		fracDao.addChildNodeToRespectiveList(mapper, newNode, "POSITION");
		fracDao.addChildNodeToRespectiveList(mapper, newNode, "ROLE");
		fracDao.addChildNodeToRespectiveList(mapper, newNode, "ACTIVITY");
		fracDao.addChildNodeToRespectiveList(mapper, newNode, "COMPETENCY");
		fracDao.addChildNodeToRespectiveList(mapper, newNode, "KNOWLEDGERESOURCE");
		assertTrue(true);
	}

	@Test
	public void getNodeFeedbackTest() throws IOException {
		BytesReference source = new BytesArray(
				"{\"feedbacks\":[{\"rating\":4,\"comments\":\"Feedback\",\"user\":\"Sakthivel Govindan\",\"updatedDate\":\"2021-03-01\"}]}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(false, fracDao.getNodeFeedback("COMPETENCIESLEVEL", "PID013", "PID083").isEmpty());
	}

	@Test
	public void getNodeFeedbackTestOne() throws IOException {
		BytesReference source = new BytesArray(
				"{\"feedbacks\":[{\"rating\":4,\"comments\":\"Feedback\",\"user\":\"Sakthivel Govindan\",\"updatedDate\":\"2021-03-01\"}]}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(false, fracDao.getNodeFeedback("COMPETENCIESLEVEL", "PID013", "").isEmpty());
	}

	// @Test
	public void getAllDataNodesTestOne() throws IOException {
		DataNode d = new DataNode();
		d.setType(Entities.POSITION.name());
		d.setStatus(NodeStatus.VERIFIED.name());
		d.setDepartment("iGOT");
		d.setBookmark(false);
		RequestObject requestObject = new RequestObject();
		Mockito.doReturn(new ArrayList<DataNode>()).when(fracDao).getAllDataNodes(requestObject, null);
		assertEquals(0, fracDao.getAllDataNodes(requestObject, null).size());

	}

	// @Test
	public void getCountOfNodesTest() throws IOException {
		Mockito.doReturn(0L).when(fracDao).getCountOfNodes(Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString(), null);
		// assertEquals(0L, fracDao.getCountOfNodes("POSITION", "department", "123",
		// null));

	}

	@Test
	public void getNodeRatingAverage() throws IOException {
		BytesReference source = new BytesArray(
				"{\"comments\":[\"Manager comment\"],\"rating\":5.00,\"id\":\"PID017\",\"type\":\"POSITION\",\"user\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 1, 1);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };

		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(null, fracDao.getNodeRatingAverage("POSITION", "PID").getRating());
	}

	// @Test
	public void getVerificationList() throws IOException {
		BytesReference source = new BytesArray(
				"{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position description\",\"id\":\"PID0114\",\"source\":\"ISTM\",\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"DRAFT\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		when(configurationPanel.titleCaseConversion(Matchers.anyString())).thenReturn("d");
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(0, fracDao.getVerificationList("POSITION", "COMPETENCIES", false, null).getRejected().size());
		assertEquals(0, fracDao.getVerificationList("POSITION", "All departments", false, null).getRejected().size());
	}

	// @Test
	public void getVerificationListOne() throws IOException {
		BytesReference source = new BytesArray(
				"{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position  description\",\"id\":\"PID0114\",\"source\":null,\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"REJECTED\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		when(configurationPanel.titleCaseConversion(Matchers.anyString())).thenReturn("d");
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(1, fracDao.getVerificationList("POSITION", "COMPETENCIES", true, null).getRejected().size());
	}

	// @Test
	public void getVerificationListTwo() throws IOException {
		BytesReference source = new BytesArray(
				"{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position description\",\"id\":\"PID0114\",\"source\":\"ISTM\",\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"VERIFIED\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		when(configurationPanel.titleCaseConversion(Matchers.anyString())).thenReturn("d");
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(0, fracDao.getVerificationList("POSITION", "COMPETENCIES", true, null).getRejected().size());
	}

	@Test
	public void getCollectionLogs() throws IOException {
		BytesReference source = new BytesArray(
				"{\"reference\":\"Review\",\"comments\":\"Thecompetencyistoogeneric\",\"changeStatement\":\"ItemwasREJECTED\",\"id\":\"PID003\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(false, fracDao.getCollectionLogs("PID003", "POSITION").isEmpty());
		assertEquals(false, fracDao.getCollectionLogs("PID003", null).isEmpty());
	}

	// @Test
	public void getChildNodes() throws IOException {
		BytesReference source = new BytesArray(
				"{\"parent\":\"POSITION\",\"parentId\":\"PID065\",\"child\":\"ROLE\",\"childIds\":[\"RID06\"]}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);

		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(1, fracDao.getChildNodes("PID065", "ROLE").size());
	}

	@Test
	public void getParentNodes() throws IOException {
		BytesReference source = new BytesArray(
				"{\"parent\":\"POSITION\",\"parentId\":\"PID065\",\"child\":\"ROLE\",\"childIds\":[\"RID034\",\"RID06\"]}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(true, fracDao.getParentNodes("AID020", "ROLE").isEmpty());
	}

	@Test
	public void filterDataNodesOne() throws IOException {
		List<String> ab = new ArrayList<>();
		ab.add("ab");
		NodeFilter nodeFilter = new NodeFilter("type", ab);
		List<NodeFilter> filters = new ArrayList<>();
		filters.add(nodeFilter);
		FilterList filterList = new FilterList("POSITION", true, "DRAFT", "", filters, null, null);
		BytesReference source = new BytesArray(
				"{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position description\",\"id\":\"PID0114\",\"source\":\"ISTM\",\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"DRAFT\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(0, fracDao.filterDataNodes(filterList, "123").size());
	}

	@Test
	public void filterDataNodesThree() throws IOException {
		List<String> ab = new ArrayList<>();
		ab.add("ab");
		NodeFilter nodeFilter = new NodeFilter("type", ab);
		List<NodeFilter> filters = new ArrayList<>();
		filters.add(nodeFilter);
		FilterList filterList = new FilterList("POSITION", true, "DRAFT", "", filters, null, null);
		BytesReference source = new BytesArray(
				"{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position description\",\"id\":\"PID0114\",\"source\":\"ISTM\",\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"DRAFT\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(0, fracDao.filterDataNodes(filterList, "123").size());
	}

	@Test
	public void filterDataNodesTwo() throws IOException {
		List<String> ab = new ArrayList<>();
		ab.add("ab");
		NodeFilter nodeFilter = new NodeFilter("type", ab);
		List<NodeFilter> filters = new ArrayList<>();
		filters.add(nodeFilter);
		FilterList filterList = new FilterList("POSITION", true, "DRAFT", "All departments", filters, null, null);
		BytesReference source = new BytesArray(
				"{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position description\",\"id\":\"PID0114\",\"source\":\"ISTM\",\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"DRAFT\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		assertEquals(0, fracDao.filterDataNodes(filterList, "123").size());
	}

	// // @Test
	// public void exploreAllNodes() throws IOException {
	// assertEquals(5, fracDao.exploreAllNodes("ABC", "POSITION",
	// "DEPARTMENT").getDataNodes().size());
	// }

	// @Test
	// public void getAllNodesOf() throws IOException {
	// BytesReference source = new BytesArray(
	// "{\"createdByEmail\":\"sakthivel.govindan@tarento.com\",\"createdDate\":\"2021-01-29
	//
	// 10:33:31\",\"createdBy\":\"a781bd01-1d72-4ee7-84cd-2e97eec520f8\",\"name\":\"Assitant\",\"description\":\"Position
	//
	// description\",\"id\":\"PID0114\",\"source\":\"ISTM\",\"additionalProperties\":{\"Department\":\"igot\"},\"status\":\"DRAFT\"}");
	// SearchHit hit = new SearchHit(1);
	// hit.sourceRef(source);
	// SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
	// SearchResponseSections searchResponseSections = new
	// SearchResponseSections(hits, null, null, false, null, null,
	// 5);
	// SearchResponse searchResponse = new SearchResponse(searchResponseSections,
	// null, 8, 8, 0, 8,
	// new ShardSearchFailure[] {}, null);
	// Item items1 = new Item(searchResponse, new Exception());
	// Item[] items = { items1 };
	// MultiSearchResponse response = new MultiSearchResponse(items);
	// Mockito.doReturn("d").when(configurationPanel.titleCaseConversion(Matchers.anyString());
	// Mockito.doReturn(response).when(commonService).executeMultiSearchRequest(Matchers.anyObject());
	// Mockito.doReturn(true).when(elasticRepository).writeDatatoElastic(Matchers.anyMapOf(String.class,
	// Object.class),
	// Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
	// Mockito.doReturn(true).when(fr).initiateDictionaryPush(new DataNode());
	// assertEquals(new HashMap<Object, Object>().getClass(),
	// fracDao.getAllNodesOf("POSITION", new ArrayList<>(), "ABCD",
	// "DEPARTMENT").getClass());
	// }

	@Test
	public void getCompetencyAreaListing() {
		Map<String, DataNode> nodeMap = new HashMap<>();
		DataNode dataNode = new DataNode();
		dataNode.setId("CID029");
		DataNode dataNode1 = new DataNode();
		dataNode1.setId("CID048");
		Map<String, Object> nodeProperty = new HashMap<>();
		nodeProperty.put("competencyArea", "Social Studies");
		dataNode.setAdditionalProperties(nodeProperty);
		dataNode1.setAdditionalProperties(nodeProperty);

		nodeMap.put("CID029", dataNode);
		nodeMap.put("CID048", dataNode1);
		Mockito.when(configurationPanel.getAllNodesFor(Entities.COMPETENCY.name())).thenReturn(nodeMap);

		Map<String, DataNode> compAreaMap = new HashMap<>();
		DataNode compArea = new DataNode();
		compArea.setId("CIA05");
		compArea.setName("Social Studies");
		DataNode compArea1 = new DataNode();
		compArea1.setId("CIA013");
		compArea1.setName("Decision Making");
		compAreaMap.put("CIA05", compArea);
		compAreaMap.put("CIA05", compArea1);
		Mockito.when(configurationPanel.getAllNodesFor("COMPETENCYAREA")).thenReturn(compAreaMap);

		assertEquals("Social Studies", fracDao.getCompetencyAreaListing(null, null).getKeyValues().get(1).getKey());
	}

	@Test
	public void filterByMappingsTest() throws IOException {
		FilterList filterList = new FilterList();
		filterList.setType(Entities.KNOWLEDGERESOURCE.name());
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.setType(Entities.POSITION.name());
		filterMappings.setId("PID07");
		filterMappings.setRelation(Constants.Parameters.PARENT);
		List<FilterMappings> mapList = new ArrayList<>();
		mapList.add(filterMappings);
		filterList.setMappings(mapList);

		DataNode dataNode = new DataNode();
		dataNode.setId("RID06");
		dataNode.setType(Entities.ROLE.name());
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());
		DataNode dataNode1 = new DataNode();
		dataNode1.setId("AID012");
		dataNode1.setType(Entities.ACTIVITY.name());
		dataNode1.setStatus(NodeStatus.UNVERIFIED.name());
		DataNode dataNode2 = new DataNode();
		dataNode2.setId("KRID07");
		dataNode2.setType(Entities.KNOWLEDGERESOURCE.name());
		dataNode2.setStatus(NodeStatus.UNVERIFIED.name());

		Map<String, DataNode> nodeListMap = new HashMap<>();
		nodeListMap.put("RID06", dataNode);
		nodeListMap.put("AID012", dataNode1);
		nodeListMap.put("KRID07", dataNode2);
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyString())).thenReturn(nodeListMap);

		NodeMapping nodeMapping = new NodeMapping();
		nodeMapping.setParent(Entities.POSITION.name());
		nodeMapping.setParentId("PID07");
		List<String> childIds = new ArrayList<>();
		childIds.add("RID06");
		childIds.add("RID019");
		nodeMapping.setChildIds(childIds);
		nodeMapping.setChild(Entities.ROLE.name());
		NodeMapping nodeMapping1 = new NodeMapping();
		nodeMapping1.setParent(Entities.ROLE.name());
		nodeMapping1.setParentId("RID06");
		List<String> childIds1 = new ArrayList<>();
		childIds1.add("AID012");
		nodeMapping1.setChildIds(childIds1);
		nodeMapping1.setChild(Entities.ACTIVITY.name());
		NodeMapping nodeMapping2 = new NodeMapping();
		nodeMapping2.setParent(Entities.ACTIVITY.name());
		nodeMapping2.setParentId("AID012");
		List<String> childIds2 = new ArrayList<>();
		childIds2.add("KRID07");
		nodeMapping2.setChildIds(childIds2);
		nodeMapping2.setChild(Entities.KNOWLEDGERESOURCE.name());

		Map<String, NodeMapping> nodeMap = new HashMap<>();
		nodeMap.put("PID07", nodeMapping);
		nodeMap.put("RID06", nodeMapping1);
		nodeMap.put("AID012", nodeMapping2);
		// Mockito.when(ConfigurationPanel.getNodeMapping()).thenReturn(nodeMap);

		List<DataNode> bookmarkNode = Arrays.asList(dataNode);
		Mockito.doReturn(bookmarkNode).when(fracDao).getBookmark(userInfo.getSub(), filterList.getType());

		assertEquals(true, fracDao.filterByMappings(filterList, userInfo.getSub()).isEmpty());

	}

	// @Test
	public void filterByMappingsTestOne() throws IOException {
		FilterList filterList = new FilterList();
		filterList.setType(Entities.POSITION.name());
		FilterMappings filterMappings = new FilterMappings();
		filterMappings.setType(Entities.KNOWLEDGERESOURCE.name());
		filterMappings.setId("KRID07");
		filterMappings.setRelation(Constants.Parameters.CHILD);
		List<FilterMappings> mapList = new ArrayList<>();
		mapList.add(filterMappings);
		filterList.setMappings(mapList);

		DataNode dataNode = new DataNode();
		dataNode.setId("RID06");
		dataNode.setType(Entities.ROLE.name());
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());
		DataNode dataNode1 = new DataNode();
		dataNode1.setId("AID012");
		dataNode1.setType(Entities.ACTIVITY.name());
		dataNode1.setStatus(NodeStatus.UNVERIFIED.name());
		DataNode dataNode2 = new DataNode();
		dataNode2.setId("KRID07");
		dataNode2.setType(Entities.KNOWLEDGERESOURCE.name());
		dataNode2.setStatus(NodeStatus.UNVERIFIED.name());

		Map<String, DataNode> nodeListMap = new HashMap<>();
		nodeListMap.put("RID06", dataNode);
		nodeListMap.put("AID012", dataNode1);
		nodeListMap.put("KRID07", dataNode2);
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyString())).thenReturn(nodeListMap);

		NodeMapping nodeMapping = new NodeMapping();
		nodeMapping.setParent(Entities.POSITION.name());
		nodeMapping.setParentId("PID07");
		List<String> childIds = new ArrayList<>();
		childIds.add("RID06");
		childIds.add("RID019");
		nodeMapping.setChildIds(childIds);
		nodeMapping.setChild(Entities.ROLE.name());
		NodeMapping nodeMapping1 = new NodeMapping();
		nodeMapping1.setParent(Entities.ROLE.name());
		nodeMapping1.setParentId("RID06");
		List<String> childIds1 = new ArrayList<>();
		childIds1.add("AID012");
		nodeMapping1.setChildIds(childIds1);
		nodeMapping1.setChild(Entities.ACTIVITY.name());
		NodeMapping nodeMapping2 = new NodeMapping();
		nodeMapping2.setParent(Entities.ACTIVITY.name());
		nodeMapping2.setParentId("AID012");
		List<String> childIds2 = new ArrayList<>();
		childIds2.add("KRID07");
		nodeMapping2.setChildIds(childIds2);
		nodeMapping2.setChild(Entities.KNOWLEDGERESOURCE.name());

		Map<String, NodeMapping> nodeMap = new HashMap<>();
		nodeMap.put("PID07", nodeMapping);
		nodeMap.put("RID06", nodeMapping1);
		nodeMap.put("AID012", nodeMapping2);
		Mockito.when(configurationPanel.getNodeMapping()).thenReturn(nodeMap);

		BytesReference source = new BytesArray(
				"{\"userInfo\":{\"sub\":\"10dc4673-7d2e-4fe9-8adb-22d112b4c8bb\"},\"name\":\"TechnicalAssistant\",\"id\":\"PID0127\",\"type\":\"POSITION\",\"status\":\"DRAFT\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());

		assertEquals(true, fracDao.filterByMappings(filterList, "10dc4673-7d2e-4fe9-8adb-22d112b4c8bb").isEmpty());

	}

	// @Test
	public void reviewMappings() throws IOException {
		MappingVerification verification = new MappingVerification();
		verification.setVerified(Boolean.TRUE);
		verification.setParentId("AID007");
		verification.setChild(Entities.KNOWLEDGERESOURCE.name());

		Map<String, NodeMapping> nodeMap = new HashMap<>();
		NodeMapping nodeMapping = new NodeMapping();
		String id = verification.getParentId() + "-" + verification.getChild();
		nodeMap.put(id, nodeMapping);
		Mockito.when(configurationPanel.getNodeMapping()).thenReturn(nodeMap);
		Mockito.doReturn(true).when(elasticRepository).writeObjectToElastic(Matchers.anyObject(), Matchers.anyString(),
				Matchers.anyString(), Matchers.anyString());

		assertEquals(true, fracDao.reviewMappings(verification));

		verification.setVerified(Boolean.FALSE);
		BytesReference source = new BytesArray(
				"{\"parent\":\"ACTIVITY\",\"parentId\":\"AID001\",\"child\":\"KNOWLEDGERESOURCE\",\"childIds\":[\"KRID03\"],\"status\":\"VERIFIED\"}");
		SearchHit hit = new SearchHit(1);
		hit.sourceRef(source);
		SearchHits hits = new SearchHits(new SearchHit[] { hit }, 5, 10);
		SearchResponseSections searchResponseSections = new SearchResponseSections(hits, null, null, false, null, null,
				5);
		SearchResponse searchResponse = new SearchResponse(searchResponseSections, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items1 = new Item(searchResponse, new Exception());
		Item[] items = { items1 };
		MultiSearchResponse response = new MultiSearchResponse(items);
		Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());

		assertEquals(true, fracDao.reviewMappings(verification));
	}

	// @Test
	// public void pushContentToDictionaryTest() throws IOException {
	//
	// DataNode verifiedNode = new DataNode();
	// assertEquals(true, fracDao.pushContentToDictionary(verifiedNode));
	// Mockito.doReturn(Constants.Parameters.PRODUCTION).when(appProperties).getApplicationEnvironment();
	// verifiedNode.setType("COMPETENCYAREA");
	// assertEquals(true, fracDao.pushContentToDictionary(verifiedNode));
	//
	// verifiedNode.setId("CID05");
	// verifiedNode.setType(Entities.COMPETENCY.name());
	// verifiedNode.setStatus(NodeStatus.VERIFIED.name());
	// Map<String, DataNode> nodeMap = new HashMap<>();
	// nodeMap.put("CID05", verifiedNode);
	// Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyString())).thenReturn(nodeMap);
	//
	// Map<String, List<DataNode>> dictionaryMap = new HashMap<>();
	// List<DataNode> nodeList = new ArrayList<>();
	// nodeList.add(verifiedNode);
	// dictionaryMap.put("competencies", nodeList);
	// Mockito.when(configurationPanel.getDictionary()).thenReturn(dictionaryMap);
	// Mockito.doReturn(true).when(elasticRepository).writeDatatoElastic(Matchers.anyMapOf(String.class,
	// Object.class),
	// Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
	// assertEquals(true, fracDao.pushContentToDictionary(verifiedNode));
	//
	// DataNode verifiedNode2 = new DataNode();
	// verifiedNode2.setId("CID06");
	// verifiedNode2.setType(Entities.COMPETENCY.name());
	// verifiedNode2.setStatus(NodeStatus.VERIFIED.name());
	// // assertEquals(true, fracDao.pushContentToDictionary(verifiedNode2));
	//
	// verifiedNode.setStatus(NodeStatus.UNVERIFIED.name());
	// assertEquals(true, fracDao.pushContentToDictionary(verifiedNode));
	//
	// verifiedNode.setId("RID034");
	// verifiedNode.setType(Entities.ROLE.name());
	// verifiedNode.setStatus(NodeStatus.UNVERIFIED.name());
	// assertEquals(true, fracDao.pushContentToDictionary(verifiedNode));
	//
	// DataNode verifiedNode1 = new DataNode();
	// verifiedNode1.setId("PID013");
	// verifiedNode1.setType(Entities.POSITION.name());
	// verifiedNode1.setStatus(NodeStatus.VERIFIED.name());
	// assertEquals(true, fracDao.pushContentToDictionary(verifiedNode1));
	// }

}
