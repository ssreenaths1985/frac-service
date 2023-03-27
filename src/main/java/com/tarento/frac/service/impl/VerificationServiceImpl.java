package com.tarento.frac.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tarento.frac.ConfigurationPanel;
import com.tarento.frac.dao.FRACDao;
import com.tarento.frac.models.DataNode;
import com.tarento.frac.models.DataNodeVerification;
import com.tarento.frac.models.NodeStatus;
import com.tarento.frac.models.TemplateParams;
import com.tarento.frac.models.UserInfo;
import com.tarento.frac.service.ActivityService;
import com.tarento.frac.service.FRACDictionaryService;
import com.tarento.frac.service.VerificationService;
import com.tarento.frac.utils.ApplicationProperties;
import com.tarento.frac.utils.AuthUtil;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.NotificationUtil;
import com.tarento.frac.utils.TaggingConstants;

@Service
public class VerificationServiceImpl implements VerificationService {

	public static final Logger Logger = LoggerFactory.getLogger(VerificationServiceImpl.class);

	@Autowired
	private FRACDao fracDao;

	@Autowired
	private ConfigurationPanel configurationPanel;

	@Autowired
	private FRACDictionaryService dictionaryService;

	@Autowired
	private ActivityService activityService;

	@Autowired
	private ApplicationProperties appProperties;

	@Autowired
	private AuthUtil authUtil;

	@Autowired
	private NotificationUtil notificationUtil;

	@Override
	public Boolean verifyDataNode(DataNodeVerification verification, UserInfo userInfo) {
		return verificationList(new ArrayList<>(Arrays.asList(verification)), userInfo);
	}

	private Boolean verificationList(List<DataNodeVerification> verificationList, UserInfo userInfo) {
		try {
			if (!verificationList.isEmpty()) {
				for (DataNodeVerification verification : verificationList) {
					if (StringUtils.isNotBlank(verification.getId()) && StringUtils.isNotBlank(verification.getType())
							&& TaggingConstants.getTaggingMap().get(verification.getType())
							&& (verification.getReviewComments() != null
									|| (verification.getVerified() == null || verification.getVerified()))) {

						String reviewLevel = "Review";
						String reviewStatus = "";
						if (!StringUtils.isBlank(verification.getUserType())
								&& verification.getUserType().equals(Constants.UserType.REVIEWER_ONE)) {
							reviewLevel = Constants.Actions.TECHNICAL_REVIEW;
							verification.setSecondaryStatus(null);
							verification.setSecondaryReviewComments(null);
							if (verification.getVerified() == null) {
								verification.setStatus(NodeStatus.UNVERIFIED.name());
							} else if (verification.getVerified()) {
								verification.setStatus(NodeStatus.VERIFIED.name());
								verification.setSecondaryStatus(NodeStatus.UNVERIFIED.name());
							} else if (!verification.getVerified()) {
								verification.setStatus(NodeStatus.REJECTED.name());
							}
							fracDao.verifyLevelOne(verification, userInfo.getSub());
						} else {
							reviewLevel = Constants.Actions.REVIEW_BOARD;
							verification.setSecondaryReviewComments(verification.getReviewComments());
							if (verification.getVerified() == null) {
								verification.setSecondaryStatus(NodeStatus.UNVERIFIED.name());
								verification.setStatus(NodeStatus.UNVERIFIED.name());
								reviewStatus = NodeStatus.UNVERIFIED.name();
							} else if (verification.getVerified()) {
								verification.setSecondaryStatus(NodeStatus.VERIFIED.name());
								verification.setStatus(NodeStatus.VERIFIED.name());
							} else if (!verification.getVerified()) {
								reviewStatus = NodeStatus.REJECTED.name();
								verification.setSecondaryStatus(NodeStatus.REJECTED.name());
								verification.setStatus(NodeStatus.UNVERIFIED.name());
							}
							fracDao.verifyLevelTwo(verification, userInfo.getSub());
						}

						sendNodeRejectedNotification(verification, userInfo.getAuthToken(), Boolean.FALSE);
						verification.setReviewedBy(userInfo.getSub());
						// update logs
						DataNode reviewNode = verification.clone();
						if (StringUtils.isNotBlank(reviewStatus)) {
							reviewNode.setStatus(reviewStatus);
						}
						activityService.updateNodeLogs(reviewNode, userInfo, reviewLevel);
						updateDictionaryOnVerification(verification, userInfo);

					}
				}
			}
			Executors.newSingleThreadExecutor()
					.submit(activityService.updateAuditData(verificationList, userInfo, Boolean.TRUE));
			return Boolean.TRUE;
		} catch (Exception e) {
			Logger.error(String.format("Error in VerifyDataNode : %s", e.getMessage()));
			return Boolean.FALSE;
		}
	}

