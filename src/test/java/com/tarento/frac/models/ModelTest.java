package com.tarento.frac.models;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import com.tarento.frac.models.analytics.AggregateDto;
import com.tarento.frac.models.analytics.ChartType;
import com.tarento.frac.models.analytics.Data;
import com.tarento.frac.models.analytics.InsightsWidget;
import com.tarento.frac.models.analytics.Plot;
import com.tarento.frac.models.analytics.RequestDate;
import com.tarento.frac.utils.Constants;
import com.tarento.frac.utils.PathRoutes;
import com.tarento.frac.utils.Sql;
import com.tarento.frac.utils.TaggingConstants;

public class ModelTest {

	@Test
	public void modelTest1() throws IOException {
		Activity ac1 = new Activity();
		Activity ac = new Activity("a", "b", "c");
		ac1.setDescription("d");
		ac1.setId("a");
		ac.setName("b");
		assertEquals("a".getClass(), ac.toString().getClass());
		ActivityDetail ad = new ActivityDetail();
		ActivityDetail ad1 = new ActivityDetail(new ArrayList<>());
		ad.setKnowledgeResources(new ArrayList<>());
		assertEquals("a".getClass(), ad1.toString().getClass());
		MultiSearch ms1 = new MultiSearch();
		ms1.setChildCount(true);
		ms1.setChildNodes(true);
		ms1.setSearches(new ArrayList<>());
		ms1.setUserType("userType");
		MultiSearch ms = new MultiSearch(new ArrayList<>(), null, false, false, "userType", null, null);
		ms.toString();
		KeyValueList kd = new KeyValueList();
		KeyValueList kd1 = new KeyValueList(new ArrayList<>());
		kd.setKeyValues(new ArrayList<>());
		assertEquals("a".getClass(), kd1.toString().getClass());
		KeyValue kv = new KeyValue();
		KeyValue kv1 = new KeyValue("a", "b");
		kv.setKey("a");
		kv.setValue("b");
		assertEquals("a".getClass(), kv1.toString().getClass());
		SearchBox sb1 = new SearchBox();
		sb1.setField("field");
		sb1.setKeyword("keyword");
		sb1.setType("type");
		SearchBox sb = new SearchBox("a", "b", "c");
		sb.toString();
		assertEquals("a".getClass(), sb.toString().getClass());
		RoleDetail rd = new RoleDetail();
		RoleDetail rd1 = new RoleDetail(new ArrayList<>());
		rd.setActivities(new ArrayList<>());
		assertEquals("a".getClass(), rd1.toString().getClass());
		Role r = new Role();
		Role r1 = new Role("a", "b", "c");
		r.setDescription("d");
		r.setId("a");
		r.setName("b");
		assertEquals("a".getClass(), r1.toString().getClass());
		PositionDetail pd = new PositionDetail();
		PositionDetail pd1 = new PositionDetail(new ArrayList<>());
		pd.setRoles(new ArrayList<>());
		assertEquals("a".getClass(), pd1.toString().getClass());
		Position p = new Position();
		Position p1 = new Position("a", "b", "c");
		p.setDescription("d");
		p.setId("a");
		p.setName("b");
		assertEquals("a".getClass(), p1.toString().getClass());
		FRACImage fd = new FRACImage();
		FRACImage fd1 = new FRACImage(new ArrayList<>());
		fd.setPositions(new ArrayList<>());
		assertEquals("a".getClass(), fd1.toString().getClass());
		NodeFilter n = new NodeFilter();
		NodeFilter n1 = new NodeFilter("a", new ArrayList<>());
		n.setField("a");
		n.setValues(new ArrayList<>());
		assertEquals("a".getClass(), n1.toString().getClass());
		Overview o = new Overview();
		Overview o1 = new Overview(new ArrayList<>(), new ArrayList<>());
		o.setAssociations(new ArrayList<>());
		o.setDataNodes(new ArrayList<>());
		assertEquals("a".getClass(), o1.toString().getClass());
		NodeFeedback n2 = new NodeFeedback();
		NodeFeedback n3 = new NodeFeedback(5.0, "abc", "a", "b");
		n2.setComments("a");
		n2.setRating(5.0);
		n2.setUpdatedDate("a");
		n2.setUser("a");
		assertEquals("a".getClass(), n3.toString().getClass());
		UserInfo ui = new UserInfo();
		UserInfo ui1 = new UserInfo("a", "a", "a", "a", "a", "a", null, "");
		ui.setEmail("a");
		ui.setFamilyName("a");
		ui.setGivenName("a");
		ui.setName("a");
		ui.setPreferredUserName("a");
		ui.setSub("a");
		assertEquals("a".getClass(), ui1.toString().getClass());
		Comment ci = new Comment();
		Comment ci1 = new Comment("a", "a", "a", "a", 1L);
		ci.setComment("a");
		ci.setDate(5L);
		ci.setId("a");
		ci.setType("a");
		ci.setUser("a");
		assertEquals("a".getClass(), ci1.toString().getClass());
		NodeCommentRating nci = new NodeCommentRating();
		NodeCommentRating nci1 = new NodeCommentRating("a", 5, 5, new ArrayList<>());
		nci.setUser("a");
		nci.setRating(5);
		nci.setAverage(5);
		nci.setComments(new ArrayList<>());
		assertEquals("a".getClass(), nci1.toString().getClass());
		FilterList fc = new FilterList();
		FilterList fc1 = new FilterList("a", true, "a", "a", new ArrayList<>(), new ArrayList<>(), "userType");
		fc.setType("a");
		fc.setFilters(new ArrayList<>());
		fc.setDepartment("a");
		fc.setIsDetail(true);
		fc.setStatus("a");
		assertEquals("a".getClass(), fc.toString().getClass());
		Dictionary d = new Dictionary();
		Dictionary d1 = new Dictionary(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
		d.setActivities(new ArrayList<>());
		d.setCompetencies(new ArrayList<>());
		d.setPositions(new ArrayList<>());
		d.setRoles(new ArrayList<>());
		assertEquals("a".getClass(), d1.toString().getClass());
		Extensions ex = new Extensions("a", "a", 5.0, new ArrayList<>(), "a", new NodeFeedback());
		assertEquals("a".getClass(), ex.toString().getClass());
		KnowledgeResource kr = new KnowledgeResource();
		KnowledgeResource kr1 = new KnowledgeResource("a", "a", "a");
		kr.setDescription("a");
		kr.setId("a");
		kr.setName("a");
		assertEquals("a".getClass(), kr1.toString().getClass());
		Association ar = new Association();
		Association ar1 = new Association("a", "a", "a");
		ar.setLabel("a");
		ar.setSource("a");
		ar.setTarget("a");
		assertEquals("a".getClass(), ar1.toString().getClass());
		Mapping m = new Mapping();
		Mapping m1 = new Mapping("a", "a");
		m.setChild("a");
		m.setParent("a");
		assertEquals("a".getClass(), m1.toString().getClass());
		ConfigurationProfile c = new ConfigurationProfile();
		ConfigurationProfile c1 = new ConfigurationProfile("a", new ArrayList<>());
		c.setMappings(new ArrayList<>());
		c.setProfileName("a");
		assertEquals("a".getClass(), c1.toString().getClass());
		ErrorResponse e = new ErrorResponse();
		ErrorResponse e1 = new ErrorResponse("a", "a");
		e.setError("a");
		e.setErrorDescription("a");
		assertEquals("a".getClass(), e1.toString().getClass());
		NodeMapping nm = new NodeMapping();
		NodeMapping nm1 = new NodeMapping(null, "POSITION", "PID001", "ROLE", "RID001", new ArrayList<>(),
				"UNVERIFIED");
		nm.setChild("a");
		nm.setChildIds(new ArrayList<>());
		nm.setParent("a");
		nm.setParentId("a");
		nm.getChild();
		nm.getChildId();
		nm.getChildIds();
		nm.getId();
		nm.getParent();
		nm.getParentId();
		nm.getStatus();
		assertEquals("a".getClass(), nm1.toString().getClass());
		DataNodeVerification dv = new DataNodeVerification();
		DataNodeVerification dv1 = new DataNodeVerification("a", true, "userType");
		dv.setUser("a");
		dv.setUpdatedDate("date");
		dv.setVerified(true);
		dv1.getActive();
		dv1.getAdditionalProperties();
		dv1.getBookmark();
		dv1.getChildCount();
		dv1.getChildren();
		dv1.getCreatedBy();
		dv1.getCreatedDate();
		dv1.getDepartment();
		dv1.getDescription();
		dv1.getId();
		dv1.getLevel();
		dv1.getName();
		dv1.getReviewComments();
		dv1.getReviewedBy();
		dv1.getReviewedDate();
		dv1.getSecondaryReviewComments();
		dv1.getSecondaryStatus();
		dv1.getSimilarities();
		dv1.getSource();
		dv1.getStatus();
		dv1.getType();
		dv1.getUpdatedBy();
		dv1.getUpdatedDate();
		dv1.getUser();
		dv1.getUserType();
		dv1.getVerified();
		assertEquals("a".getClass(), dv1.toString().getClass());
		DataNodesVerificationView dvv = new DataNodesVerificationView();
		DataNodesVerificationView dvv1 = new DataNodesVerificationView(new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>());
		dvv.setRejected(new ArrayList<>());
		dvv.setUnverified(new ArrayList<>());
		dvv.setVerified(new ArrayList<>());
		assertEquals("a".getClass(), dvv1.toString().getClass());
		NodeLogs nl = new NodeLogs();
		NodeLogs nl1 = new NodeLogs("a", "a", "a", "a", "a", "a", new DataNode(), new DataNode(), "a", "a", "a");
		nl.setId("a");
		nl.setChangedStage(new DataNode());
		nl.setChangeStatement("a");
		nl.setComments("a");
		nl.setEarlierStage(new DataNode());
		nl.setType("a");
		nl.setReference("a");
		nl.setUpdatedBy("a");
		nl.setUpdatedByEmail("a");
		nl.setUpdatedDate("a");
		nl.setUser("a");
		nl.setEarlierStage(new DataNode());
		assertEquals("a".getClass(), nl1.toString().getClass());

		RequestDate rd2 = new RequestDate();
		RequestDate rd3 = new RequestDate("targetDate", "startDate", "endDate", "interval");
		rd2.setEndDate("endDate");
		rd2.setInterval("interval");
		rd2.setStartDate("startDate");
		rd2.setTargetDate("targetDate");
		assertEquals("a".getClass(), rd3.toString().getClass());

		Plot plot = new Plot();
		Plot plot1 = new Plot("name", new Object(), "symbol");
		Plot plot2 = new Plot("name", new Object(), "symbol", "headerLabel", "valueLabel");
		Plot plot3 = new Plot("name", new Object(), "symbol", "parentName", "headerLabel", "valueLabel");
		Plot plot4 = new Plot("name", new Object(), "symbol", "parentName", "headerLabel", "valueLabel", "parentLabel");
		Plot plot5 = new Plot(new Object(), "name", new Object(), "valueLabel", "symbol", "parentName", "parentLabel");
		plot.setLabel(new Object());
		plot.setName("name");
		plot.setParentLabel("parentLabel");
		plot.setParentName("parentName");
		plot.setSymbol("symbol");
		plot.setValue(new Object());
		plot.setValueLabel("valueLabel");
		assertEquals("a".getClass(), plot1.toString().getClass());

		Data data = new Data();
		Data data1 = new Data("name", new Object(), "symbol");
		Data data2 = new Data("name", new Object(), "symbol", new ArrayList<>());
		Data data3 = new Data("headerName", new Object(), "headerSymbol", "colorPaletteCode", 1L, new ArrayList<>(),
				new InsightsWidget(), true);
		data.setColorPaletteCode("colorPaletteCode");
		data.setColorPaletteId(1L);
		data.setHeaderName("headerName");
		data.setHeaderSymbol("headerSymbol");
		data.setHeaderValue(new Object());
		data.setInsight(new InsightsWidget());
		data.setIsDecimal(true);
		data.setPlots(new ArrayList<>());
		assertEquals("a".getClass(), data1.toString().getClass());

		InsightsWidget in = new InsightsWidget();
		InsightsWidget in1 = new InsightsWidget("name", new Object(), "indicator", "colorCode");
		in.setColorCode("colorCode");
		in.setIndicator("indicator");
		in.setName("name");
		in.setValue(new Object());
		assertEquals("a".getClass(), in1.toString().getClass());

		AggregateDto ag = new AggregateDto();
		ag.setChartFormat("chartFormat");
		ag.setChartType(null);
		ag.setCustomData(new HashMap<>());
		ag.setData(new ArrayList<>());
		ag.setDates(new RequestDate());
		ag.setDrillDownChartId("drillDownChartId");
		ag.setFilter(new Object());
		ag.setFilterKeys(new Object());
		ag.setVisualizationCode("visualizationCode");
		RequestDate dates = new RequestDate();
		ChartType chartType = null;
		AggregateDto ag1 = new AggregateDto(chartType, "visualizationCode", "chartFormat", "drillDownChartId",
				new Object(), new HashMap<String, Object>(), dates, new Object(), new ArrayList<>());
		assertEquals("a".getClass(), ag1.toString().getClass());

		TaggingConstants tc = new TaggingConstants();

		PathRoutes path = new PathRoutes();

		Sql sql = new Sql();

		Constants constants = new Constants();

		Notification notification = new Notification();
		notification.setConfig(new EmailConfig("a", "b"));
		notification.setDeliveryType("a");
		notification.setMode("m");
		notification.setTemplate(new Template("a", null, new TemplateParams("a", "b", "c")));
		notification.setIds(new ArrayList<>());
		Notification notification1 = new Notification("m", "d", new EmailConfig(), new ArrayList<>(), new Template());
		assertEquals("a".getClass(), notification1.toString().getClass());
		new EmailConfig().setSender("sender");
		new EmailConfig().setSubject("subject");
		Template t = new Template();
		t.setData("data");
		t.setParams(new TemplateParams());
		t.toString();

		TemplateParams pa1 = new TemplateParams();
		TemplateParams p2 = new TemplateParams("a", "b", "c");
		pa1.setMessage("a");
		pa1.setNodeId("b");
		pa1.setType("a");
		assertEquals("a".getClass(), p2.toString().getClass());

		// DataNode bd = new DataNode("a", "id", "name", "description", "status",
		// "secondaryStatus", "source", true,
		// "department", true, "level", "reviewComments", "secondaryReviewComments",
		// null, new ArrayList<>(), null,
		// new ArrayList<>(), null, "", "", "updatedDate", "updatedBy", "reviewedDate",
		// "reviewedBy");
		// assertEquals("a".getClass(), bd.toString().getClass());

		Conditions cf = new Conditions();
		Conditions cf1 = new Conditions(1L, "name", "description", new ArrayList<>());
		cf.setDescription("description");
		cf.setName("name");
		cf.setId(1L);
		cf.setSubCondition(new ArrayList<>());
		assertEquals("a".getClass(), cf.toString().getClass());

		MappingVerification mv = new MappingVerification();
		MappingVerification mv1 = new MappingVerification(true);
		mv.setChild("");
		mv.setChildId("n");
		mv.setChildIds(new ArrayList<>());
		mv.setId(1L);
		mv.setParent("parent");
		mv.setParentId("parentId");
		mv.setStatus("status");
		mv.setVerified(true);
		assertEquals("a".getClass(), mv1.toString().getClass());

		ExploreNodesMapper exn = new ExploreNodesMapper();
		exn.setActivityMap(new HashMap<>());
		exn.setCompetencyMap(new HashMap<>());
		exn.setKrMap(new HashMap<>());
		exn.setPositionMap(new HashMap<>());
		exn.setRoleMap(new HashMap<>());

		FilterMappings fm = new FilterMappings();
		fm.setId("id");
		fm.setRelation("relation");
		fm.setType("type");
		fm.toString();

		SubCondition sc3 = new SubCondition();
		SubCondition sc31 = new SubCondition(1L, "cmd");
		sc3.setCmd("cmd");
		sc3.setId(1L);
		sc3.toString();

		Entities.fromValue("ACTIVITY").toString();
		Entities.fromValue(null);

		NodeStatus.fromValue("UNVERIFIED").toString();
		NodeStatus.fromValue(null);

		NodeSource.fromValue("ISTM").toString();
		NodeSource.fromValue(null);

	}

}