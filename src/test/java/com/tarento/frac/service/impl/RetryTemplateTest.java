package com.tarento.frac.service.impl;

import static org.junit.Assert.assertEquals;

import org.apache.poi.ss.formula.functions.T;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import com.tarento.frac.utils.ApplicationProperties;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ApplicationProperties.class)
@TestPropertySource(locations = { "/application.properties" })
public class RetryTemplateTest {

	@Mock
	private RestTemplate restTemplate;

	@Spy
	@InjectMocks
	RetryTemplate rt;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void postForEntity() {
		Mockito.doReturn(null).when(restTemplate).postForEntity(Matchers.anyString(), Matchers.anyObject(),
				Matchers.anyObject());
		assertEquals(null, rt.postForEntity(Matchers.anyString(), Matchers.anyObject()));

	}

}