package com.adm.lucas.microblog.domain.comment;

import com.adm.lucas.microblog.domain.note.Note;
import com.adm.lucas.microblog.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "comments")
@NoArgsConstructor
@Data
@JsonIgnoreProperties({"user", "note"})
@ToString(exclude = {"user", "note"})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "user_id")
    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;

    @JoinColumn(name = "note_id")
    @ManyToOne
    private Note note;

    private Instant createdAt = Instant.now();

    private Instant updatedAt;

    private String text;

    private boolean modified = false;

    public Comment(User user, Note note, String text) {
        this.user = user;
        this.note = note;
        this.text = text;
    }

}