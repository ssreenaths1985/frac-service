package com.tarento.frac.service.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.FRACDao;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.DataNodesVerificationView;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.Extensions;
import com.tarento.frac.models.FilterList;
import com.tarento.frac.models.KeyValueList;
import com.tarento.frac.models.MappingVerification;
import com.tarento.frac.models.MultiSearch;
import com.tarento.frac.models.NodeMapping;
import com.tarento.frac.models.Overview;
import com.tarento.frac.models.RequestObject;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.models.analytics.AggregateDto;
import com.tarento.frac.repository.ElasticSearchRepository;
import com.tarento.frac.utils.ApplicationProperties;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class FRACServiceImplTest {

	@Mock
	FRACDao fracDao;

	@Mock
	ElasticSearchRepository elasticRepository;

	@Mock
	RestTemplate restTemplate;

	@Mock
	JdbcTemplate jdbcTemplate;

	@Mock
	FRACServiceImpl fr;

	@Spy
	@InjectMocks
	FRACServiceImpl fracService;

	@Spy
	@InjectMocks
	DataNodeServiceImpl dataService;

	@Spy
	@InjectMocks
	VerificationServiceImpl verificationService;

	@SpyBean
	ApplicationProperties appProperties;

	@Mock
	ConfigurationPanel configurationPanel;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getNodeByIdTest() throws IOException {
		Mockito.doReturn(new DataNode()).when(fracDao).getNodeById(Matchers.anyString(), Matchers.anyBoolean(),
				Matchers.anyBoolean(), Matchers.anyBoolean(), Matchers.anyString());
		assertEquals(new DataNode().getClass(),
				fracService.getNodeById("PID003", true, true, true, "userId").getClass());

	}

	@Test
	public void searchNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).searchNodes(Matchers.anyObject());
		assertEquals(0L, fracService.searchNodes(new MultiSearch()).size());

	}

	@Test
	public void bookmarkDataNodeTest() throws IOException {
		Mockito.doReturn(true).when(fracDao).bookmarkDataNode(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(true, fracService.bookmarkDataNode(new DataNode(), new UserInfo()));

	}

	@Test
	public void exploreAllNodesV3Test() throws IOException {
		Mockito.doReturn(new Overview()).when(fracDao).exploreAllNodes(Matchers.anyString(), Matchers.anyString());
		assertEquals(null, fracService.exploreAllNodes("POSITION", "department").getDataNodes());

	}

	@Test
	public void getParentNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getParentNodes(Matchers.anyString(), Matchers.anyString());
		assertEquals(0, fracService.getParentNodes("PID6767", "department").size());

	}

	@Test
	public void filterDataNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).filterDataNodes(Matchers.anyObject(), Matchers.anyString());
		assertEquals(0, fracService.filterDataNodes(new FilterList(), "department").size());

	}

	@Test
	public void getCountOfNodesTest() throws IOException {
		Mockito.doReturn(1L).when(fracDao).getCountOfNodes(Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString(), Matchers.anyString());
		assertEquals(1, fracService.getCountOfNodes("PID6767", "department", "status", "userType").intValue());

	}

	@Test
	public void getVerificationListTest() throws IOException {
		Mockito.doReturn(new DataNodesVerificationView()).when(fracDao).getVerificationList(Matchers.anyString(),
				Matchers.anyString(), Matchers.anyBoolean(), Matchers.anyString());
		assertEquals(new DataNodesVerificationView().getClass(),
				fracService.getVerificationList("PID6767", "department", true, "userType").getClass());

	}

	@Test
	public void getBookmarkTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getBookmark(Matchers.anyString(), Matchers.anyString());
		assertEquals(0, fracService.getBookmark("PID6767", "department").size());

	}

	@Test
	public void filterReviewNodesTest() throws IOException {
		Mockito.doReturn(new DataNodesVerificationView()).when(fracDao).filterReviewNodes(Matchers.anyObject());
		assertEquals(new DataNodesVerificationView().getClass(),
				fracService.filterReviewNodes(new FilterList()).getClass());

	}

	@Test
	public void getSourceListTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getSourceList(Matchers.anyString());
		assertEquals(0, fracService.getSourceList("department").size());

	}

