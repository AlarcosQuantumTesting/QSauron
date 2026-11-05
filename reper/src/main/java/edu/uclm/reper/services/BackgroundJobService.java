package edu.uclm.reper.services;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class BackgroundJobService {

    private static final Logger log = LoggerFactory.getLogger(BackgroundJobService.class);

    public enum JobStatus {
        QUEUED, RUNNING, DONE, FAILED
    }

    public static class JobInfo {
        private final String id;
        private final String kind;
        private final String ownerEmail;
        private volatile JobStatus status;
        private volatile String error;
        private final Instant createdAt;
        private volatile Instant finishedAt;
        private volatile Object result;

        public JobInfo(String id, String kind, String ownerEmail, Instant createdAt) {
            this.id = id;
            this.kind = kind;
            this.ownerEmail = ownerEmail;
            this.status = JobStatus.QUEUED;
            this.createdAt = createdAt;
        }

        public String getId() { return id; }
        public String getKind() { return kind; }
        public String getOwnerEmail() { return ownerEmail; }
        public JobStatus getStatus() { return status; }
        public String getError() { return error; }
        public Instant getCreatedAt() { return createdAt; }
        public Instant getFinishedAt() { return finishedAt; }
        public Object getResult() { return result; }

        public void setStatus(JobStatus status) { this.status = status; }
        public void setError(String error) { this.error = error; }
        public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
        public void setResult(Object result) { this.result = result; }
    }

    private final ExecutorService executor;
    private final ConcurrentHashMap<String, JobInfo> jobs = new ConcurrentHashMap<>();
    private final long ttlMinutes;
    private Clock clock;

    public BackgroundJobService() {
        this(2, 30);
    }

    public BackgroundJobService(
            @Value("${app.jobs.workers:2}") int workers,
            @Value("${app.jobs.ttl-minutes:30}") long ttlMinutes) {
        this(workers, ttlMinutes, Clock.systemUTC());
    }

    public BackgroundJobService(int workers, long ttlMinutes, Clock clock) {
        this.ttlMinutes = ttlMinutes;
        this.clock = clock;
        AtomicInteger threadCount = new AtomicInteger(1);
        this.executor = Executors.newFixedThreadPool(workers, r -> {
            Thread t = new Thread(r, "reper-job-" + threadCount.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down BackgroundJobService executor");
        executor.shutdownNow();
    }

    public String submit(String kind, String ownerEmail, Callable<Object> task) {
        cleanupExpiredJobs();
        String jobId = UUID.randomUUID().toString();
        JobInfo job = new JobInfo(jobId, kind, ownerEmail, clock.instant());
        jobs.put(jobId, job);

        executor.submit(() -> {
            job.setStatus(JobStatus.RUNNING);
            try {
                Object res = task.call();
                job.setResult(res);
                job.setStatus(JobStatus.DONE);
            } catch (Throwable t) {
                log.error("Job {} of kind {} failed: {}", jobId, kind, t.getMessage(), t);
                String msg = t.getMessage();
                job.setError((msg != null && !msg.isBlank()) ? msg : t.getClass().getSimpleName());
                job.setStatus(JobStatus.FAILED);
            } finally {
                job.setFinishedAt(clock.instant());
            }
        });

        return jobId;
    }

    public JobInfo get(String jobId) {
        cleanupExpiredJobs();
        return jobs.get(jobId);
    }

    public Object getResult(String jobId) {
        JobInfo job = get(jobId);
        return job != null ? job.getResult() : null;
    }

    private void cleanupExpiredJobs() {
        Instant now = clock.instant();
        Duration ttl = Duration.ofMinutes(ttlMinutes);
        jobs.entrySet().removeIf(entry -> {
            JobInfo job = entry.getValue();
            Instant finished = job.getFinishedAt();
            return finished != null && Duration.between(finished, now).compareTo(ttl) > 0;
        });
    }
}