	@Override
	public Boolean verifyAllDataNode(DataNodeVerification verification, UserInfo userInfo) {
		try {
			if (StringUtils.isNotBlank(verification.getType())) {
				List<DataNode> dataNodes = fracDao.getAllDataNodeByType(verification.getType());

				// create list of data node to verify
				List<DataNodeVerification> verificationList = new ArrayList<>();
				for (DataNode nodeObj : dataNodes) {
					DataNodeVerification verificationObj = new DataNodeVerification();
					verificationObj.setId(nodeObj.getId());
					verificationObj.setType(verification.getType());
					verificationObj.setVerified(verification.getVerified());
					if (verification.getUserType().equals(Constants.UserType.REVIEWER_ONE)) {
						verificationObj.setReviewComments(verification.getReviewComments());
					}
					if (verification.getUserType().equals(Constants.UserType.REVIEWER_TWO)) {
						verificationObj.setSecondaryReviewComments(verification.getSecondaryReviewComments());
					}
					verificationObj.setUserType(verification.getUserType());
					verificationList.add(verificationObj);
				}

				new Thread(() -> {
					verificationList(verificationList, userInfo);
				}).start();
				return Boolean.TRUE;
			}

		} catch (Exception e) {
			Logger.error(String.format("Encountered an error in verifyAllDataNode  :  %s", e.getMessage()));
		}
		return Boolean.FALSE;
	}

	private void sendNodeRejectedNotification(DataNodeVerification verification, String authToken, Boolean review) {
		String status = review ? verification.getStatus() : verification.getSecondaryStatus();
		if (StringUtils.isNotBlank(status) && status == NodeStatus.REJECTED.name()) {
			if (configurationPanel.getAllNodesFor(verification.getType()).containsKey(verification.getId())) {
				DataNode node = configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId());
				String messageTitle = configurationPanel.titleCaseConversion(verification.getType()) + " rejected";
				String userId = review ? node.getReviewedBy() : node.getCreatedBy();
				String pageURL = appProperties.getFracUrl()
						+ ConfigurationPanel.getFRACURLByType(verification.getType(), verification.getId());
				String subject = configurationPanel.titleCaseConversion(verification.getType())
						+ " rejection from FRAC tool";
				if (StringUtils.isNotBlank(userId)) {
					String userEmail = authUtil.getUserEmail(userId, authToken);
					TemplateParams templateParams = new TemplateParams(null, verification.getId(), messageTitle,
							verification.getType().toLowerCase(), verification.getReviewComments(),
							new ArrayList<>(Arrays.asList(userEmail)), node.getName(), pageURL, subject, authToken);
					notificationUtil.sendNotification(templateParams);
				}
			}
		}
	}

	private void updateDictionaryOnVerification(DataNodeVerification verification, UserInfo userInfo) {
		new Thread(() -> {
			try {
				DataNode reviewNode = new DataNode();
				if (configurationPanel.getAllNodesFor(verification.getType()) != null && configurationPanel
						.getAllNodesFor(verification.getType()).containsKey(verification.getId())) {
					reviewNode = configurationPanel.getAllNodesFor(verification.getType()).get(verification.getId());
				} else {
					reviewNode = fracDao.getNodeById(verification.getId(), Boolean.FALSE, Boolean.FALSE, Boolean.FALSE,
							null);
				}

				if (reviewNode != null && StringUtils.isNotBlank(reviewNode.getId())) {
					reviewNode.setStatus(verification.getStatus());
					reviewNode.setSecondaryStatus(verification.getSecondaryStatus());
					reviewNode.setReviewComments(verification.getReviewComments());
					reviewNode.setSecondaryReviewComments(verification.getSecondaryReviewComments());
					ConfigurationPanel.addDataNode(reviewNode, reviewNode.getType());
					dictionaryService.initiateDictionaryPush(reviewNode.clone(), userInfo);

				}
			} catch (Exception e) {
				Logger.error(String.format("Error in updateDictionaryOnVerification : %s", e.getMessage()));
			}
		}).start();
	}

}
