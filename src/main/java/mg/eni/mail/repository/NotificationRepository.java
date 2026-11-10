package mg.eni.mail.repository;
import mg.eni.mail.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findTop50ByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    long countByUserEmailIgnoreCaseAndReadFalse(String email);
    Optional<Notification> findByIdAndUserEmailIgnoreCase(UUID id, String email);
}
