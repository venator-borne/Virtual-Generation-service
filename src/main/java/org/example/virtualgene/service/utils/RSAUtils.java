package org.example.virtualgene.service.utils;


import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class RSAUtils {
    private static final KeyPair KEY_PAIR = initKeyPair();

    private static KeyPair initKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateBase64PublicKey() {
        RSAPublicKey publicKey = (RSAPublicKey) KEY_PAIR.getPublic();
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String decryptBase64(String code) {
        return new String(decrypt(Base64.getDecoder().decode(code)));
    }

    private static byte[] decrypt(byte[] data) {
        try {
            Cipher decrypt = Cipher.getInstance("RSA");
            decrypt.init(Cipher.DECRYPT_MODE, KEY_PAIR.getPrivate());
            return decrypt.doFinal(data);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}
