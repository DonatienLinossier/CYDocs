package com.example.demo;

import com.example.demo.controllers.DocumentController;
import com.example.demo.controllers.DocumentController.ShareRequest;
import com.example.demo.models.Document;
import com.example.demo.services.DocumentAccesService;
import com.example.demo.services.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    @Test
    void share_shouldCallAccessService_whenTokenValid() {
        DocumentService docService = mock(DocumentService.class);
        DocumentAccesService accessService = mock(DocumentAccesService.class);

        DocumentController controller = new DocumentController(docService, accessService);

        when(docService.validateTokenViaActor("validToken")).thenReturn(5L);

        ShareRequest req = new ShareRequest();
        req.setDocumentId(1L);
        req.setTargetEmail("t@e.com");
        req.setAccessType("read");

        ResponseEntity<?> resp = controller.share("Bearer validToken", req);

        assertEquals(200, resp.getStatusCodeValue());
        verify(accessService, times(1)).shareByEmail(1L, "t@e.com", "read", 5L);
    }

    @Test
    void getById_ownerShouldHaveWritePermission() {
        DocumentService docService = mock(DocumentService.class);
        DocumentAccesService accessService = mock(DocumentAccesService.class);

        DocumentController controller = new DocumentController(docService, accessService);

        when(docService.validateTokenViaActor("tkn")).thenReturn(3L);

        Document doc = new Document();
        doc.setId(2L);
        doc.setOwnerId(3L);

        when(docService.getByIdDirect(2L)).thenReturn(Optional.of(doc));

        ResponseEntity<Document> resp = controller.getById(2L, "Bearer tkn");

        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertEquals("write", resp.getBody().getCurrentPermission());
    }

    @Test
    void getMyDocuments_shouldReturnDocs() {
        DocumentService docService = mock(DocumentService.class);
        DocumentAccesService accessService = mock(DocumentAccesService.class);

        DocumentController controller = new DocumentController(docService, accessService);

        Document d1 = new Document(); d1.setId(11L);
        when(docService.getUserDocumentsFromToken("tk"))
            .thenReturn(List.of(d1));

        ResponseEntity<List<Document>> resp = controller.getMyDocuments("Bearer tk");

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(1, resp.getBody().size());
    }

}
