package edu.uclm.reper.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uclm.reper.services.BackgroundJobService;
import edu.uclm.reper.services.BackgroundJobService.JobInfo;
import edu.uclm.reper.services.BackgroundJobService.JobStatus;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("jobs")
public class JobController extends BaseController {

    @Autowired
    private BackgroundJobService backgroundJobService;

    @PostMapping("/status")
    public ResponseEntity<?> getJobStatus(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String email = body != null ? body.get("email") : null;
        String jobId = body != null ? body.get("jobId") : null;
        this.validateCookie(req, email);

        JobInfo job = backgroundJobService.get(jobId);
        if (job == null || email == null || !email.equals(job.getOwnerEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Job not found"));
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", job.getId());
        resp.put("kind", job.getKind());
        resp.put("status", job.getStatus().name());
        resp.put("error", job.getError());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/result")
    public ResponseEntity<?> getJobResult(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String email = body != null ? body.get("email") : null;
        String jobId = body != null ? body.get("jobId") : null;
        this.validateCookie(req, email);

        JobInfo job = backgroundJobService.get(jobId);
        if (job == null || email == null || !email.equals(job.getOwnerEmail())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Job not found"));
        }

        if (job.getStatus() == JobStatus.QUEUED || job.getStatus() == JobStatus.RUNNING) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "Job not finished", "status", job.getStatus().name()));
        }

        if (job.getStatus() == JobStatus.FAILED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", job.getError() != null ? job.getError() : "Job failed"));
        }

        return ResponseEntity.ok(job.getResult());
    }
}
