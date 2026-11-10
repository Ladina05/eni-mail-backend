package mg.eni.mail.repository;
import mg.eni.mail.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {
    List<Attachment> findByMessageId(UUID messageId);
    Optional<Attachment> findByIdAndMessageId(UUID id, UUID messageId);
}
