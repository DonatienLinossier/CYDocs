package com.example.demo;

import com.example.demo.models.Document;
import com.example.demo.models.DocumentAcces;
import com.example.demo.models.User;
import com.example.demo.repositories.DocumentAccesRepository;
import com.example.demo.repositories.DocumentRepository;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.DocumentAccesService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentAccesServiceTest {

    @Test
    void shareByEmail_whenRequesterIsNotOwner_shouldThrow() {
        DocumentRepository docRepo = mock(DocumentRepository.class);
        DocumentAccesRepository accesRepo = mock(DocumentAccesRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        DocumentAccesService service = new DocumentAccesService(accesRepo, userRepo, docRepo);

        Document doc = new Document();
        doc.setId(10L);
        doc.setOwnerId(1L);

        when(docRepo.findById(10L)).thenReturn(Optional.of(doc));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            service.shareByEmail(10L, "target@test.com", "read", 2L)
        );

        assertTrue(ex.getMessage().toLowerCase().contains("propri"));
    }

    @Test
    void shareByEmail_whenOwner_shouldSaveAccess() {
        DocumentRepository docRepo = mock(DocumentRepository.class);
        DocumentAccesRepository accesRepo = mock(DocumentAccesRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        DocumentAccesService service = new DocumentAccesService(accesRepo, userRepo, docRepo);

        Document doc = new Document();
        doc.setId(20L);
        doc.setOwnerId(5L);

        User target = new User();
        target.setId(7L);
        target.setEmail("target@example.com");

        when(docRepo.findById(20L)).thenReturn(Optional.of(doc));
        when(userRepo.findByEmail("target@example.com")).thenReturn(Optional.of(target));
        when(accesRepo.findByDocumentIdAndUserId(20L, 7L)).thenReturn(Optional.empty());

        service.shareByEmail(20L, "target@example.com", "write", 5L);

        verify(accesRepo, times(1)).save(argThat(a ->
            a.getDocumentId().equals(20L) && a.getUserId().equals(7L) && a.getAccessType().equals("write")
        ));
    }

    @Test
    void getUserPermission_shouldReturnAccessType() {
        DocumentRepository docRepo = mock(DocumentRepository.class);
        DocumentAccesRepository accesRepo = mock(DocumentAccesRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        DocumentAccesService service = new DocumentAccesService(accesRepo, userRepo, docRepo);

        DocumentAcces acc = new DocumentAcces();
        acc.setDocumentId(30L);
        acc.setUserId(9L);
        acc.setAccessType("write");

        when(accesRepo.findByDocumentIdAndUserId(30L, 9L)).thenReturn(Optional.of(acc));

        assertEquals("write", service.getUserPermission(30L, 9L));
    }

    @Test
    void revokeAccess_whenOwner_shouldDeleteAccess() {
        DocumentRepository docRepo = mock(DocumentRepository.class);
        DocumentAccesRepository accesRepo = mock(DocumentAccesRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        DocumentAccesService service = new DocumentAccesService(accesRepo, userRepo, docRepo);

        Document doc = new Document();
        doc.setId(40L);
        doc.setOwnerId(11L);

        DocumentAcces acc = new DocumentAcces();
        acc.setDocumentId(40L);
        acc.setUserId(12L);
        acc.setAccessType("read");

        when(docRepo.findById(40L)).thenReturn(Optional.of(doc));
        when(accesRepo.findByDocumentIdAndUserId(40L, 12L)).thenReturn(Optional.of(acc));

        service.revokeAccess(40L, 12L, 11L);

        verify(accesRepo, times(1)).delete(acc);
    }

    @Test
    void getCollaborators_shouldReturnNonOwnerCollaborators() {
        DocumentRepository docRepo = mock(DocumentRepository.class);
        DocumentAccesRepository accesRepo = mock(DocumentAccesRepository.class);
        UserRepository userRepo = mock(UserRepository.class);

        DocumentAccesService service = new DocumentAccesService(accesRepo, userRepo, docRepo);

        DocumentAcces ownerAcc = new DocumentAcces();
        ownerAcc.setDocumentId(50L);
        ownerAcc.setUserId(21L);
        ownerAcc.setAccessType("owner");

        DocumentAcces readerAcc = new DocumentAcces();
        readerAcc.setDocumentId(50L);
        readerAcc.setUserId(22L);
        readerAcc.setAccessType("read");

        when(accesRepo.findByDocumentId(50L)).thenReturn(List.of(ownerAcc, readerAcc));

        User u22 = new User();
        u22.setId(22L);
        u22.setEmail("r@example.com");
        u22.setFirstName("Read");
        u22.setLastName("User");

        when(userRepo.findById(22L)).thenReturn(Optional.of(u22));

        List<?> collabs = service.getCollaborators(50L);

        assertEquals(1, collabs.size());
    }

}
