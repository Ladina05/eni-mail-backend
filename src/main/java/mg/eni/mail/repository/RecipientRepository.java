package mg.eni.mail.repository;
import mg.eni.mail.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface RecipientRepository extends JpaRepository<Recipient, UUID> {
    List<Recipient> findByUserEmailIgnoreCaseAndDeletedFalseAndArchivedFalseOrderByMessageSentAtDesc(String email);
    List<Recipient> findByUserEmailIgnoreCaseAndDeletedFalseOrderByMessageSentAtDesc(String email);
    List<Recipient> findByUserEmailIgnoreCaseAndArchivedTrueAndDeletedFalseOrderByMessageSentAtDesc(String email);
    List<Recipient> findByUserEmailIgnoreCaseAndDeletedTrueOrderByMessageSentAtDesc(String email);
    Optional<Recipient> findByMessageIdAndUserEmailIgnoreCase(UUID messageId, String email);
    boolean existsByMessageIdAndUserEmailIgnoreCase(UUID messageId, String email);
    void deleteByMessageIdAndUserEmailIgnoreCase(UUID messageId, String email);
    void deleteByUserEmailIgnoreCaseAndDeletedTrue(String email);
}