//	@Test
	public void getAllDataNodesTest() throws IOException {
		DataNode d = new DataNode();
		RequestObject requestObject = new RequestObject();
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getAllDataNodes(requestObject, null);
		assertEquals(0, fracService.getAllDataNodes(d, true, true, "UID", "userType").size());

	}

	@Test
	public void getAllDataNodesTestOne() throws IOException {
		DataNode d = new DataNode();
		d.setType(Entities.COMPETENCYAREA.name());
		Mockito.doReturn(new ArrayList<>()).when(fracDao).fetchCompetencyAreas();
		assertEquals(0, fracService.getAllDataNodes(d, true, true, "UID", "userType").size());

	}

	@Test
	public void addDataNodeTest() throws IOException {
		Mockito.doReturn(new DataNode()).when(dataService).addDataNode(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(new DataNode().getClass(), dataService.addDataNode(new DataNode(), new UserInfo()).getClass());

	}

	@Test
	public void addDataNodeTestOne() throws IOException {
		Mockito.doReturn(new DataNode()).when(dataService).addDataNode(Matchers.anyObject(), Matchers.anyObject());
//		assertEquals(new LinkedList<>().getClass(),
//				dataService.addDataNodes(new ArrayList<>(), new UserInfo()).getClass());

	}

	// @Test
	public void addDataNodeBulkTest() throws IOException {
		// Mockito.doReturn(new
		// DataNode()).when(dataService).addDataNodeBulkList(Matchers.anyObject(),
		// Matchers.anyObject())
		// .get(0);
		// assertEquals(new DataNode().getClass(), dataService.addDataNodeBulk(new
		// DataNode(), new UserInfo()).getClass());

	}

	@Test
	public void deleteNodeTest() throws IOException {
		Mockito.doReturn(true).when(dataService).deleteNode(Matchers.anyString(), Matchers.anyString(),
				Matchers.anyObject());
		assertEquals(true, dataService.deleteNode("PID6767", "department", new UserInfo()));

	}

	@Test
	public void exploreSearchTest() throws IOException {
		Mockito.doReturn(new Overview()).when(fracDao).exploreSearch(Matchers.anyString(), Matchers.anyString());
		assertEquals(new Overview().getClass(), fracService.exploreSearch("PID6767", "department").getClass());

	}

	@Test
	public void verifyDataNodeTest() throws IOException {
		Mockito.doReturn(true).when(verificationService).verifyDataNode(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(true, verificationService.verifyDataNode(new DataNodeVerification(), new UserInfo()));
	}

	@Test
	public void getCompetencyAreaListingTest() throws IOException {
		Mockito.doReturn(new KeyValueList()).when(fracDao).getCompetencyAreaListing(Matchers.anyString(),
				Matchers.anyString());
		assertEquals(new KeyValueList().getClass(),
				fracService.getCompetencyAreaListing("department", "status").getClass());

	}

	@Test
	public void filterByMappingsTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).filterByMappings(Matchers.anyObject(), Matchers.anyString());
		assertEquals(new ArrayList<>().getClass(), fracService.filterByMappings(new FilterList(), "UID").getClass());

	}

	@Test
	public void getMappingTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getMapping(Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString(), Matchers.anyBoolean());
		assertEquals(new ArrayList<>().getClass(), fracService.getMapping("id", "type", "returnType", true).getClass());

	}

	@Test
	public void reviewMappingsTest() throws IOException {
		Mockito.doReturn(true).when(fracDao).reviewMappings(Matchers.anyObject());
		assertEquals(true, fracService.reviewMappings(new MappingVerification()));

	}

	@Test
	public void getMyGraphsTest() throws IOException {
		Mockito.doReturn(new AggregateDto()).when(fracDao).getMyGraphs(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(new AggregateDto().getClass(), fracService.getMyGraphs("visual", "sub").getClass());

	}

	@Test
	public void mapNodesTest() throws IOException {
		Mockito.doReturn(true).when(dataService).mapNodes(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(true, dataService.mapNodes(new NodeMapping(), new UserInfo()));

	}

	@Test
	public void nodeFeedbackTest() throws IOException {
		Mockito.doReturn(true).when(fracDao).nodeFeedback(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(true, fracService.nodeFeedback(new Extensions(), "name"));

	}

	@Test
	public void getNodeFeedbackTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getNodeFeedback(Matchers.anyString(), Matchers.anyString(),
				Matchers.anyString());
		assertEquals(new ArrayList<>().getClass(), fracService.getNodeFeedback("ID", "department", "UID").getClass());

	}

	@Test
	public void getNodeRatingAverageTest() throws IOException {
		Mockito.doReturn(new Extensions()).when(fracDao).getNodeRatingAverage(Matchers.anyString(),
				Matchers.anyString());
		assertEquals(new Extensions().getClass(), fracService.getNodeRatingAverage("ID", "department").getClass());

	}

	@Test
	public void appendMapNodesTest() throws IOException {
		Mockito.doReturn(true).when(dataService).appendMapNodes(Matchers.anyObject(), Matchers.anyObject());
		assertEquals(true, dataService.appendMapNodes(new NodeMapping(), new UserInfo()));

	}

	@Test
	public void getCollectionLogsTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getCollectionLogs(Matchers.anyString(), Matchers.anyString());
		assertEquals(new ArrayList<>().getClass(), fracService.getCollectionLogs("ID", "department").getClass());

	}

	@Test
	public void getChildNodesTest() throws IOException {
		Mockito.doReturn(new ArrayList<>()).when(fracDao).getChildNodes(Matchers.anyString(), Matchers.anyString());
		assertEquals(new ArrayList<>().getClass(), fracService.getChildNodes("ID", "department").getClass());

	}

	@Test
	public void flushReloadCacheTest() throws IOException {
		Mockito.when(configurationPanel.flushCache(Matchers.anyObject())).thenReturn(true);
		Mockito.when(configurationPanel.reloadCache(Matchers.anyObject())).thenReturn(true);
		assertEquals(Boolean.TRUE, fracService.flushReloadCache("ID", true, true));

	}

}