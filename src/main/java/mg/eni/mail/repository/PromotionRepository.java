package mg.eni.mail.repository;
import mg.eni.mail.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface PromotionRepository extends JpaRepository<Promotion, UUID> {
    boolean existsByNameIgnoreCase(String name);
}
