package ynzmz.server.s3.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class S3File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long s3FileId;
    private String fileUrl;
    private String dbTableName;
    private Long idOfTable;
    @CreatedDate
    private LocalDateTime uploadedAt;
    @Enumerated(value = EnumType.STRING)
    private Status status;

    public enum Status{
        TEMP,
        ACTIVE
    }

}
