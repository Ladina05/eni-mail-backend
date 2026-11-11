package mg.eni.mail.repository;
import mg.eni.mail.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByCreatedByEmailIgnoreCase(String email);
    List<Group> findByCreatedById(UUID userId);
}
