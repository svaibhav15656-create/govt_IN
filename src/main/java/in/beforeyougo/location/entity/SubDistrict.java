package in.beforeyougo.location.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a sub-district (taluk, tehsil, block, mandal, etc.) within a district.
 * Data sourced from the Local Government Directory (LGD).
 */
@Entity
@Table(name = "sub_districts")
public class SubDistrict {

    @Id
    @Column(name = "lgd_code")
    private Integer lgdCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code", nullable = false)
    private District district;

    @Column(name = "district_code", insertable = false, updatable = false)
    private Integer districtCode;

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

    public SubDistrict() {}

    public SubDistrict(Integer lgdCode, District district, String nameEn, String nameLocal) {
        this.lgdCode = lgdCode;
        this.district = district;
        this.districtCode = district.getLgdCode();
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

    public District getDistrict() { return district; }
    public void setDistrict(District district) {
        this.district = district;
        this.districtCode = district != null ? district.getLgdCode() : null;
    }

    public Integer getDistrictCode() { return districtCode; }

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
