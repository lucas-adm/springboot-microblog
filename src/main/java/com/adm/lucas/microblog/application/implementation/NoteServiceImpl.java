package com.adm.lucas.microblog.application.implementation;

import com.adm.lucas.microblog.application.dto.request.note.CreateNoteREQ;
import com.adm.lucas.microblog.domain.note.Note;
import com.adm.lucas.microblog.domain.note.NoteRepository;
import com.adm.lucas.microblog.domain.note.NoteService;
import com.adm.lucas.microblog.domain.tag.Tag;
import com.adm.lucas.microblog.domain.tag.TagRepository;
import com.adm.lucas.microblog.domain.user.User;
import com.adm.lucas.microblog.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final NoteRepository repository;

    private void validateAccess(UUID idFromToken, Note note) {
        if (!Objects.equals(idFromToken, note.getUser().getId())) {
            throw new AccessDeniedException("Usuário sem permissão.");
        }
    }

    private void deleteNoteAndFlush(Note note) {
        repository.delete(note);
        repository.flush();
    }

    private void removeOrphanTags(List<String> names) {
        List<Tag> tags = tagRepository.findAllByNameIn(names);
        tags.stream().filter(tag -> tag.getNotes().isEmpty()).forEach(tagRepository::delete);
    }

    private List<Tag> findOrCreateTags(List<String> tags) {
        return new ArrayList<>(tags.stream().map(tag -> tagRepository.findByName(tag).orElseGet(() -> new Tag(tag.toLowerCase()))).toList());
    }

    private void changeField(UUID idFromToken, UUID idFromPath, Consumer<Note> setter) {
        Note note = repository.findById(idFromPath).orElseThrow(EntityNotFoundException::new);
        validateAccess(idFromToken, note);
        setter.accept(note);
        note.setModifiedAt(Instant.now());
        note.setModified(true);
        repository.saveAndFlush(note);
    }

    @Override
    public Note mapToNote(UUID idFromToken, CreateNoteREQ req) {
        User user = userRepository.findById(idFromToken).orElseThrow(EntityNotFoundException::new);
        List<Tag> tags = findOrCreateTags(req.tags());
        return new Note(user, req.title(), req.markdown(), req.closed(), req.hidden(), tags);
    }

    @Override
    public Note create(Note note) {
        return repository.save(note);
    }

    @Override
    public void edit(UUID idFromToken, UUID idFromPath, String title, List<String> tags, boolean closed, boolean hidden) {
        changeField(idFromToken, idFromPath, note -> {
            List<String> oldTags = note.getTags().stream().map(Tag::getName).toList();
            note.setTitle(title);
            note.setTags(findOrCreateTags(tags));
            note.setClosed(closed);
            note.setHidden(hidden);
            removeOrphanTags(oldTags);
        });
    }

    @Override
    public void changeTitle(UUID idFromToken, UUID idFromPath, String title) {
        changeField(idFromToken, idFromPath, note -> note.setTitle(title));
    }

    @Override
    public void changeMarkdown(UUID idFromToken, UUID idFromPath, String markdown) {
        changeField(idFromToken, idFromPath, note -> note.setMarkdown(markdown));
    }

    @Override
    public void changeClosed(UUID idFromToken, UUID idFromPath) {
        changeField(idFromToken, idFromPath, note -> note.setClosed(!note.isClosed()));
    }

    @Override
    public void changeHidden(UUID idFromToken, UUID idFromPath) {
        changeField(idFromToken, idFromPath, note -> note.setHidden(!note.isHidden()));
    }

    @Override
    public void changeTags(UUID idFromToken, UUID idFromPath, List<String> tags) {
        changeField(idFromToken, idFromPath, note -> {
            List<String> oldTagNames = note.getTags().stream().map(Tag::getName).toList();
            note.setTags(findOrCreateTags(tags));
            removeOrphanTags(oldTagNames);
        });
    }

    @Override
    public void delete(UUID idFromToken, UUID idFromPath) {
        Note note = repository.findById(idFromPath).orElseThrow(EntityNotFoundException::new);
        validateAccess(idFromToken, note);
        List<String> oldTagNames = note.getTags().stream().map(Tag::getName).toList();
        deleteNoteAndFlush(note);
        removeOrphanTags(oldTagNames);
    }

}