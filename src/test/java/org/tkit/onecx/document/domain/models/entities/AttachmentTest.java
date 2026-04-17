package org.tkit.onecx.document.domain.models.entities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.tkit.onecx.document.domain.models.enums.AttachmentUnit;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttachmentTest {

    @Test
    @DisplayName("Getter And Setters for Attachment")
    void testAttachmentSetters() {

        Attachment attachment = new Attachment();

        attachment.setSizeUnit(AttachmentUnit.BYTES);
        attachment.setSize(BigDecimal.ZERO);
        attachment.setFile("Document File");

        AttachmentUnit SizeUnit = attachment.getSizeUnit();
        BigDecimal Size = attachment.getSize();
        String File = attachment.getFile();

        assertThat(SizeUnit, equalTo(AttachmentUnit.BYTES));
        assertThat(Size, equalTo(BigDecimal.ZERO));
        assertThat(File, equalTo("Document File"));

    }
}
