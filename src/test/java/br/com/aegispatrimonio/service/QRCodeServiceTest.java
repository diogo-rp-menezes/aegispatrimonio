package br.com.aegispatrimonio.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QRCodeServiceTest {

    @Test
    public void testGenerateQRCode() {
        QRCodeService qrCodeService = new QRCodeService();
        byte[] qrCode = qrCodeService.generateQRCode("TEST-DATA", 200, 200);

        Assertions.assertNotNull(qrCode);
        Assertions.assertTrue(qrCode.length > 0);

        // Check PNG magic numbers: 89 50 4E 47 0D 0A 1A 0A
        if (qrCode.length > 8) {
            Assertions.assertEquals((byte) 0x89, qrCode[0]);
            Assertions.assertEquals((byte) 0x50, qrCode[1]);
            Assertions.assertEquals((byte) 0x4E, qrCode[2]);
            Assertions.assertEquals((byte) 0x47, qrCode[3]);
        }
    }
}
