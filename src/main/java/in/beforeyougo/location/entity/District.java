package in.beforeyougo.location.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a district within a state.
 * Data sourced from the Local Government Directory (LGD).
 */
@Entity
@Table(name = "districts")
public class District {

    @Id
    @Column(name = "lgd_code")
    private Integer lgdCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_code", nullable = false)
    private State state;

    @Column(name = "state_code", insertable = false, updatable = false)
    private Integer stateCode;

    @Column(name = "name_en", nullable = false)
    private String nameEn;

    @Column(name = "name_local")
    private String nameLocal;

    @Column(name = "census_2011_code")
    private String census2011Code;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public District() {}

    public District(Integer lgdCode, State state, String nameEn, String nameLocal) {
        this.lgdCode = lgdCode;
        this.state = state;
        this.stateCode = state.getLgdCode();
        this.nameEn = nameEn;
        this.nameLocal = nameLocal;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // --- Getters and Setters ---

    public Integer getLgdCode() { return lgdCode; }
    public void setLgdCode(Integer lgdCode) { this.lgdCode = lgdCode; }

    public State getState() { return state; }
    public void setState(State state) {
        this.state = state;
        this.stateCode = state != null ? state.getLgdCode() : null;
    }

    public Integer getStateCode() { return stateCode; }

    public String getNameEn() { return nameEn; }
    public void setNameEn(String nameEn) { this.nameEn = nameEn; }

    public String getNameLocal() { return nameLocal; }
    public void setNameLocal(String nameLocal) { this.nameLocal = nameLocal; }

    public String getCensus2011Code() { return census2011Code; }
    public void setCensus2011Code(String census2011Code) { this.census2011Code = census2011Code; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
