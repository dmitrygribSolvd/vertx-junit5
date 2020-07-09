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

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Unit tests for VertxTestContext")
public class VertxTestContextTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(VertxTestContextTest.class);

	@Test
	@DisplayName("Check that failing with a null exception is forbidden")
	void fail_with_null() {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThatThrownBy(() -> context.failNow(null)).isInstanceOf(NullPointerException.class)
				.hasMessage("The exception cannot be null");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check the behavior of succeeding() and that it does not complete the test context")
	void check_async_assert() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.succeeding().handle(Future.succeededFuture());
		assertThat(context.awaitCompletion(1, TimeUnit.MILLISECONDS)).isFalse();
		assertThat(context.completed()).isFalse();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		context = new VertxTestContext();
		context.succeeding().handle(Future.failedFuture(new RuntimeException("Plop")));
		assertThat(context.awaitCompletion(1, TimeUnit.MILLISECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(RuntimeException.class).hasMessage("Plop");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check the behavior of failing()")
	void check_async_assert_fail() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.failing().handle(Future.failedFuture("Bam"));
		context.awaitCompletion(1, TimeUnit.MILLISECONDS);
		assertThat(context.failed()).isFalse();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		context = new VertxTestContext();
		context.failing().handle(Future.succeededFuture());
		context.awaitCompletion(1, TimeUnit.MILLISECONDS);
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).hasMessage("The asynchronous result was expected to have failed");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check the behavior of succeeding(callback)")
	void check_async_assert_with_handler() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		AtomicBoolean checker = new AtomicBoolean(false);
		VertxTestContext context = new VertxTestContext();

		VertxTestContext finalContext = context;
		Handler<Object> nextHandler = obj -> {
			checker.set(true);
			finalContext.completeNow();
		};

		context.succeeding(nextHandler).handle(Future.succeededFuture());
		assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isTrue();
		assertThat(checker).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		checker.set(false);
		context = new VertxTestContext();

		context.succeeding(nextHandler).handle(Future.failedFuture(new RuntimeException("Plop")));
		assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(RuntimeException.class).hasMessage("Plop");
		assertThat(checker).isFalse();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check callback exception of succeeding(callback)")
	void check_succeeding_callback_with_exception() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		Handler<Object> nextHandler = obj -> {
			throw new RuntimeException("Boom");
		};
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		context.succeeding(nextHandler).handle(Future.succeededFuture());
		assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(RuntimeException.class).hasMessage("Boom");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check the behavior of failing(callback)")
	void check_async_assert_fail_with_handler() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		AtomicBoolean checker = new AtomicBoolean(false);
		VertxTestContext context = new VertxTestContext();

		VertxTestContext finalContext = context;
		Handler<Throwable> nextHandler = ar -> {
			checker.set(true);
			finalContext.completeNow();
		};

		context.failing(nextHandler).handle(Future.failedFuture("Bam"));
		assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isTrue();
		assertThat(checker).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		checker.set(false);
		context = new VertxTestContext();

		context.failing(nextHandler).handle(Future.succeededFuture());
		assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).hasMessage("The asynchronous result was expected to have failed");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check callback exception of failing(callback)")
	void check_failing_callback_with_exception() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		Handler<Throwable> nextHandler = throwable -> {
			throw new RuntimeException("Pow");
		};
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		context.failing(nextHandler).handle(Future.failedFuture("some failure"));
		assertThat(context.awaitCompletion(2, TimeUnit.SECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(RuntimeException.class).hasMessage("Pow");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check the behavior of verify() and no error")
	void check_verify_ok() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.verify(() -> {
			assertThat("ok").isEqualTo("ok");
			context.completeNow();
		});
		assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check the behavior of verify() with an error")
	void check_verify_fail() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.verify(() -> {
			throw new RuntimeException("Bam");
		});
		assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");

		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(RuntimeException.class).hasMessage("Bam");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check that flagging 2 checkpoints completes the test context")
	void check_checkpoint() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();

		Checkpoint a = context.checkpoint();
		Checkpoint b = context.checkpoint();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		new Thread(a::flag).start();
		new Thread(b::flag).start();
		assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check that not flagging all checkpoints ends up in a timeout")
	void checK_not_all_checkpoints_passed_timesout() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();

		Checkpoint a = context.checkpoint(2);
		context.checkpoint();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		new Thread(a::flag).start();
		new Thread(a::flag).start();
		assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isFalse();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check that flagging strict checkpoints more than expected fails the test context")
	void check_strict_checkpoint_overuse() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();

		Checkpoint a = context.checkpoint();
		Checkpoint b = context.checkpoint();
		new Thread(a::flag).start();
		new Thread(a::flag).start();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("flagged too many times");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check that a lax checkpoint can be flagged more often than required")
	void check_lax_checkpoint_no_overuse() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();

		Checkpoint a = context.laxCheckpoint();
		Checkpoint b = context.checkpoint();
		new Thread(() -> {
			a.flag();
			a.flag();
			a.flag();
			b.flag();
		}).start();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.awaitCompletion(500, TimeUnit.MILLISECONDS)).isTrue();
		assertThat(context.failed()).isFalse();
		assertThat(context.completed()).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check that failing an already completed context is not possible")
	void complete_then_fail() {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();

		context.completeNow();
		context.failNow(new IllegalStateException("Oh"));
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.completed()).isTrue();
		assertThat(context.failed()).isFalse();
		assertThat(context.causeOfFailure()).isNull();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Just fail immediately and on the test runner thread")
	void just_fail() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.failNow(new RuntimeException("Woops"));
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.failed()).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Pass a success to a succeedingThenComplete() async handler")
	void check_succeedingThenComplete_success() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.succeedingThenComplete().handle(Future.succeededFuture());
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.completed()).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Pass a failure to a succeedingThenComplete() async handler")
	void check_succeedingThenComplete_failure() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.succeedingThenComplete().handle(Future.failedFuture(new RuntimeException("Boo!")));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isFalse();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).hasMessage("Boo!");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Pass a failure to a failingThenComplete() async handler")
	void check_failingThenComplete_failure() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.failingThenComplete().handle(Future.failedFuture(new IllegalArgumentException("42")));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.failed()).isFalse();
		assertThat(context.completed()).isTrue();
		assertThat(context.causeOfFailure()).isNull();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Pass a success to a failingThenComplete() async handler")
	void check_failingThenComplete_success() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.failingThenComplete().handle(Future.succeededFuture("gold"));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isFalse();
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(AssertionError.class)
				.hasMessage("The asynchronous result was expected to have failed");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Pass future assertComplete")
	void check_future_completion() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.assertComplete(Future.succeededFuture("bla"))
				.compose(s -> context.assertComplete(Future.succeededFuture(s + "bla")))
				.onComplete(context.succeeding(res -> {
					assertThat(res).isEqualTo("blabla");
					context.completeNow();
				}));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Fail future assertComplete")
	void check_future_completion_failure() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.assertComplete(Future.succeededFuture("bla"))
				.compose(s -> context.assertComplete(Future.failedFuture(new IllegalStateException(s + "bla"))))
				.onComplete(context.succeeding(res -> {
					context.completeNow();
				}));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isFalse();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(AssertionError.class);
		assertThat(context.causeOfFailure().getCause()).isInstanceOf(IllegalStateException.class).hasMessage("blabla");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

