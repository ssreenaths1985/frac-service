package com.tarento.frac.dao.impl;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

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
import com.tarento.frac.models.AdditionalProperties;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.service.impl.ActivityServiceImpl;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class ActivityServiceImplTest {

	@Mock
	ConfigurationPanel configurationPanel;

	@Spy
	@InjectMocks
	ActivityServiceImpl activityService;

	private static UserInfo userInfo;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		userInfo = new UserInfo("f:923bdc18-520d-48d4-a84e-3cde1e655ebd:5574b3c5-16ca-49d8-8059-705304f2c7fb",
				"igot demo1", "igotdemo1", "igot", "demo1", "", null, "");
	}

	@Test
	public void updateNodeLogsTestOne() {
		DataNode dataNode = new DataNode();
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, ""));

		dataNode.setId("0");
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, ""));

		dataNode.setId("CID001");
		dataNode.setType(Entities.COMPETENCY.name());
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, ""));

		// Mockito.verify(configurationPanel.addDataNode(Matchers.anyObject(),
		// Matchers.anyString()));
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.CREATE));
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.PUBLISH));

		// Mockito.doNothing().when(configurationPanel.deleteDataNode(Matchers.anyString(),
		// Matchers.anyString()));
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.DELETE));
	}

	@Test
	public void updateNodeLogsTestTwo() {
		DataNode dataNode = new DataNode();
		dataNode.setId("PID002");
		dataNode.setType(Entities.POSITION.name());
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(null);
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.UPDATE));

		Map<String, DataNode> nodeMap = new HashMap<>();
		nodeMap.put("PID003", dataNode);
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(nodeMap);
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.UPDATE));
	}

	@Test
	public void updateNodeLogsTestThree() {
		DataNode dataNode = new DataNode();
		dataNode.setId("PID002");
		dataNode.setType(Entities.POSITION.name());
		dataNode.setName("Director");
		dataNode.setDescription("description");
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("Department", "iGOT");
		dataNode.setAdditionalProperties(additionalProperties);

		Map<String, DataNode> nodeMap = new HashMap<>();
		DataNode node = new DataNode();
		node.setId("PID002");
		node.setType(Entities.POSITION.name());
		node.setName("Director General");
		node.setDescription("Position description");
		node.setSource("ISTM");
		Map<String, Object> additionalProp = new HashMap<>();
		additionalProp.put("Department", "FRAC");
		node.setAdditionalProperties(additionalProp);

		nodeMap.put("PID002", node);
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(nodeMap);
		Mockito.when(configurationPanel.titleCaseConversion(Matchers.anyString())).thenReturn("Department");
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.UPDATE));
	}

	@Test
	public void updateNodeLogsTestFour() {
		DataNode dataNode = new DataNode();
		dataNode.setId("CID002");
		dataNode.setType(Entities.COMPETENCY.name());
		dataNode.setName("Admin");
		dataNode.setDescription("description");
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("competencyType", "Behavioural");
		additionalProperties.put("competencyArea", "Management");
		additionalProperties.put("cod", "JPAL");
		dataNode.setAdditionalProperties(additionalProperties);

		Map<String, DataNode> nodeMap = new HashMap<>();
		DataNode node = new DataNode();
		node.setId("CID002");
		node.setType(Entities.COMPETENCY.name());
		node.setName("Administartion");
		node.setDescription("Competency description");
		node.setSource("ISTM");
		Map<String, Object> additionalProp = new HashMap<>();
		additionalProp.put("competencyType", "Domain");
		additionalProperties.put("competencyArea", "Gov Management");
		additionalProperties.put("cod", "Karmayogi");
		node.setAdditionalProperties(additionalProp);

		nodeMap.put("CID002", node);
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(nodeMap);
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.UPDATE));
	}

	@Test
	public void updateNodeLogsTestFive() {
		DataNode dataNode = new DataNode();
		dataNode.setId("KRI001");
		dataNode.setType(Entities.KNOWLEDGERESOURCE.name());
		dataNode.setName("Project Management");
		dataNode.setDescription("description");
		Map<String, Object> additionalProperties = new HashMap<>();
		additionalProperties.put("URL", "tarento.com");
		additionalProperties.put("files", "Content1");
		dataNode.setAdditionalProperties(additionalProperties);

		Map<String, DataNode> nodeMap = new HashMap<>();
		DataNode node = new DataNode();
		node.setId("KRI001");
		node.setType(Entities.KNOWLEDGERESOURCE.name());
		node.setName("Role Management");
		node.setDescription("Resource description");
		node.setSource("ISTM");
		Map<String, Object> additionalProp = new HashMap<>();
		additionalProp.put("URL", "https://tarento.com");
		AdditionalProperties ap4 = new AdditionalProperties("files", null, "Content2");
		additionalProperties.put("files", "Content2");
		node.setAdditionalProperties(additionalProp);

		nodeMap.put("KRI001", node);
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(nodeMap);
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.UPDATE));
	}

	@Test
	public void updateNodeLogsTestSix() {
		DataNode dataNode = new DataNode();
		dataNode.setId("AID001");
		dataNode.setType(Entities.ACTIVITY.name());
		dataNode.setName("name");
		dataNode.setSource("ISTM");

		Map<String, DataNode> nodeMap = new HashMap<>();
		DataNode node = new DataNode();
		node.setId("AID001");
		node.setType(Entities.ACTIVITY.name());
		node.setName("name");
		node.setDescription("description");
		nodeMap.put("AID001", node);
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(nodeMap);
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.UPDATE));
	}

	@Test
	public void updateNodeLogsTestSeven() {
		DataNode dataNode = new DataNode();
		dataNode.setId("AID002");
		dataNode.setType(Entities.ACTIVITY.name());
		dataNode.setStatus(NodeStatus.UNVERIFIED.name());
		Map<String, DataNode> nodeMap = new HashMap<>();
		nodeMap.put("AID002", dataNode);
		Mockito.when(configurationPanel.getAllNodesFor(dataNode.getType())).thenReturn(nodeMap);
		assertEquals(true, activityService.updateNodeLogs(dataNode, userInfo, Constants.Actions.REVIEW_BOARD));
	}

}
