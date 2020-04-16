package util;

import io.netty.buffer.ByteBuf;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class EncryptionUtil {
    private static KeyFactory keyFactory;

    static {
        try {

            keyFactory = KeyFactory.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static Map<String,Key> generateRSAKeyMap() {
        Map<String,Key> keyMap = new LinkedHashMap<String,Key>();
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom secureRandom = new SecureRandom(new Date().toString().getBytes());
            keyPairGenerator.initialize(1024, secureRandom);
            KeyPair keyPair = keyPairGenerator.genKeyPair();
            keyMap.put("publicKey",keyPair.getPublic());
            keyMap.put("privateKey",keyPair.getPrivate());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return keyMap;
    }

    public synchronized static void encryptBytesByLongTime(ByteBuf byteBuf, long time){
        long div16 = System.currentTimeMillis()%16;
        String div16String = Long.toBinaryString(div16);
        while(div16String.length()<4){
            div16String = "0"+div16String;
        }
        String s="";
        for(int i =0;i<4;i++){
            s+=Math.random()>0.5?1:0;
        }
        long res1 = Long.valueOf(s,2);
        long res2 = Long.valueOf(s,2)^div16;
        byteBuf.writeShort((int) res1);
        byteBuf.writeShort((int) res2);

    }

    public synchronized static long decryptShorts(short s1,short s2){
        return s1^s2;
    }

    public synchronized static boolean checkShortsAndLongTime(short s1,short s2,long time){
        return decryptShorts(s1,s2)==time%16;
    }

    public synchronized static boolean isIllegalProtocal(ByteBuf byteBuf){
       if(!byteBuf.isReadable(12)){
           return false;
       }
       long time = byteBuf.readLongLE();
       if(Math.abs(time-System.currentTimeMillis())>24*60*60*1000){ // if client timestamp is illegal
          byteBuf.readerIndex(byteBuf.readableBytes());
          return false;
       }
       if(!checkShortsAndLongTime(byteBuf.readShort(),byteBuf.readShort(),time)){ // if encryption is illegal
           byteBuf.readerIndex(byteBuf.readableBytes());
           return false;
       }
       return true;
    }

    public synchronized static byte[] encryptMessageWithPublicKey(byte[] message,PublicKey publicKey) throws InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        return cipher.doFinal(message);
    }

    public synchronized static byte[] decryptMessageWithPrivateKey(byte[] message,PrivateKey privateKey) throws InvalidKeySpecException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(message);
    }

    public synchronized static PublicKey getPublicKeyFromBytes(byte[] publicKeyBytes) throws InvalidKeySpecException {
        return keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
    }

}