//  @Test
//  @DisplayName("Pass future chain assertComplete")
//  void check_future_chain_completion() throws InterruptedException {
//    VertxTestContext context = new VertxTestContext();
//    context
//      .assertComplete(Future.succeededFuture("bla")
//        .compose(s -> Future.failedFuture(new IllegalStateException(s + "bla")))
//        .recover(ex -> Future.succeededFuture(ex.getMessage()))
//      )
//      .onComplete(context.succeeding(res -> {
//        assertThat(res).isEqualTo("blabla");
//        context.completeNow();
//      }));
//    assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
//    assertThat(context.completed()).isTrue();
//  }

	@Test
	@DisplayName("Fail future chain assertComplete")
	void check_future_chain_completion_failure() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.assertComplete(
				Future.succeededFuture("bla").compose(s -> Future.failedFuture(new IllegalStateException(s + "bla"))))
				.onComplete(context.succeeding(res -> {
					context.completeNow();
				}));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isFalse();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(AssertionError.class);
		assertThat(context.causeOfFailure().getCause()).isInstanceOf(IllegalStateException.class).hasMessage("blabla");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Pass future assertFailure")
	void check_future_failing() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.assertFailure(Future.failedFuture(new IllegalStateException("bla"))).recover(
				s -> context.assertFailure(Future.failedFuture(new IllegalStateException(s.getMessage() + "bla"))))
				.onComplete(context.failing(ex -> {
					assertThat(ex).isInstanceOf(IllegalStateException.class).hasMessage("blabla");
					context.completeNow();
				}));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isTrue();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Fail future assertComplete")
	void check_future_failing_failure() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.assertFailure(Future.failedFuture(new IllegalStateException("bla")))
				.recover(s -> context.assertFailure(Future.succeededFuture(s.getMessage() + "bla")))
				.onComplete(context.succeeding(res -> {
					context.completeNow();
				}));
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.completed()).isFalse();
		assertThat(context.failed()).isTrue();
		assertThat(context.causeOfFailure()).isInstanceOf(AssertionError.class)
				.hasMessage("Future completed with value: blabla");
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Call verify() with a block that throws an exception")
	void check_verify_with_exception() throws InterruptedException {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		context.verify(() -> {
			throw new RuntimeException("!");
		});
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		assertThat(context.awaitCompletion(1, TimeUnit.SECONDS)).isTrue();
		assertThat(context.causeOfFailure()).hasMessage("!").isInstanceOf(RuntimeException.class);
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}

	@Test
	@DisplayName("Check that unsatisfied call sites are properly identified")
	void check_unsatisifed_checkpoint_callsites() {
		LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
		VertxTestContext context = new VertxTestContext();
		Checkpoint a = context.checkpoint();
		Checkpoint b = context.checkpoint(2);

		assertThat(context.unsatisfiedCheckpointCallSites()).hasSize(2);
		LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
		a.flag();
		b.flag();
		assertThat(context.unsatisfiedCheckpointCallSites()).hasSize(1);

		StackTraceElement element = context.unsatisfiedCheckpointCallSites().iterator().next();
		assertThat(element.getClassName()).isEqualTo(VertxTestContextTest.class.getName());
		assertThat(element.getMethodName()).isEqualTo("check_unsatisifed_checkpoint_callsites");

		b.flag();
		assertThat(context.unsatisfiedCheckpointCallSites()).isEmpty();
		LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
	}
}
