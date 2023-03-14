package ynzmz.server.Board.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Board {

    @Id @GeneratedValue
    long boardId;
    @Column
    String title;

    String contentBody;
    LocalDateTime createdAt;
    LocalDateTime modifiedAt;
    int view_count;
    int vote_count;
    long memberId;
    long boardCommentId;
    long boardVoteId;

}
