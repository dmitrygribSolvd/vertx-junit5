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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.apache.log4j.Logger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Unit test for CountingCheckpoint")
public class CountingCheckpointTest {
	private static final Logger LOGGER = Logger.getLogger(CountingCheckpointTest.class);

	@Test
	@DisplayName("Smoke tests")
	void smoke_test() {
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the beginning of the test.");
		AtomicBoolean success = new AtomicBoolean(false);
		AtomicReference<Checkpoint> witness = new AtomicReference<>();
		Consumer<Checkpoint> consumer = c -> {
			success.set(true);
			witness.set(c);
		};
		CountingCheckpoint checkpoint = CountingCheckpoint.laxCountingCheckpoint(consumer, 3);

		checkpoint.flag();
		assertThat(success).isFalse();
		assertThat(witness).hasValue(null);
		assertThat(checkpoint.satisfied()).isFalse();

		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info in the middle of the test execution.");

		checkpoint.flag();
		assertThat(success).isFalse();
		assertThat(witness).hasValue(null);
		assertThat(checkpoint.satisfied()).isFalse();

		checkpoint.flag();
		assertThat(success).isTrue();
		assertThat(witness).hasValue(checkpoint);
		assertThat(checkpoint.satisfied()).isTrue();
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the ending of the test.");
	}

	private static final Consumer<Checkpoint> NOOP = c -> {
	};

	@Test
	@DisplayName("Refuse null triggers")
	void refuse_null_triggers() {
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the beginning of the test.");
		assertThrows(NullPointerException.class, () -> CountingCheckpoint.laxCountingCheckpoint(null, 1));
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info in the middle of the test execution.");
		assertThrows(NullPointerException.class, () -> CountingCheckpoint.strictCountingCheckpoint(v -> {
		}, null, 1));
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Refuse having 0 expected passes")
	void refuse_zero_passes() {
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the beginning of the test.");
		assertThrows(IllegalArgumentException.class, () -> CountingCheckpoint.laxCountingCheckpoint(NOOP, 0));
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Refuse having negative expected passes")
	void refuse_negative_passes() {
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the beginning of the test.");
		assertThrows(IllegalArgumentException.class, () -> CountingCheckpoint.laxCountingCheckpoint(NOOP, -1));
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check of a lax checkpoint")
	void check_lax_checkpoint() {
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the beginning of the test.");
		CountingCheckpoint checkpoint = CountingCheckpoint.laxCountingCheckpoint(NOOP, 1);
		checkpoint.flag();
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info in the middle of the test execution.");
		checkpoint.flag();
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check of a strict checkpoint")
	void check_strict_checkpoint() {
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the beginning of the test.");
		AtomicReference<Throwable> box = new AtomicReference<>();
		CountingCheckpoint checkpoint = CountingCheckpoint.strictCountingCheckpoint(NOOP, box::set, 1);

		assertThat(checkpoint.satisfied()).isFalse();
		checkpoint.flag();
		assertThat(box).hasValue(null);
		assertThat(checkpoint.satisfied()).isTrue();
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info in the middle of the test execution.");
		checkpoint.flag();
		assertThat(box.get()).isNotNull().isInstanceOf(IllegalStateException.class)
				.hasMessage("Strict checkpoint flagged too many times");
		assertThat(checkpoint.satisfied()).isTrue();
		LOGGER.info("Log4j (org.apache.log4j.Logger): Test info at the ending of the test.");
	}
}
