package com.tarento.frac.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.models.analytics.AggregateDto;
import com.tarento.frac.service.DataNodeService;
import com.tarento.frac.service.FRACService;
import com.tarento.frac.service.VerificationService;
import com.tarento.frac.utils.AuthUtil;

/**
 * 
 * @author nivetha
 *
 */
public class FRACControllerTest {

	@InjectMocks
	FRACController fracController;

	@Mock
	AuthUtil authUtil;

	@Mock
	FRACService fracService;

	@Mock
	private DataNodeService dataNodeService;

	@Mock
	private VerificationService verificationService;

	private UserInfo userInfo;

	private String authToken;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		// building user object
		userInfo = new UserInfo("f:923bdc18-520d-48d4-a84e-3cde1e655ebd:5574b3c5-16ca-49d8-8059-705304f2c7fb",
				"igot demo1", "igotdemo1", "igot", "demo1", "", null, authToken);
		authToken = "Bearer token";
	}

	@Test
	public void addDataNodeTest() throws JsonProcessingException {
		DataNode dataNode = new DataNode();

		// Failure response
		Mockito.doReturn(null).when(dataNodeService).addDataNode(dataNode, userInfo);
		fracController.addDataNode(dataNode, userInfo);
		Mockito.doReturn(dataNode).when(dataNodeService).addDataNode(dataNode, userInfo);
		fracController.addDataNode(dataNode, userInfo);

		// Success
		dataNode.setId("PID01");
		fracController.addDataNode(dataNode, userInfo);

		// Unauthorized
		fracController.addDataNode(dataNode, userInfo);
	}

	@Test
	public void addDataNodesTest() throws JsonProcessingException {
		List<DataNode> dataNodeList = new ArrayList<>();

		// Failure response
		Mockito.doReturn(null).when(dataNodeService).addDataNodes(dataNodeList, userInfo);
		fracController.addDataNodes(dataNodeList, userInfo);
		Mockito.doReturn(dataNodeList).when(dataNodeService).addDataNodes(dataNodeList, userInfo);
		fracController.addDataNodes(dataNodeList, userInfo);

		// Success
		dataNodeList.add(new DataNode());
		fracController.addDataNodes(dataNodeList, userInfo);

		// Unauthorized
		fracController.addDataNodes(dataNodeList, userInfo);
	}

	@Test
	public void getCountOfNodesTest() throws IOException {
		Mockito.doReturn(2L).when(fracService).getCountOfNodes(Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString(), Matchers.anyString());
		fracController.getCountOfNodes("positions", "PID01", "abc", "");
		// Unauthorized
		fracController.getCountOfNodes("positions", "PID01", "abc", "");
	}

	@Test
	public void verifyDataNodeTest() throws IOException {
		DataNodeVerification dataNodeVerification = new DataNodeVerification();

		// Failure response
		Mockito.doReturn(null).when(verificationService).verifyDataNode(dataNodeVerification, userInfo);
		fracController.verifyDataNode(dataNodeVerification, userInfo, "https://frac.igot-dev.in");

		// Success
		Mockito.doReturn(true).when(verificationService).verifyDataNode(dataNodeVerification, userInfo);
		fracController.verifyDataNode(dataNodeVerification, userInfo, "https://frac.igot-dev.in");

		// Unauthorized
		fracController.verifyDataNode(dataNodeVerification, userInfo, "https://frac.igot-dev.in");
	}

	@Test
	public void getAllDataNodesTest() throws IOException {
		DataNode d = new DataNode();
		d.setType(Entities.POSITION.name());
		d.setStatus(NodeStatus.VERIFIED.name());
		d.setDepartment("iGOT");
		d.setBookmark(false);
		Mockito.doReturn(new ArrayList<>()).when(fracService).getAllDataNodes(d, false, null, authToken, "userType");
		fracController.getAllDataNodes(Entities.POSITION.name(), false, NodeStatus.VERIFIED.name(), "iGOT", false, true,
				"userType", userInfo);

		// Unauthorized
		fracController.getAllDataNodes(Entities.POSITION.name(), false, NodeStatus.VERIFIED.name(), "iGOT", null, false,
				"userType", userInfo);
	}

	@Test
	public void getAllDataNodesTestOne() throws IOException {
		DataNode d = new DataNode();
		d.setType(Entities.POSITION.name());
		d.setStatus(NodeStatus.VERIFIED.name());
		d.setDepartment("iGOT");
		d.setBookmark(false);

		Mockito.doReturn(null).when(fracService).getAllDataNodes(d, false, null, authToken, "userType");
		fracController.getAllDataNodes(Entities.POSITION.name(), false, NodeStatus.VERIFIED.name(), "iGOT", false, true,
				"userType", userInfo);

		// Unauthorized
		fracController.getAllDataNodes(Entities.POSITION.name(), false, NodeStatus.VERIFIED.name(), "iGOT", null, false,
				"userType", userInfo);
	}

	@Test
	public void filterDataNodesTest() throws IOException {
		FilterList filterList = new FilterList();
		Mockito.doReturn(new ArrayList<>()).when(fracService).filterDataNodes(filterList, "userId");
		fracController.filterDataNodes(filterList, userInfo);

		// Unauthorized
		fracController.filterDataNodes(filterList, userInfo);
	}

	@Test
	public void filterDataNodesTestOne() throws IOException {
		FilterList filterList = new FilterList();
		Mockito.doReturn(null).when(fracService).filterDataNodes(filterList, "userId");
		fracController.filterDataNodes(filterList, userInfo);

		// Unauthorized
		fracController.filterDataNodes(filterList, userInfo);
	}

	@Test
	public void getVerificationListTest() throws IOException {
		Mockito.doReturn(new DataNodesVerificationView()).when(fracService)
				.getVerificationList(Entities.POSITION.name(), "iGOT", false, "");
		fracController.getVerificationList(Entities.POSITION.name(), "", false, "iGOT");

		Mockito.doReturn(null).when(fracService).getVerificationList(Entities.POSITION.name(), "iGOT", false, "");
		fracController.getVerificationList(Entities.POSITION.name(), "", false, "iGOT");

		// Unauthorized
		fracController.getVerificationList(Entities.POSITION.name(), "", false, "iGOT");

	}

	@Test
	public void mapNodesTest() throws IOException {
		NodeMapping nodeMapping = new NodeMapping();
		Mockito.doReturn(null).when(dataNodeService).mapNodes(nodeMapping, userInfo);
		fracController.mapNodes(nodeMapping, userInfo);
		// Success
		Mockito.doReturn(true).when(dataNodeService).mapNodes(nodeMapping, userInfo);
		fracController.mapNodes(nodeMapping, userInfo);
		// Unauthorized
		fracController.mapNodes(nodeMapping, userInfo);
	}

	@Test
	public void nodeFeedbackTest() throws IOException {
		Extensions extensions = new Extensions();

		// Success
		Mockito.doReturn(true).when(fracService).nodeFeedback(extensions, "user");
		fracController.nodeFeedback(extensions, userInfo);

		// Unauthorized
		fracController.nodeFeedback(extensions, userInfo);
	}

	@Test
	public void appendMapNodesTest() throws IOException {
		NodeMapping nodeMapping = new NodeMapping();
		// Failure
		Mockito.doReturn(false).when(dataNodeService).appendMapNodes(nodeMapping, new UserInfo());
		fracController.appendMapNodes(nodeMapping, userInfo);
		// Success
		Mockito.doReturn(true).when(dataNodeService).appendMapNodes(nodeMapping, new UserInfo());
		fracController.appendMapNodes(nodeMapping, userInfo);
		// Unauthorized
		fracController.appendMapNodes(nodeMapping, userInfo);
	}

	@Test
	public void getNodeFeedbackTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).getNodeFeedback(Entities.POSITION.name(), "PID01",
				"userId");
		fracController.getNodeFeedback(Entities.POSITION.name(), "PID01", Boolean.FALSE, userInfo);
		fracController.getNodeFeedback(Entities.POSITION.name(), "PID01", Boolean.TRUE, userInfo);
		fracController.getNodeFeedback(Entities.POSITION.name(), "PID01", null, userInfo);

		Mockito.doReturn(null).when(fracService).getNodeFeedback(Entities.POSITION.name(), "PID01", "userId");
		fracController.getNodeFeedback(Entities.POSITION.name(), "PID01", Boolean.FALSE, userInfo);

		// Unauthorized
		fracController.getNodeFeedback(Entities.POSITION.name(), "PID01", Boolean.FALSE, userInfo);
	}

	@Test
	public void getNodeRatingAverageTest() throws IOException {
		Extensions ex = new Extensions();
		ex.setId("ID");
		Mockito.doReturn(ex).when(fracService).getNodeRatingAverage("PID01", "userId");
		fracController.getNodeRatingAverage("positions", "PID01");
		// Unauthorized
		fracController.getNodeRatingAverage("positions", "PID01");
	}

	@Test
	public void getChildNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).getChildNodes("PID01", "userId");
		fracController.getChildNodes("positions", "PID01");

		Mockito.doReturn(null).when(fracService).getChildNodes("PID01", "userId");
		fracController.getChildNodes("positions", "PID01");

		// Unauthorized
		fracController.getChildNodes("positions", "PID01");
	}

	@Test
	public void getChildNodesTestOne() throws IOException {
		Mockito.doReturn(null).when(fracService).getChildNodes("PID01", "userId");
		fracController.getChildNodes("positions", "PID01");

		// Unauthorized
		fracController.getChildNodes("positions", "PID01");
	}

	@Test
	public void getParentNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).getParentNodes("PID01", "userId");
		fracController.getParentNodes("positions", "PID01");
		// Unauthorized
		fracController.getParentNodes("positions", "PID01");
	}

	@Test
	public void getNodeByIdTest() throws IOException {
		Mockito.doReturn(new DataNode()).when(fracService).getNodeById("PID01", true, null, false, "userId");
		Mockito.doReturn(new DataNode()).when(fracService).getNodeById("PID01", false, true, true, "userId");
		fracController.getNodeById("PID01", true, false, false, userInfo);
		fracController.getNodeById("PID01", false, true, true, userInfo);
		// Unauthorized
		fracController.getNodeById("PID01", true, null, null, userInfo);
	}

	@Test
	public void getMappingTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).getMapping("PID01", "POSITION", "KNOWLEDGERESOURCE",
				false);
		Mockito.doReturn(new ArrayList<>()).when(fracService).getMapping("PID01", "POSITION", "KNOWLEDGERESOURCE",
				true);
		fracController.getMapping("PID01", "POSITION", "KNOWLEDGERESOURCE", false);
		fracController.getMapping("PID01", "POSITION", "KNOWLEDGERESOURCE", true);
		// Unauthorized
		fracController.getMapping("PID01", "POSITION", "KNOWLEDGERESOURCE", true);
	}

	@Test
	public void deleteNodeTest() throws IOException {
		Mockito.doReturn(true).when(dataNodeService).deleteNode(Matchers.anyString(), Matchers.anyString(),
				Matchers.any());
		fracController.deleteNode("positions", "PID01", userInfo);
		// Unauthorized
		fracController.deleteNode("positions", "PID01", userInfo);
	}

	@Test
	public void searchNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).searchNodes(Matchers.any());
		fracController.searchNodes(new MultiSearch());
		// Unauthorized
		fracController.searchNodes(new MultiSearch());
	}

	@Test
	public void exploreAllNodesTest() throws IOException {
		// Mockito.doReturn(new
		// Overview()).when(fracService).exploreAllNodes(Matchers.anyString(),
		// Matchers.anyString(),
		// Matchers.anyString());
		fracController.exploreAllNodes("PID01", "abc");
		// Unauthorized
		fracController.exploreAllNodes("PID01", "abc");
	}

	@Test
	public void exploreSearchTest() throws IOException {
		Mockito.doReturn(new Overview()).when(fracService).exploreSearch(Matchers.anyString(), Matchers.anyString());
		fracController.exploreSearch("positions", "PID01");
		// Unauthorized
		fracController.exploreSearch("positions", "PID01");
	}

	@Test
	public void getCompetencyAreaListingTest() throws IOException {
		Mockito.doReturn(new KeyValueList()).when(fracService).getCompetencyAreaListing(Matchers.anyString(),
				Matchers.anyString());
		fracController.getCompetencyAreaListing("positions", "PID01");
		// Unauthorized
		fracController.getCompetencyAreaListing("positions", "PID01");
	}

	@Test
	public void getCollectionLogsTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).getCollectionLogs(Matchers.anyString(),
				Matchers.anyString());
		fracController.getCollectionLogs("positions", "PID01");
		// Unauthorized
		fracController.getCollectionLogs("positions", "PID01");
	}

	@Test
	public void addDataNodeBulkTest() throws IOException, CloneNotSupportedException {
		Mockito.doReturn(new DataNode()).when(dataNodeService).addDataNodeBulk(Matchers.any(), Matchers.any());
		fracController.addDataNodeBulk(new DataNode(), userInfo);
		// Unauthorized
		fracController.addDataNodeBulk(new DataNode(), userInfo);
	}

	@Test
	public void addDataNodeBulkTestOne() throws IOException, CloneNotSupportedException {
		Mockito.doReturn(null).when(dataNodeService).addDataNodeBulk(Matchers.any(), Matchers.any());
		fracController.addDataNodeBulk(new DataNode(), userInfo);
		// Unauthorized
		fracController.addDataNodeBulk(new DataNode(), userInfo);
	}

	@Test
	public void bookmarkDataNodeTest() throws JsonProcessingException {
		Mockito.doReturn(true).when(fracService).bookmarkDataNode(Matchers.anyObject(), Matchers.anyObject());
		fracController.bookmarkDataNode(new DataNode(), userInfo);

		DataNode bookmarkNode = new DataNode();
		bookmarkNode.setId("KIR001");
		bookmarkNode.setType(Entities.KNOWLEDGERESOURCE.name());
		fracController.bookmarkDataNode(bookmarkNode, userInfo);

		// Unauthorized
		fracController.bookmarkDataNode(bookmarkNode, userInfo);
	}

	@Test
	public void getBookmarkTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).getBookmark(Matchers.anyString(), Matchers.anyString());
		fracController.getBookmark(Entities.KNOWLEDGERESOURCE.name(), userInfo);

		// Unauthorized
		fracController.getBookmark(null, userInfo);
	}

	@Test
	public void filterByMappingsTest() throws JsonProcessingException {
		Mockito.doReturn(new ArrayList<>()).when(fracService).filterByMappings(Matchers.any(), Matchers.any());
		fracController.filterByMappings(new FilterList(), userInfo);

		// Unauthorized
		fracController.filterByMappings(new FilterList(), userInfo);

	}

	@Test
	public void reviewMappings() throws JsonProcessingException {
		Mockito.doReturn(Boolean.TRUE).when(fracService).reviewMappings(Matchers.anyObject());
		fracController.reviewMappings(Matchers.any());

		Mockito.doReturn(Boolean.FALSE).when(fracService).reviewMappings(Matchers.anyObject());
		fracController.reviewMappings(Matchers.any());

		Mockito.doReturn(null).when(fracService).reviewMappings(Matchers.anyObject());
		fracController.reviewMappings(Matchers.any());

		// Unauthorized
		fracController.reviewMappings(Matchers.any());
	}

	@Test
	public void filterReviewNodesTest() throws IOException {
		FilterList filterList = new FilterList();
		DataNodesVerificationView dataNodes = new DataNodesVerificationView();
		Mockito.doReturn(dataNodes).when(fracService).filterReviewNodes(Matchers.anyObject());
		fracController.filterReviewNodes(filterList);

		Mockito.doReturn(dataNodes).when(fracService).filterReviewNodes(Matchers.anyObject());
		fracController.filterReviewNodes(filterList);

		// Unauthorized
		fracController.filterReviewNodes(filterList);
	}

	@Test
	public void getSourceListTest() throws IOException {
		List<String> source = new ArrayList<>();
		Mockito.doReturn(source).when(fracService).getSourceList(Matchers.anyObject());
		fracController.getSourceList("a");

		Mockito.doReturn(source).when(fracService).getSourceList(Matchers.anyObject());
		fracController.getSourceList("a");

		// Unauthorized
		fracController.getSourceList("a");
	}

	@Test
	public void getMyGraphsTest() throws IOException {
		Mockito.doReturn(new AggregateDto()).when(fracService).getMyGraphs(Matchers.anyObject(), Matchers.anyString());
		fracController.getMyGraphs("a", userInfo);

		Mockito.doReturn(new AggregateDto()).when(fracService).getMyGraphs(Matchers.anyObject(), Matchers.anyString());
		fracController.getMyGraphs("a", userInfo);

		// Unauthorized
		fracController.getMyGraphs("a", userInfo);
	}

	@Test
	public void flushReloadCacheTest() throws IOException {
		Mockito.doReturn(true).when(fracService).flushReloadCache(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean());
		fracController.flushReloadCache("a", true, true);

		Mockito.doReturn(true).when(fracService).flushReloadCache(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean());
		fracController.flushReloadCache("a", true, true);

		// Unauthorized
		fracController.flushReloadCache("a", true, true);
	}

}