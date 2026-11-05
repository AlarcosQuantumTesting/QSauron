package edu.uclm.reper.services;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import edu.uclm.reper.http.JobController;
import edu.uclm.reper.services.BackgroundJobService.JobInfo;
import edu.uclm.reper.services.BackgroundJobService.JobStatus;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackgroundJobServiceTest {

    private BackgroundJobService service;

    @BeforeEach
    void setUp() {
        service = new BackgroundJobService(2, 30);
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Test
    void testJobDoneWithResult() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String jobId = service.submit("test", "user@example.com", () -> {
            latch.countDown();
            return Map.of("key", "val");
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Poll until DONE
        long start = System.currentTimeMillis();
        JobInfo info = null;
        while (System.currentTimeMillis() - start < 5000) {
            info = service.get(jobId);
            if (info != null && info.getStatus() == JobStatus.DONE) {
                break;
            }
            Thread.sleep(50);
        }

        assertNotNull(info);
        assertEquals(JobStatus.DONE, info.getStatus());
        assertNull(info.getError());
        assertNotNull(info.getCreatedAt());
        assertNotNull(info.getFinishedAt());
        assertEquals(Map.of("key", "val"), service.getResult(jobId));
    }

    @Test
    void testJobFailedWithMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String jobId = service.submit("test", "user@example.com", () -> {
            latch.countDown();
            throw new RuntimeException("Calculation timeout");
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        long start = System.currentTimeMillis();
        JobInfo info = null;
        while (System.currentTimeMillis() - start < 5000) {
            info = service.get(jobId);
            if (info != null && info.getStatus() == JobStatus.FAILED) {
                break;
            }
            Thread.sleep(50);
        }

        assertNotNull(info);
        assertEquals(JobStatus.FAILED, info.getStatus());
        assertEquals("Calculation timeout", info.getError());
        assertNotNull(info.getFinishedAt());
        assertNull(service.getResult(jobId));
    }

    @Test
    void testJobControllerOwnerAccessAndMismatch() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        String jobId = service.submit("statistics", "owner@example.com", () -> {
            latch.countDown();
            return "stats-output";
        });

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Wait for completion
        while (service.get(jobId).getStatus() != JobStatus.DONE) {
            Thread.sleep(50);
        }

        JobController controller = new JobController();
        UserService mockUserService = mock(UserService.class);
        ReflectionTestUtils.setField(controller, "userService", mockUserService);
        ReflectionTestUtils.setField(controller, "backgroundJobService", service);

        HttpServletRequest req = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("SESSION_TOKEN", "valid-token");
        when(req.getCookies()).thenReturn(new Cookie[]{cookie});

        // Other email -> 404 NOT_FOUND
        ResponseEntity<?> notFoundStatus = controller.getJobStatus(
                Map.of("email", "other@example.com", "jobId", jobId), req);
        assertEquals(HttpStatus.NOT_FOUND, notFoundStatus.getStatusCode());

        ResponseEntity<?> notFoundResult = controller.getJobResult(
                Map.of("email", "other@example.com", "jobId", jobId), req);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResult.getStatusCode());

        // Owner email -> 200 OK
        ResponseEntity<?> okStatus = controller.getJobStatus(
                Map.of("email", "owner@example.com", "jobId", jobId), req);
        assertEquals(HttpStatus.OK, okStatus.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) okStatus.getBody();
        assertNotNull(body);
        assertEquals(jobId, body.get("id"));
        assertEquals("DONE", body.get("status"));
        assertEquals("statistics", body.get("kind"));

        ResponseEntity<?> okResult = controller.getJobResult(
                Map.of("email", "owner@example.com", "jobId", jobId), req);
        assertEquals(HttpStatus.OK, okResult.getStatusCode());
        assertEquals("stats-output", okResult.getBody());
    }

    @Test
    void testTtlCleanupWithInjectableClock() throws Exception {
        AtomicReference<Instant> currentInstant = new AtomicReference<>(Instant.parse("2026-11-05T10:00:00Z"));
        Clock testClock = new Clock() {
            @Override
            public ZoneId getZone() { return ZoneId.of("UTC"); }
            @Override
            public Clock withZone(ZoneId zone) { return this; }
            @Override
            public Instant instant() { return currentInstant.get(); }
        };

        BackgroundJobService ttlService = new BackgroundJobService(1, 30, testClock);
        try {
            CountDownLatch latch = new CountDownLatch(1);
            String jobId = ttlService.submit("cleanup", "user@example.com", () -> {
                latch.countDown();
                return "done";
            });

            assertTrue(latch.await(5, TimeUnit.SECONDS));
            while (ttlService.get(jobId).getStatus() != JobStatus.DONE) {
                Thread.sleep(50);
            }

            // Still visible after 10 minutes
            currentInstant.set(currentInstant.get().plus(Duration.ofMinutes(10)));
            assertNotNull(ttlService.get(jobId));

            // Pruned after 31 minutes (> 30 min TTL)
            currentInstant.set(currentInstant.get().plus(Duration.ofMinutes(25)));
            assertNull(ttlService.get(jobId));
        } finally {
            ttlService.shutdown();
        }
    }
}
