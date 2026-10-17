package in.beforeyougo.location.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a village or town within a sub-district.
 * Data sourced from the Local Government Directory (LGD).
 */
@Entity
@Table(name = "villages")
public class Village {

    @Id
    @Column(name = "lgd_code")
    private Integer lgdCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_district_code", nullable = false)
    private SubDistrict subDistrict;

    @Column(name = "sub_district_code", insertable = false, updatable = false)
    private Integer subDistrictCode;

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

    public Village() {}

    public Village(Integer lgdCode, SubDistrict subDistrict, String nameEn, String nameLocal) {
        this.lgdCode = lgdCode;
        this.subDistrict = subDistrict;
        this.subDistrictCode = subDistrict.getLgdCode();
        this.nameEn = nameEn;
        this.nameLocal = nameLocal;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Integer getLgdCode() { return lgdCode; }
    public void setLgdCode(Integer lgdCode) { this.lgdCode = lgdCode; }

    public SubDistrict getSubDistrict() { return subDistrict; }
    public void setSubDistrict(SubDistrict subDistrict) {
        this.subDistrict = subDistrict;
        this.subDistrictCode = subDistrict != null ? subDistrict.getLgdCode() : null;
    }

    public Integer getSubDistrictCode() { return subDistrictCode; }

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
