/*
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Test the injection in a complete lifecycle (esp., with @BeforeEach)")
@ExtendWith(VertxExtension.class)
public class VertxExtensionCompleteLifecycleInjectionTest {

	private static final Logger LOGGER = LogManager.getLogger(VertxExtensionCompleteLifecycleInjectionTest.class);

	static Vertx daVertx;
	static VertxTestContext daContext;

	@BeforeAll
	static void inTheBeginning(Vertx vertx, VertxTestContext testContext) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in @BeforeAll.");
		daVertx = vertx;
		daContext = testContext;
		testContext.completeNow();
	}

	@BeforeEach
	void rightBefore(Vertx vertx, VertxTestContext testContext) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in @BeforeEach.");
		assertThat(vertx).isSameAs(daVertx);
		assertThat(testContext).isNotSameAs(daContext);
		testContext.completeNow();
	}

	@Test
	@DisplayName("Check that the Vertx instance is shared and that VertxTestContext is fresh")
	void test1(Vertx vertx, VertxTestContext testContext) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info at the beginning of the test.");
		assertThat(vertx).isSameAs(daVertx);
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in the middle of the test execution.");
		assertThat(testContext).isNotSameAs(daContext);
		testContext.completeNow();
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Same test, same assumptions")
	void test2(Vertx vertx, VertxTestContext testContext) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info at the beginning of the test.");
		assertThat(vertx).isSameAs(daVertx);
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in the middle of the test execution.");
		assertThat(testContext).isNotSameAs(daContext);
		testContext.completeNow();
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info at the ending of the test.");
	}

	@AfterEach
	void rightAfter(Vertx vertx, VertxTestContext testContext) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in @AfterEach.");
		assertThat(vertx).isSameAs(daVertx);
		assertThat(testContext).isNotSameAs(daContext);
		testContext.completeNow();
	}

	@AfterAll
	static void inTheEnd(Vertx vertx, VertxTestContext testContext) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in @AfterAll.");
		assertThat(vertx).isSameAs(daVertx);
		assertThat(testContext).isNotSameAs(daContext);
		testContext.completeNow();
	}
}
