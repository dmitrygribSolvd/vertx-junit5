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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://julien.ponge.org/">Julien Ponge</a>
 */
@DisplayName("Integration tests")
public class IntegrationTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(IntegrationTest.class);

	static class HttpServerVerticle extends AbstractVerticle {

		@Override
		public void start(Promise<Void> startFuture) throws Exception {
			LOGGER.info("Logback (org.slf4j): Test info at the beginning of the 'start' method, HttpServerVerticle class.");
			vertx.createHttpServer().requestHandler(request -> request.response().end("Plop")).listen(8080, ar -> {
				LOGGER.info("Logback (org.slf4j): Test info in the middle of the 'start' method, HttpServerVerticle class.");
				if (ar.succeeded()) {
					startFuture.complete();
				} else {
					startFuture.fail(ar.cause());
				}
			});
			LOGGER.info("Logback (org.slf4j): Test info at the ending of the 'start' method, HttpServerVerticle class.");
		}
	}

	@Nested
	@DisplayName("Tests without parameter injection and explicit Vertx and VertxTestContext instances")
	public class Naked {

		@Test
		@DisplayName("Start a HTTP server")
		void start_http_server() throws InterruptedException {
			LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
			VertxTestContext testContext = new VertxTestContext();
			Vertx vertx = Vertx.vertx();
			vertx.createHttpServer().requestHandler(req -> req.response().end()).listen(16969,
					testContext.succeeding(ar -> testContext.completeNow()));
			LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
			assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
			closeVertx(vertx);
			LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
		}

		@Test
		@DisplayName("Start a HTTP server, then issue a HTTP client request and check the response")
		void vertx_check_http_server_response() throws InterruptedException {
			LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
			Vertx vertx = Vertx.vertx();
			VertxTestContext testContext = new VertxTestContext();

			vertx.deployVerticle(new HttpServerVerticle(), testContext.succeeding(id -> {
				HttpClient client = vertx.createHttpClient();
				client.get(8080, "localhost", "/").flatMap(HttpClientResponse::body).onFailure(testContext::failNow)
						.onSuccess(buffer -> testContext.verify(() -> {
							LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
							assertThat(buffer.toString()).isEqualTo("Plop");
							testContext.completeNow();
						}));
			}));

			assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS)).isTrue();
			closeVertx(vertx);
			LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
		}

		private void closeVertx(Vertx vertx) throws InterruptedException {
			CountDownLatch latch = new CountDownLatch(1);
			vertx.close(ar -> latch.countDown());
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}
	}

	@Nested
	@ExtendWith(VertxExtension.class)
	@DisplayName("Tests with parameter injection")
	public class WithExtension {

		@Test
		@Timeout(10_000)
		@DisplayName("Start a HTTP server, make 10 client requests, and use several checkpoints")
		void start_and_request_http_server_with_checkpoints(Vertx vertx, VertxTestContext testContext) {
			LOGGER.info("Logback (org.slf4j): Test info at the beginning of the test.");
			Checkpoint serverStarted = testContext.checkpoint();
			Checkpoint requestsServed = testContext.checkpoint(10);
			Checkpoint responsesReceived = testContext.checkpoint(10);

			vertx.createHttpServer().requestHandler(req -> {
				req.response().end("Ok");
				requestsServed.flag();
			}).listen(8080, ar -> {
				if (ar.failed()) {
					testContext.failNow(ar.cause());
				} else {
					serverStarted.flag();
				}
			});
			LOGGER.info("Logback (org.slf4j): Test info in the middle of the test execution.");
			HttpClient client = vertx.createHttpClient();
			for (int i = 0; i < 10; i++) {
				client.get(8080, "localhost", "/").flatMap(HttpClientResponse::body).onFailure(testContext::failNow)
						.onSuccess(buffer -> {
							testContext.verify(() -> assertThat(buffer.toString()).isEqualTo("Ok"));
							responsesReceived.flag();
						});
			}
			LOGGER.info("Logback (org.slf4j): Test info at the ending of the test.");
		}
	}
}
