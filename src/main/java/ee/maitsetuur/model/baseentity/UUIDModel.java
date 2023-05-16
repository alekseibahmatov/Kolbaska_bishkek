package ee.maitsetuur.model.baseentity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public class UUIDModel extends TimeControl {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
}
