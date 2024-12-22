package cn.hackedmc.urticaria.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@SuppressWarnings("All")
public class CryptUtil {
    public static class Netease {
        public static String encryptDataJoin(String b) {
            String prefixAndSuffix = "HelOSJDZCNHSHH177616427184184YHAHDJDIKZXNC92477819lo11";

            // 将输入字符串反转
            String reversed = new StringBuilder(b).reverse().toString();

            // 将反转后的字符串转换为字节数组，并以十六进制表示
            byte[] bytes = reversed.getBytes(StandardCharsets.UTF_8);
            StringBuilder hexString = new StringBuilder();
            for (byte byteValue : bytes) {
                hexString.append(String.format("%02x", byteValue));
            }

            // 添加前缀和后缀
            String withPrefixAndSuffix = prefixAndSuffix + hexString.toString() + prefixAndSuffix;

            // 最后再次反转字符串
            String finalReversed = new StringBuilder(withPrefixAndSuffix).reverse().toString();

            return finalReversed;
        }
    }

    public static class Base64Crypt {
        public static String decrypt(String message) {
            return new String(Base64.getDecoder().decode(message));
        }
        public static String encrypt(String message) {
            return Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static class RSA {
        private static final String RSA_KEY_ALGORITHM = "RSA";

        public static byte[] encryptByPublicKey(byte[] data, String publicKeyStr) {
            try {
                //Java原生base64解码
                byte[] pubKey = Base64.getDecoder().decode(publicKeyStr);
                //创建X509编码密钥规范
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(pubKey);
                //返回转换指定算法的KeyFactory对象
                KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
                //根据X509编码密钥规范产生公钥对象
                PublicKey publicKey = keyFactory.generatePublic(x509KeySpec);
                //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
                Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
                //用公钥初始化此Cipher对象（加密模式）
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                //对数据加密
                byte[] encrypt = cipher.doFinal(Base64.getEncoder().encode(data));
                //返回base64编码后的字符串
                return Base64.getEncoder().encode(encrypt);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return new byte[]{};
        }

        /**
         * 私钥解密(用于数据解密)
         *
         * @param data          解密前的字符串
         * @param privateKeyStr 私钥
         * @return 解密后的字符串
         */
        public static String decryptByPrivateKey(byte[] data, String privateKeyStr) {
            try {
                //Java原生base64解码
                byte[] priKey = Base64.getDecoder().decode(privateKeyStr);
                //创建PKCS8编码密钥规范
                PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
                //返回转换指定算法的KeyFactory对象
                KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
                //根据PKCS8编码密钥规范产生私钥对象
                PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
                //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
                Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
                //用私钥初始化此Cipher对象（解密模式）
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                //对数据解密
                //返回字符串
                return new String(Base64.getDecoder().decode(cipher.doFinal(Base64.getDecoder().decode(data))));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return "";
        }

        public static byte[] decryptByPrivateKeyByte(byte[] data, String privateKeyStr) {
            try {
                //Java原生base64解码
                byte[] priKey = Base64.getDecoder().decode(privateKeyStr);
                //创建PKCS8编码密钥规范
                PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(priKey);
                //返回转换指定算法的KeyFactory对象
                KeyFactory keyFactory = KeyFactory.getInstance(RSA_KEY_ALGORITHM);
                //根据PKCS8编码密钥规范产生私钥对象
                PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
                //根据转换的名称获取密码对象Cipher（转换的名称：算法/工作模式/填充模式）
                Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
                //用私钥初始化此Cipher对象（解密模式）
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                //对数据解密
                //返回字符串
                return Base64.getDecoder().decode(cipher.doFinal(Base64.getDecoder().decode(data)));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return new byte[]{};
        }
    }

    public static class AES {
        /**
         * encrypt input kotlin.text with AES CBC NoPadding
         *
         * @param key decrypt key
         * @param data un-decrypt data
         * @param iv initVector
         * @return encrypted data
         */
        public static byte[] encrypt(byte[] key, byte[] data, byte[] iv) throws Exception {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

            return cipher.doFinal(data);
        }

        /**
         * decrypt input kotlin.text with AES CBC NoPadding
         *
         * @param key decrypt key
         * @param data un-decrypt data
         * @param iv initVector
         * @return decrypted data
         */
        public static byte[] decrypt(byte[] key, byte[] data, byte[] iv) throws Exception {
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            final Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

            return cipher.doFinal(data);
        }
    }
}
