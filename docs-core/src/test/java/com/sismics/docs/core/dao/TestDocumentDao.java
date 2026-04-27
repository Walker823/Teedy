package com.sismics.docs.core.dao;

import com.sismics.docs.BaseTransactionalTest;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.Document;
import com.sismics.docs.core.model.jpa.File;
import com.sismics.docs.core.model.jpa.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Tests for DocumentDao.
 */
public class TestDocumentDao extends BaseTransactionalTest {
    @Test
    public void testCreateFindAndGetDocument() throws Exception {
        User user = createUser("docDaoCreate");
        DocumentDao documentDao = new DocumentDao();
        File fileBefore = createFile(user, FILE_JPG_SIZE);

        Document document = createDocument(user.getId(), "Doc title");
        document.setDescription("Doc description");
        document.setSubject("Doc subject");
        document.setIdentifier("Doc identifier");
        document.setPublisher("Doc publisher");
        document.setFormat("Doc format");
        document.setSource("Doc source");
        document.setType("Doc type");
        document.setCoverage("Doc coverage");
        document.setRights("Doc rights");
        document.setFileId(fileBefore.getId());

        String documentId = documentDao.create(document, user.getId());

        Assert.assertNotNull(documentId);
        Assert.assertNotNull(documentDao.getById(documentId));
        Assert.assertNull(documentDao.getById("missing-document-id"));

        List<Document> all = documentDao.findAll(0, 10);
        Assert.assertEquals(1, all.size());

        List<Document> byUser = documentDao.findByUserId(user.getId());
        Assert.assertEquals(1, byUser.size());

        Assert.assertEquals(1L, documentDao.getDocumentCount());

        DocumentDto denied = documentDao.getDocument(documentId, PermType.READ, Collections.emptyList());
        Assert.assertNull(denied);

        DocumentDto missing = documentDao.getDocument("missing-document-id", PermType.READ, Collections.singletonList("admin"));
        Assert.assertNull(missing);

        DocumentDto dto = documentDao.getDocument(documentId, PermType.READ, Collections.singletonList("admin"));
        Assert.assertNotNull(dto);
        Assert.assertEquals(documentId, dto.getId());
        Assert.assertEquals("Doc title", dto.getTitle());
        Assert.assertEquals("Doc description", dto.getDescription());
        Assert.assertEquals("Doc subject", dto.getSubject());
        Assert.assertEquals("Doc identifier", dto.getIdentifier());
        Assert.assertEquals("Doc publisher", dto.getPublisher());
        Assert.assertEquals("Doc format", dto.getFormat());
        Assert.assertEquals("Doc source", dto.getSource());
        Assert.assertEquals("Doc type", dto.getType());
        Assert.assertEquals("Doc coverage", dto.getCoverage());
        Assert.assertEquals("Doc rights", dto.getRights());
        Assert.assertEquals("eng", dto.getLanguage());
        Assert.assertEquals(fileBefore.getId(), dto.getFileId());
        Assert.assertEquals(Integer.valueOf(0), dto.getFileCount());
        Assert.assertFalse(dto.getShared());
        Assert.assertEquals("docDaoCreate", dto.getCreator());
    }

    @Test
    public void testUpdateAndUpdateFileId() throws Exception {
        User user = createUser("docDaoUpdate");
        DocumentDao documentDao = new DocumentDao();
        File initialFile = createFile(user, FILE_JPG_SIZE);
        File updatedFile = createFile(user, FILE_JPG_SIZE);
        File finalFile = createFile(user, FILE_JPG_SIZE);

        Document created = createDocument(user.getId(), "Initial title");
        created.setFileId(initialFile.getId());
        String documentId = documentDao.create(created, user.getId());

        Document updateDocument = createDocument(user.getId(), "Updated title");
        updateDocument.setId(documentId);
        updateDocument.setDescription("Updated description");
        updateDocument.setSubject("Updated subject");
        updateDocument.setIdentifier("Updated identifier");
        updateDocument.setPublisher("Updated publisher");
        updateDocument.setFormat("Updated format");
        updateDocument.setSource("Updated source");
        updateDocument.setType("Updated type");
        updateDocument.setCoverage("Updated coverage");
        updateDocument.setRights("Updated rights");
        updateDocument.setFileId(updatedFile.getId());

        Document updated = documentDao.update(updateDocument, user.getId());
        Assert.assertEquals("Updated title", updated.getTitle());
        Assert.assertEquals("Updated description", updated.getDescription());
        Assert.assertEquals("Updated subject", updated.getSubject());
        Assert.assertEquals("Updated identifier", updated.getIdentifier());
        Assert.assertEquals("Updated publisher", updated.getPublisher());
        Assert.assertEquals("Updated format", updated.getFormat());
        Assert.assertEquals("Updated source", updated.getSource());
        Assert.assertEquals("Updated type", updated.getType());
        Assert.assertEquals("Updated coverage", updated.getCoverage());
        Assert.assertEquals("Updated rights", updated.getRights());
        Assert.assertEquals(updatedFile.getId(), updated.getFileId());
        Assert.assertNotNull(updated.getUpdateDate());

        Document fileIdUpdate = new Document();
        fileIdUpdate.setId(documentId);
        fileIdUpdate.setFileId(finalFile.getId());
        documentDao.updateFileId(fileIdUpdate);

        Document reloaded = documentDao.getById(documentId);
        Assert.assertNotNull(reloaded);
        Assert.assertEquals(finalFile.getId(), reloaded.getFileId());
    }

    private Document createDocument(String userId, String title) {
        Document document = new Document();
        document.setUserId(userId);
        document.setTitle(title);
        document.setLanguage("eng");
        document.setCreateDate(new Date());
        return document;
    }
}
