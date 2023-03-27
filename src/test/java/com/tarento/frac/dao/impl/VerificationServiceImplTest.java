package com.tarento.frac.dao.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.Entities;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.service.impl.FRACDictionaryServiceImpl;
import com.tarento.frac.service.impl.VerificationServiceImpl;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.DateUtils;
import com.tarento.frac.utils.Sql;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class VerificationServiceImplTest {

	@Mock
	ConfigurationPanel configurationPanel;

	@Mock
	JdbcTemplate jdbcTemplate;

	@Mock
	FRACDictionaryServiceImpl dictionaryService;

	@Spy
	@InjectMocks
	VerificationServiceImpl verificationService;

	private static UserInfo userInfo;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		userInfo = new UserInfo("f:923bdc18-520d-48d4-a84e-3cde1e655ebd:5574b3c5-16ca-49d8-8059-705304f2c7fb",
				"igot demo1", "igotdemo1", "igot", "demo1", "", null, "");
	}

	@Test
	public void verifyDataNodeTest() throws IOException {
		DataNodeVerification verification = new DataNodeVerification("", Boolean.TRUE, Constants.UserType.REVIEWER_ONE);
		verification.setId("PID098");
		verification.setType("POSITION");

		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.LEVEL_ONE_REVIEW_WITH_SECONDARY,
				new Object[] { verification.getStatus(), verification.getReviewComments(),
						verification.getSecondaryStatus(), userInfo.getSub(), DateUtils.getCurrentDateTimeInUTC(),
						verification.getId() });
		DataNode dataNode = new DataNode();
		dataNode.setId(verification.getId());
		dataNode.setType(verification.getType());
		Map<String, DataNode> nodeMap = new HashMap<>();
		nodeMap.put(verification.getId(), dataNode);
		Mockito.doReturn(nodeMap).when(configurationPanel).getAllNodesFor(verification.getType());
		Mockito.doReturn(true).when(dictionaryService).initiateDictionaryPush(verification, userInfo);
		assertEquals(false, verificationService.verifyDataNode(verification, userInfo));
		verification.setVerified(Boolean.FALSE);
		verification.setReviewComments("abc");
		assertEquals(false, verificationService.verifyDataNode(verification, userInfo));
		verification.setVerified(null);
		assertEquals(false, verificationService.verifyDataNode(verification, userInfo));

		verification.setVerified(Boolean.TRUE);
		verification.setUserType(Constants.UserType.REVIEWER_TWO);
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.LEVEL_TWO_REVIEW_WITH_PRIMARY,
				new Object[] { verification.getStatus(), verification.getSecondaryStatus(),
						verification.getSecondaryReviewComments(), userInfo.getSub(),
						DateUtils.getCurrentDateTimeInUTC(), verification.getId() });
		assertEquals(false, verificationService.verifyDataNode(verification, userInfo));
		// verification.setVerified(Boolean.FALSE);
		// Mockito.doReturn("").when(fracDao)
		// .getAuthToken(new Params(
		// "The node with the id $nodeId and type $type created by you has been
		// rejected. Please check.",
		// verification.getId(), verification.getType()));
		// assertEquals(true, fracDao.verifyDataNode(verification, userInfo));

		verification.setUserType(null);
		verification.setVerified(null);
		verification.setStatus(null);
		Mockito.doReturn(1).when(jdbcTemplate).update(Sql.DataNode.LEVEL_TWO_REVIEW,
				new Object[] { verification.getSecondaryStatus(), verification.getSecondaryReviewComments(),
						userInfo.getSub(), DateUtils.getCurrentDateTimeInUTC(), verification.getId() });
		assertEquals(false, verificationService.verifyDataNode(verification, userInfo));

		verification.setType(Entities.KNOWLEDGERESOURCE.name());
		assertEquals(false, verificationService.verifyDataNode(verification, userInfo));

	}

}
