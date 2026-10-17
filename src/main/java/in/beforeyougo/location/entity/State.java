package in.beforeyougo.location.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a State or Union Territory of India.
 * Data sourced from the Local Government Directory (LGD).
 */
@Entity
@Table(name = "states")
public class State {

    @Id
    @Column(name = "lgd_code")
    private Integer lgdCode;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_local")
    private String nameLocal;

    /**
     * STATE or UT (Union Territory).
     */
    @Column(name = "kind", nullable = false)
    private String kind = "STATE";

    @Column(name = "census_2011_code")
    private String census2011Code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public State() {}

    public State(Integer lgdCode, String nameEn, String nameLocal, String kind) {
        this.lgdCode = lgdCode;
        this.nameEn = nameEn;
        this.nameLocal = nameLocal;
        this.kind = kind;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Integer getLgdCode() { return lgdCode; }
    public void setLgdCode(Integer lgdCode) { this.lgdCode = lgdCode; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameLocal() { return nameLocal; }
    public void setNameLocal(String nameLocal) { this.nameLocal = nameLocal; }

    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }

    public String getCensus2011Code() { return census2011Code; }
    public void setCensus2011Code(String census2011Code) { this.census2011Code = census2011Code; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
