package mg.eni.mail.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "recipients", uniqueConstraints =
    @UniqueConstraint(name = "uk_message_recipient", columnNames = {"message_id", "user_id", "type"}))
@Getter @Setter @NoArgsConstructor
public class Recipient {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RecipientType type;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private boolean starred = false;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private boolean deleted = false;
}
