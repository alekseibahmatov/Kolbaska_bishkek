package ee.kolbaska.kolbaska.model.baseentity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class DefaultModel extends TimeControl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
