package ee.kolbaska.kolbaska.model.baseentity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public class TimeControl {
    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private Instant deletedAt;

    @Column(
            name = "deleted",
            columnDefinition = "boolean default false",
            nullable = false
    )
    private boolean deleted;

    @PreUpdate
    public void updateDeletedAt() {
        if (isDeleted() && getDeletedAt() == null) {
            setDeletedAt(Instant.now());
        }
    }
}
