/*
 * Copyright (c) 2018
 *       BSK (BlueSky Consultants Limited, Kenya)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.app.helaHub.config.security;

import org.app.helaHub.config.properties.OrganizationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author KennethKZMMuhia
 */
@Component
public class HashingImplementation {

    private static Cipher cipher;
    @Autowired
    private OrganizationProperties op;
    private SecretKey secretKey;

    public String getDecryptedValue(String encryptedText) {
        byte[] masterkeybyte = Base64.getDecoder().decode(op.getOrganizationSerial());
        secretKey = new SecretKeySpec(masterkeybyte, 0, masterkeybyte.length, op.getOrganizationValidator());
        try {
            String plainText = decrypt(encryptedText, secretKey);
            Logger.getLogger(HashingImplementation.class.getName()).log(Level.FINEST, "Decrypted Text After Decryption: {0}", plainText);

            return plainText;
        } catch (Exception e) {
            Logger.getLogger(HashingImplementation.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }

    public String getEncryptedValue(String plaintext) {
        byte[] masterkeybyte = Base64.getDecoder().decode(op.getOrganizationSerial());
        secretKey = new SecretKeySpec(masterkeybyte, 0, masterkeybyte.length, op.getOrganizationValidator());

        try {

            String encryptedText = encrypt(plaintext, secretKey);
            Logger.getLogger(HashingImplementation.class.getName()).log(Level.FINEST, "Decrypted Text After Decryption: {0}", toString());

            return encryptedText;
        } catch (Exception e) {
            Logger.getLogger(HashingImplementation.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            return "";
        }
    }


    public String encryptSha2(String plainText) {
        /*
            DONE: Externalise and validate the the scheme is ok
         */
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.reset();
            md.update(plainText.getBytes());
            byte[] digest = md.digest();
            Logger.getLogger(this.getClass().getName()).log(Level.FINE, String.format("[Method 2 =%064x]", new java.math.BigInteger(1, digest)));
            Base64.Encoder encoder = Base64.getEncoder();
            result = encoder.encodeToString(String.format("%064x", new java.math.BigInteger(1, digest)).toUpperCase().getBytes());
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
        return result;
    }


    private String encrypt(String plainText, SecretKey secretKey) throws Exception {
        cipher = Cipher.getInstance(op.getOrganizationValidator());

        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedByte;
        try {
            encryptedByte = cipher.doFinal(plainTextByte);
        } catch (IllegalBlockSizeException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("%s value %s is probably not encrypted", e.getMessage(), "********"));
            return "";
        }
        Base64.Encoder encoder = Base64.getEncoder();
        return encoder.encodeToString(encryptedByte);
    }

    private String decrypt(String encryptedText, SecretKey secretKey) throws Exception {
        cipher = Cipher.getInstance(op.getOrganizationValidator());
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedByte;
        try {

            decryptedByte = cipher.doFinal(encryptedTextByte);
        } catch (IllegalBlockSizeException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, String.format("%s value %s is probably not encrypted", e.getMessage(), encryptedText));
            return encryptedText;
        }
        return new String(decryptedByte);
    }

    protected void GenerateNewKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(op.getHashingConfigAlgorithm());
            keyGenerator.init(op.getHashingConfigKeysize());
            SecretKey secretKey = keyGenerator.generateKey();
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            Logger.getLogger(this.getClass().getName()).info("Secret Key: " + encodedKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
