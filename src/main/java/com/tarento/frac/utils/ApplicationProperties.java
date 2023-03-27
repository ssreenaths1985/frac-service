package com.tarento.frac.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.tarento.frac.models.Entities;

import lombok.Getter;

@Configuration
@SuppressWarnings("all")
@Getter
@PropertySource(value = { "/application.properties" })
public class ApplicationProperties {

	@Value("${elasticsearch.type}")
	String esDocumentType;
	@Value("${elasticsearch.commentrating.index}")
	String commentRatingIndex;
	@Value("${elasticsearch.collectionlogs.index}")
	String collectionLogs;
	@Value("${elasticsearch.dictionary.index}")
	String dictionary;
	@Value("${elasticsearch.verifiedmapping.index}")
	String verifiedMapping;
	@Value("${elasticsearch.logs.type}")
	String esLogsType;
	@Value("${webhook.callback.url}")
	String webhookCallbackUrl;
	@Value("${application.environment}")
	String applicationEnvironment;
	@Value("${services.esindexer.host.name}")
	String rainHostName;
	@Value("${services.esindexer.host}")
	String elasticSearchHost;
	@Value("${services.esindexer.host.port}")
	int elasticSearchPort;
	@Value("${services.esindexer.username}")
	String esUsername;
	@Value("${services.esindexer.password}")
	String esPassword;
	@Value("${services.esindexer.host.nameport}")
	String elasticFullUrl;
	@Value("${role-verification-url}")
	String roleVerificationUrl;
	@Value("${cloud-provider}")
	String cloudProvider;
	@Value("${auth-api-key}")
	String authAPIKey;
	@Value("${notification-url}")
	String notificationUrl;
	@Value("${send-notification}")
	String sendNotification;
	@Value("${frac-url}")
	String fracUrl;
	@Value("${audit.eid}")
	String eid;
	@Value("${audit.edata.props}")
	String props;
	@Value("${audit.pdata.id}")
	String pdataId;
	@Value("${audit.pdata.pid}")
	String pdataPid;
	@Value("${audit.pdata.ver}")
	String pdataVer;
	@Value("${audit.type}")
	String auditType;
	@Value("${audit.ver}")
	String auditVer;
	@Value("${kafka.audit.topic}")
	String auditTopic;
	@Value("${accesstoken.publickey.basepath}")
	String accessTokenKeyFile;
	@Value("${sunbird_sso_url}")
	String ssoUrl;
	@Value("${sunbird_sso_realm}")
	String ssoRealm;
}