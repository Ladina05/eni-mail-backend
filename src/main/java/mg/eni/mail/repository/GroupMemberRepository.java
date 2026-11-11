package mg.eni.mail.repository;
import mg.eni.mail.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
    List<GroupMember> findByGroupId(UUID groupId);
    void deleteByGroupIdAndUserId(UUID groupId, UUID userId);
    void deleteByGroupId(UUID groupId);
    void deleteByUserId(UUID userId);
}
