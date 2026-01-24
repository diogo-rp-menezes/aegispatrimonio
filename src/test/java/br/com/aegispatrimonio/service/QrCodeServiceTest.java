package br.com.aegispatrimonio.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QrCodeServiceTest {

    @Test
    public void shouldGenerateQrCode() {
        QrCodeService service = new QrCodeService();
        byte[] result = service.generateQrCode("test", 200, 200);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.length > 0);

        // PNG header signature
        Assertions.assertEquals((byte) 0x89, result[0]);
        Assertions.assertEquals((byte) 0x50, result[1]);
        Assertions.assertEquals((byte) 0x4E, result[2]);
        Assertions.assertEquals((byte) 0x47, result[3]);
    }
}
