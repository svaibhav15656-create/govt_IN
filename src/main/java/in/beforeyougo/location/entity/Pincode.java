package in.beforeyougo.location.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Maps a pincode to a village or district.
 * Data sourced from the LGD "villages with PIN codes" dataset.
 */
@Entity
@Table(name = "pincodes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"pincode", "village_code"})
})
public class Pincode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pincode", nullable = false, length = 6)
    private String pincode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "village_code")
    private Village village;

    @Column(name = "village_code", insertable = false, updatable = false)
    private Integer villageCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code")
    private District district;

    @Column(name = "district_code", insertable = false, updatable = false)
    private Integer districtCode;

    @Column(name = "source")
    private String source;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Pincode() {}

    public Pincode(String pincode, Village village, String source) {
        this.pincode = pincode;
        this.village = village;
        this.villageCode = village != null ? village.getLgdCode() : null;
        this.source = source;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Village getVillage() { return village; }
    public void setVillage(Village village) {
        this.village = village;
        this.villageCode = village != null ? village.getLgdCode() : null;
    }

    public Integer getVillageCode() { return villageCode; }

    public District getDistrict() { return district; }
    public void setDistrict(District district) {
        this.district = district;
        this.districtCode = district != null ? district.getLgdCode() : null;
    }

    public Integer getDistrictCode() { return districtCode; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
