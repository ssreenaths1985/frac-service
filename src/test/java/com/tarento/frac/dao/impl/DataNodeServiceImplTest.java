package com.tarento.frac.dao.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchResponseSections;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.service.impl.ActivityServiceImpl;
import com.tarento.frac.service.impl.DataNodeServiceImpl;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class DataNodeServiceImplTest {

	@Mock
	ConfigurationPanel configurationPanel;

	@Mock
	FRACDaoImpl fracDao;

	@Mock
	ActivityServiceImpl activityService;

	@Spy
	@InjectMocks
	DataNodeServiceImpl dataNodeService;

	private static UserInfo userInfo;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		userInfo = new UserInfo("f:923bdc18-520d-48d4-a84e-3cde1e655ebd:5574b3c5-16ca-49d8-8059-705304f2c7fb",
				"igot demo1", "igotdemo1", "igot", "demo1", "", null, "");
	}

	@Test
	public void addDataNodeTestOne() throws IOException {
		String nodeId = "PID001";
		DataNode dataNode = new DataNode();
		dataNode.setName("Director");
		dataNode.setType(Entities.POSITION.name());
		dataNode.setStatus(NodeStatus.DRAFT.name());
		// Mockito.doReturn(nodeId).when(fracDao).getNextSequenceFor(Matchers.anyString());
		// Mockito.doReturn(true).when(fracDao).updateNodeLogs(Matchers.anyObject(),
		// Matchers.anyObject(),
		// Matchers.anyString());
		// Mockito.doReturn(true).when(fracDao).addDataNode(Matchers.any());
		// assertEquals(null, fracDao.addDataNode(dataNode, userInfo).getId());

		Map<String, DataNode> posMap = new HashMap<>();
		DataNode nodeMap = new DataNode();
		nodeMap.setId(nodeId);
		nodeMap.setStatus(NodeStatus.DRAFT.name());
		posMap.put(nodeId, nodeMap);
		dataNode.setId(nodeId);
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());
		// Mockito.doReturn(true).when(activityService).updateDataNode(Matchers.any());
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyObject())).thenReturn(posMap);
		assertEquals("PID001", dataNodeService.addDataNode(dataNode, userInfo).getId());
	}

	// @Test
	public void addDataNodeTestTwo() throws IOException {
		String nodeId = "CIL001";
		DataNode dataNode = new DataNode();
		dataNode.setName("Level1");
		dataNode.setType(Entities.COMPETENCIESLEVEL.name());
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());
		// Mockito.doReturn(nodeId).when(fracDao).getNextSequenceFor(Matchers.anyString());
		// Mockito.doReturn(true).when(fracDao).updateNodeLogs(Matchers.anyObject(),
		// Matchers.anyObject(),
		// Matchers.anyString());
		// Mockito.doReturn(true).when(fracDao).addDataNode(Matchers.any());
		// assertEquals(null, fracDao.addDataNode(dataNode, userInfo).getId());

		dataNode.setId(nodeId);
		BytesReference source = new BytesArray(
				"{\"parent\":\"COMPETENCY\",\"parentId\":\"CID05\",\"child\":\"COMPETENCIESLEVEL\"}");
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
		// Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		Mockito.doReturn(true).when(fracDao).updateDataNode(Matchers.anyObject());
		Map<String, DataNode> posMap = new HashMap<>();
		DataNode nodeMap = new DataNode();
		nodeMap.setId(nodeId);
		nodeMap.setStatus(NodeStatus.UNVERIFIED.name());
		posMap.put(nodeId, nodeMap);
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyObject())).thenReturn(posMap);
		// assertEquals("CIL001", fracDao.addDataNode(dataNode, userInfo).getId());
	}

	// @Test
	public void addDataNodeTestThree() throws IOException {
		String nodeId = "CIL001";
		DataNode dataNode = new DataNode();
		dataNode.setId(nodeId);
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyObject())).thenReturn(null);
		// Mockito.doReturn(true).when(fracDao).updateNodeLogs(Matchers.anyObject(),
		// Matchers.anyObject(),
		// Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).updateDataNode(Matchers.any());
		// Mockito.doReturn("").when(appProperties).getIndexNameForType(null);
		// assertEquals(nodeId, fracDao.addDataNode(dataNode, userInfo).getId());
	}

	// @Test
	public void addDataNodeTestFour() throws IOException {
		String nodeId = "CIL001";
		DataNode dataNode = new DataNode();
		dataNode.setStatus(NodeStatus.DRAFT.name());
		dataNode.setId(nodeId);
		dataNode.setName("Level1");
		dataNode.setType(Entities.COMPETENCIESLEVEL.name());
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());

		Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put(Constants.Parameters.ACTION, Constants.Actions.UPDATE);
		nodeMap.put(Constants.Parameters.INDEX, "index");
		nodeMap.put(Constants.Parameters.TYPE, "type");
		// Mockito.doReturn(nodeMap).when(fracDao).setDataNodeDetailsBeforeInsertionOrUpdation(dataNode,
		// userInfo);

		BytesReference source = new BytesArray("{\"parent\":\"COMPETENCY\",\"child\":\"COMPETENCIESLEVEL\"}");
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
		// Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		Mockito.doReturn(true).when(fracDao).addDataNode(Matchers.anyObject());
		Mockito.doReturn(true).when(activityService).updateNodeLogs(Matchers.anyObject(), Matchers.anyObject(),
				Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).updateDataNode(Matchers.any());
		// assertEquals(nodeId, fracDao.addDataNode(dataNode, userInfo).getId());
	}

	// @Test
	public void addDataNodesTestOne() throws IOException {
		List<DataNode> dataNodeList = new ArrayList<>();
		DataNode dataNode = new DataNode();
		dataNode.setType(Entities.ACTIVITY.name());
		dataNodeList.add(dataNode);
		// Mockito.doReturn("AID001").when(fracDao).getNextSequenceFor(Matchers.anyString());
		Mockito.doReturn(true).when(activityService).updateNodeLogs(Matchers.anyObject(), Matchers.anyObject(),
				Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).addDataNodeBulk(Matchers.any());
		Mockito.doReturn(true).when(fracDao).updateDataNodeBulk(Matchers.any());
		// assertEquals(true, fracDao.addDataNodes(dataNodeList, userInfo).isEmpty());
	}

	// @Test
	public void addDataNodesTestTwo() throws IOException {
		List<DataNode> dataNodeList = new ArrayList<>();
		DataNode dataNode = new DataNode();
		dataNode.setId("CIL001");
		dataNode.setType(Entities.COMPETENCIESLEVEL.name());
		dataNodeList.add(dataNode);
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyObject())).thenReturn(null);
		Mockito.doReturn(true).when(activityService).updateNodeLogs(Matchers.anyObject(), Matchers.anyObject(),
				Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).addDataNodeBulk(Matchers.any());
		Mockito.doReturn(true).when(fracDao).updateDataNodeBulk(Matchers.any());

		BytesReference source = new BytesArray("{}");
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
		// Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		// assertEquals(true, fracDao.addDataNodes(dataNodeList, userInfo).isEmpty());
	}

	// @Test
	public void addDataNodesTestThree() throws IOException {
		List<DataNode> dataNodeList = new ArrayList<>();
		DataNode dataNode1 = new DataNode();
		dataNode1.setId("AID001");
		DataNode dataNode2 = new DataNode();
		dataNode2.setId("AID002");
		dataNodeList.add(dataNode1);
		dataNodeList.add(dataNode2);
		Map<String, Object> nodeMap1 = new HashMap<>();
		nodeMap1.put(Constants.Parameters.OPERATION, "");
		nodeMap1.put(Constants.Parameters.ACTION, "");
		DataNode dataNode = new DataNode();
		dataNode.setType(Entities.COMPETENCIESLEVEL.name());
		nodeMap1.put(Constants.Parameters.DATA_NODE, dataNode);
		// Mockito.doReturn(nodeMap1).when(fracDao).setDataNodeDetailsBeforeInsertionOrUpdation(dataNode1,
		// userInfo);
		Map<String, Object> nodeMap2 = new HashMap<>();
		nodeMap2.put(Constants.Parameters.OPERATION, "");
		nodeMap2.put(Constants.Parameters.ACTION, "");
		nodeMap2.put(Constants.Parameters.DATA_NODE, new DataNode());
		// Mockito.doReturn(nodeMap2).when(fracDao).setDataNodeDetailsBeforeInsertionOrUpdation(dataNode2,
		// userInfo);
		Mockito.doReturn(true).when(activityService).updateNodeLogs(Matchers.anyObject(), Matchers.anyObject(),
				Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).addDataNodeBulk(Matchers.any());
		Mockito.doReturn(true).when(fracDao).updateDataNodeBulk(Matchers.any());
		// assertEquals(false, fracDao.addDataNodes(dataNodeList, userInfo).isEmpty());
	}

	// @Test
	public void addDataNodeBulkTestOne() throws IOException, CloneNotSupportedException {
		DataNode dataNode = new DataNode();
		dataNode.setType(Entities.ROLE.name());
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());
		List<DataNode> childNodes = new ArrayList<>();

		DataNode childNode1 = new DataNode();
		childNode1.setId("CIL001");
		childNode1.setType(Entities.COMPETENCIESLEVEL.name());
		childNodes.add(childNode1);

		DataNode childNode2 = new DataNode();
		childNode2.setId("CIL002");
		childNode2.setType(Entities.COMPETENCIESLEVEL.name());
		childNode2.setStatus(NodeStatus.UNVERIFIED.name());
		childNodes.add(childNode2);
		dataNode.setChildren(childNodes);

		Map<String, DataNode> childMap = new HashMap<>();
		childMap.put("CIL001", childNode1);
		childMap.put("CIL002", childNode2);
		Mockito.when(configurationPanel.getAllNodesFor(Entities.COMPETENCIESLEVEL.name())).thenReturn(childMap);

		// Mockito.doReturn("RID001").when(fracDao).getNextSequenceFor(dataNode.getType());
		Mockito.doReturn(true).when(activityService).updateNodeLogs(Matchers.anyObject(), Matchers.anyObject(),
				Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).addDataNodeBulk(Matchers.any());
		Mockito.doReturn(true).when(fracDao).updateDataNodeBulk(Matchers.any());
		List<DataNode> dataNodes = Arrays.asList(dataNode);
		// assertEquals("RID001", fracDao.addDataNodeBulkList(dataNodes,
		// userInfo).get(0).getId());
	}

	// @Test
	public void addDataNodeBulkTestTwo() throws IOException, CloneNotSupportedException {
		DataNode dataNode = new DataNode();
		dataNode.setId("RID001");
		dataNode.setType(Entities.ROLE.name());

		List<DataNode> childNodes = new ArrayList<>();
		DataNode childNode1 = new DataNode();
		childNode1.setType(Entities.COMPETENCIESLEVEL.name());
		DataNode childNode2 = new DataNode();
		childNode2.setType(Entities.COMPETENCIESLEVEL.name());
		childNodes.add(childNode1);
		childNodes.add(childNode2);
		dataNode.setChildren(childNodes);

		// Mockito.doReturn("CIL001").when(fracDao).getNextSequenceFor(Entities.COMPETENCIESLEVEL.name());
		Map<String, Object> childDataNode = new HashMap<>();
		childDataNode.put(Constants.Parameters.OPERATION, "");
		childDataNode.put(Constants.Parameters.ACTION, "");
		childDataNode.put(Constants.Parameters.DATA_NODE, childNode2);
		// Mockito.doReturn(childDataNode).when(fracDao).setDataNodeDetailsBeforeInsertionOrUpdation(childNode2,
		// userInfo);

		Mockito.doReturn(true).when(activityService).updateNodeLogs(Matchers.anyObject(), Matchers.anyObject(),
				Matchers.anyString());
		Mockito.doReturn(true).when(fracDao).addDataNodeBulk(Matchers.any());
		Mockito.doReturn(true).when(fracDao).updateDataNodeBulk(Matchers.any());

		List<DataNode> dataNodes = Arrays.asList(dataNode);
		// fracDao.addDataNodeBulkList(dataNodes, userInfo);
		// assertEquals("RID001", fracDao.addDataNodeBulkList(dataNodes,
		// userInfo).get(0).getId());
	}

	// @Test
	public void addDataNodeBulkTestThree() throws IOException, CloneNotSupportedException {
		DataNode dataNode = new DataNode();
		dataNode.setId("RID001");
		Map<String, Object> nodeMap = new HashMap<>();
		nodeMap.put(Constants.Parameters.ACTION, "");
		nodeMap.put(Constants.Parameters.OPERATION, "");
		nodeMap.put(Constants.Parameters.DATA_NODE, dataNode);
		// Mockito.doReturn(nodeMap).when(fracDao).setDataNodeDetailsBeforeInsertionOrUpdation(Matchers.anyObject(),
		// Matchers.anyObject());
		//
		// Mockito.doReturn(true).when(fracDao).updateNodeLogs(Matchers.anyObject(),
		// Matchers.anyObject(),
		// Matchers.anyString());
		List<DataNode> dataNodes = Arrays.asList(dataNode);
		// fracDao.addDataNodeBulkList(dataNodes, userInfo);

		Mockito.doReturn(true).when(fracDao).addDataNodeBulk(Matchers.any());
		Mockito.doReturn(true).when(fracDao).updateDataNodeBulk(Matchers.any());

		List<DataNode> dataNodeList = Arrays.asList(dataNode);
		// assertEquals("RID001", fracDao.addDataNodeBulkList(dataNodeList,
		// userInfo).get(0).getId());
	}

	// @Test
	public void appendMapNodesOne() throws IOException {
		List<String> childIds = new ArrayList<>();
		childIds.add("a");
		childIds.add("b");
		NodeMapping newMapping = new NodeMapping(null, "COMPETENCY", "abcd", "COMPETENCIESLEVEL", "", childIds,
				"UNVERIFIED");
		BytesReference source = new BytesArray(
				"{\"parent\":\"COMPETENCY\",\"childIds\":[\"CIL024\"],\"parentId\":\"CID05\",\"child\":\"COMPETENCIESLEVEL\"}");
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
		// Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		// Mockito.doReturn(true).when(elasticRepository).writeDatatoElastic(Matchers.anyMapOf(String.class,
		// Object.class),
		// Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
		// assertEquals(true, fracDao.appendMapNodes(newMapping, userInfo));
	}

	// @Test
	public void deleteNode() throws IOException {
		// Mockito.doReturn(1).when(jdbcTemplate).update("update data_node set is_active
		// = false where id=? and type=?",
		// new Object[] {}, new Object[] { "RID001", "ROLE" });
		// Mockito.doReturn(1).when(jdbcTemplate).update("delete from node_mapping where
		// child_id=? or parent_id=?",
		// new Object[] {}, new Object[] { "RID001", "RID001" });
		// Mockito.doReturn(true).when(dictionaryService).initiateDictionaryPush(new
		// DataNode(), userInfo);
		// Mockito.doReturn(true).when(fracDao).updateNodeLogs(Matchers.anyObject(),
		// Matchers.anyObject(),
		// Matchers.anyString());
		//
		// assertEquals(true, fracDao.deleteNode("RID001", "ROLE", userInfo));
	}

	// @Test
	public void mapNodesTest() throws IOException {
		BytesReference source = new BytesArray(
				"{\"parent\":\"COMPETENCY\",\"childIds\":[\"CIL024\"],\"parentId\":\"CID05\",\"child\":\"COMPETENCIESLEVEL\"}");
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
		SearchRequest sr = new SearchRequest("frac-nodemapping").types("doc").source(new SearchSourceBuilder());
		// Mockito.doReturn(response).when(commonDao).executeMultiSearchRequest(sr);
		DataNode dataNode = new DataNode();
		dataNode.setName("CreativityLevel");
		dataNode.setDescription("abc");
		dataNode.setLevel("Level 10");
		dataNode.setStatus("abc");
		dataNode.setId("PID");
		dataNode.setAdditionalProperties(new HashMap<>());
		dataNode.setType("POSITION");
		List<String> childIds = new ArrayList<>();
		childIds.add("a");
		childIds.add("b");
		BytesReference sources = new BytesArray("{\"source\":\"abc\"}");
		SearchHit hitss = new SearchHit(1);
		hit.sourceRef(sources);
		SearchHits hits1 = new SearchHits(new SearchHit[] { hitss }, 5, 10);
		SearchResponseSections searchResponseSections1 = new SearchResponseSections(hits1, null, null, false, null,
				null, 5);
		SearchResponse searchResponse1 = new SearchResponse(searchResponseSections1, null, 8, 8, 0, 8,
				new ShardSearchFailure[] {}, null);
		Item items12 = new Item(searchResponse1, new Exception());
		Item[] items123 = { items12 };
		MultiSearchResponse responses = new MultiSearchResponse(items123);
		// Mockito.doReturn(responses).when(commonDao).executeMultiSearchRequest(Matchers.anyObject());
		NodeMapping nodeMapping = new NodeMapping(null, "COMPETENCY", "PID", "COMPETENCIESLEVEL", "", childIds,
				NodeStatus.UNVERIFIED.name());

		// Mockito.doReturn(true).when(elasticRepository).writeObjectToElastic(Matchers.anyObject(),
		// Matchers.anyString(),
		// Matchers.anyString(), Matchers.anyString());
		Map<String, NodeMapping> nodeMap = new HashMap<>();
		nodeMap.put("PID", nodeMapping);
		Map<String, DataNode> dMap = new HashMap<>();
		dMap.put("PID", dataNode);
		Mockito.when(configurationPanel.getAllNodesFor(Matchers.anyString())).thenReturn(dMap);
		// Mockito.doReturn(true).when(elasticRepository).updateDataInElastic(
		// Matchers.anyMapOf(String.class, Object.class), Matchers.anyString(),
		// Matchers.anyString(),
		// Matchers.anyString());
		Mockito.when(configurationPanel.getNodeMapping()).thenReturn(nodeMap);
		// Mockito.doReturn(true).when(elasticRepository).writeDatatoElastic(Matchers.anyMapOf(String.class,
		// Object.class),
		// Matchers.anyString(), Matchers.anyString(), Matchers.anyString());
		// assertTrue(fracDao.mapNodes(nodeMapping, userInfo));
	}

	// // @Test
	// public void getNextSequenceForTest() throws IOException {
	// Mockito.doReturn(true).when(elasticRepository).writeObjectToElastic(Matchers.anyObject(),
	// Matchers.anyString(),
	// Matchers.anyString(), Matchers.anyString());
	// FRACKeys keys = new FRACKeys(1, "ab", 2, "ab", 3, "ab", 4, "ab", 5, "ab", 6,
	// "ab", 7, "ab");
	// Mockito.when(configurationPanel.getFRACKeys()).thenReturn(keys);
	// assertEquals("ab2", fracDao.getNextSequenceFor("POSITION"));
	// assertEquals("ab3", fracDao.getNextSequenceFor("ROLE"));
	// assertEquals("ab4", fracDao.getNextSequenceFor("ACTIVITY"));
	// assertEquals("ab5", fracDao.getNextSequenceFor("COMPETENCY"));
	// assertEquals("ab6", fracDao.getNextSequenceFor("KNOWLEDGERESOURCE"));
	// assertEquals("ab7", fracDao.getNextSequenceFor("COMPETENCIESLEVEL"));
	// assertEquals("ab8", fracDao.getNextSequenceFor("COMPETENCYAREA"));
	// }

	// // @Test
	// public void getLabelTest() {
	// assertEquals(Constants.Parameters.KR,
	// data.getLabel(Entities.KNOWLEDGERESOURCE.name()));
	// assertEquals(Constants.Parameters.COMPETENCY_LEVEL,
	// fracDao.getLabel(Entities.COMPETENCIESLEVEL.name()));
	// assertEquals(Constants.Parameters.COMPETENCY_AREA,
	// fracDao.getLabel(Entities.COMPETENCYAREA.name()));
	// assertEquals(Constants.Parameters.COMP_TYPE,
	// fracDao.getLabel(Constants.Parameters.COMPETENCY_TYPE));
	// assertEquals(Constants.Parameters.COD.toUpperCase(),
	// fracDao.getLabel(Constants.Parameters.COD));
	// assertEquals(Constants.Parameters.URL.toUpperCase(),
	// fracDao.getLabel(Constants.Parameters.URL));
	// }

}
