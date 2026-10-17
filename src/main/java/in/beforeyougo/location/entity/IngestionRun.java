package in.beforeyougo.location.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tracks each data import run for auditing and idempotency.
 */
@Entity
@Table(name = "ingestion_runs")
public class IngestionRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_type", nullable = false, length = 100)
    private String runType;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "RUNNING";

    @Column(name = "records_processed")
    private Integer recordsProcessed = 0;

    @Column(name = "records_skipped")
    private Integer recordsSkipped = 0;

    @Column(name = "source_file", length = 500)
    private String sourceFile;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    public IngestionRun() {}

    public IngestionRun(String runType, String sourceFile) {
        this.runType = runType;
        this.sourceFile = sourceFile;
    }

    public void markCompleted(int processed, int skipped) {
        this.status = "COMPLETED";
        this.recordsProcessed = processed;
        this.recordsSkipped = skipped;
        this.completedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
        this.completedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRunType() { return runType; }
    public void setRunType(String runType) { this.runType = runType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRecordsProcessed() { return recordsProcessed; }
    public void setRecordsProcessed(Integer recordsProcessed) { this.recordsProcessed = recordsProcessed; }

    public Integer getRecordsSkipped() { return recordsSkipped; }
    public void setRecordsSkipped(Integer recordsSkipped) { this.recordsSkipped = recordsSkipped; }

    public String getSourceFile() { return sourceFile; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
