package com.y3technologies.masters.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.util.ResourceUtils;

public class EncryptUtils {

  private final String PRIVATE_KEY_FILE_NAME = "private_key";
  private final String PUBLIC_KEY_FILE_NAME = "public_key";
  private final String ENCRYPT_METHOD_NAME = "RSA";
  private final String KEY_FOLDER = "key";

  public String encrypt(String input) throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
      BadPaddingException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException {
    byte[] publicKey = getKey(PUBLIC_KEY_FILE_NAME);
    X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKey);
    KeyFactory factory = KeyFactory.getInstance(ENCRYPT_METHOD_NAME);
    PublicKey pubKey = factory.generatePublic(spec);
    Cipher c = Cipher.getInstance(ENCRYPT_METHOD_NAME);
    c.init(Cipher.ENCRYPT_MODE, pubKey);
    byte[] encryptOut = c.doFinal(input.getBytes());
    return new String(Base64.getEncoder().encode(encryptOut));
  }

  public String decrypt(String input) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException,
      NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    byte[] privateKey = getKey(PRIVATE_KEY_FILE_NAME);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey);
    KeyFactory factory = KeyFactory.getInstance(ENCRYPT_METHOD_NAME);
    PrivateKey priKey = factory.generatePrivate(spec);
    Cipher c = Cipher.getInstance(ENCRYPT_METHOD_NAME);
    c.init(Cipher.DECRYPT_MODE, priKey);
    byte[] decryptOut = c.doFinal(Base64.getDecoder().decode(input));
    return new String(decryptOut);
  }

  private byte[] getKey(String keyFileName) throws NoSuchAlgorithmException, IOException {
    InputStream fis = getClass().getResourceAsStream(getClassPathFilePath(keyFileName));
    if (Objects.isNull(fis)) {
      generateNewKeyPair();
      fis = getClass().getResourceAsStream(getClassPathFilePath(keyFileName));
      if(Objects.isNull(fis)) {
        return new byte[0];
      }
    }
    byte[] result = fis.readAllBytes();
    fis.close();
    return result;
  }

  private void generateNewKeyPair() throws NoSuchAlgorithmException, IOException {
    SecureRandom sr = new SecureRandom();
    KeyPairGenerator kpg;
    kpg = KeyPairGenerator.getInstance(ENCRYPT_METHOD_NAME);
    kpg.initialize(2048, sr);
    KeyPair kp = kpg.genKeyPair();
    PublicKey pubKey = kp.getPublic();
    PrivateKey priKey = kp.getPrivate();
    writeKeyToFile(PUBLIC_KEY_FILE_NAME, pubKey.getEncoded());
    writeKeyToFile(PRIVATE_KEY_FILE_NAME, priKey.getEncoded());
  }

  private void writeKeyToFile(String fileName, byte[] key) throws IOException {
    File file = ResourceUtils.getFile(getClassPathFilePath(fileName));
    try (FileOutputStream fos = new FileOutputStream(file)) {
      fos.write(key);
    } catch (IOException e) {
      throw e;
    }
  }

  private String getClassPathFilePath(String fileName) {
    return "/" + KEY_FOLDER + "/" + fileName;
  }

}
