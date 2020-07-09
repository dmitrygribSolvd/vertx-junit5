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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
@DisplayName("Test multiple @BeforeEach methods")
public class AsyncBeforeEachTest {
	
	private static final Logger LOGGER = LogManager.getLogger(AsyncBeforeEachTest.class);
	
	private boolean started1;
	private boolean started2;
	private final AtomicInteger count = new AtomicInteger();

	@BeforeEach
	void before1(VertxTestContext context, Vertx vertx) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in @BeforeEach before1.");
		int c = count.get();
		boolean s = started2;
		if (c == 1) {
			assertTrue(s);
		}
		Checkpoint checkpoint = context.checkpoint();
		vertx.setTimer(20, id -> {
			started1 = true;
			count.incrementAndGet();
			checkpoint.flag();
		});
	}

	@BeforeEach
	void before2(VertxTestContext context, Vertx vertx) {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info in @BeforeEach before2.");
		int c = count.get();
		boolean s = started1;
		if (c == 1) {
			assertTrue(s);
		}
		Checkpoint checkpoint = context.checkpoint();
		vertx.setTimer(20, id -> {
			started2 = true;
			count.incrementAndGet();
			checkpoint.flag();
		});
	}

	@RepeatedTest(10)
	void check_async_before_completed() {
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info at the beginning of the @RepeatedTest test.");
		assertEquals(2, count.get());
		assertTrue(started1);
		assertTrue(started2);
		LOGGER.info("Log4j2 (org.apache.logging.log4j): Test info at the ending of the @RepeatedTest test.");
	}
}
