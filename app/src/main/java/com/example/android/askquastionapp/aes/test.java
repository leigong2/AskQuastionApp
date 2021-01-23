package com.example.android.askquastionapp.aes;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Description: AES+RSA签名，加密 验签，解密
 *
 * @author: wubaoguo
 * @email: wustrive2008@gmail.com
 * @date: 2015/8/13 15:12
 */
public class test {

    public static String[] testData = new String[]{
            ""
            , "o4m9Ju+ofjF2Bjs+6UTzJtwhLG5kkL0L6XdlXAvIDBw0vn5p9wczwxh9eJLIN1E/mKosGY/TsaM7L3br4o6SX9ocEuf2tR8/W5p8bTWaZxpblVFf7Y4O0kSJjVJRT4tNG0115hoZmDLKDxGNIUVtNNjBJY0++/QSzE843KL/EImUGEoojQBcw78EINiaM2MZb99tFl0SHRO9Jt3Q5EeMR3i8xciXkayLtuGWu70Krzp9dZ4h3CwfhX8k0GMiL5mCR3KzvRfvoZswMECKZU4WceSxGjFfbWT1SuTa8mB13iW6TCRMkI/XmVakNmDkJO6Yvpdi+6qqJ2c+w9pmtsFdKYS5d1ehM609ROyfWJ4AnJPJMabE0XUzrh8vS3Df+D42KuGQGxsXgA8/IJeDs0Y4IQpr1XMD+E+rkK+Ci+i5iq+pZ34lN4fPQlST0Qv7YUYmYyNcPNQ6ohHjvw3wp7g+xGQ8CCegHclCR2VHqRxByOO6O/KtsHY/3sHmr1T5wlHLC2IMVhHhz5JnCa2aC01gKBm6Icb1N4IwAxTq+F6oIvFyLZGX8i44VbNfTTEALjFGwiUflPME7BhteNeQb80IHNllSD9X3jgr8F7Y88AbNjWYL0ZVAQ+PSvAVEQ+EoDP5CjrvwoFiMuIcfvxEYU48fyTHLNCTcar8kWBea3FqfXVzqXy6J0qCM67LmKXa0u/bjO7bLjMj6vGyNNpFtRZwwYlVXU5+eZlmz3F9o0UcMqx3z9LJQluZe1lbn8Myl/E+eh5D2xijPJMjLVkiSygkdDz/qrwr3BuyOKGcLvrBFDpd+YzEKuCxEa+ckZq/ZA8YC2qO/9iyWdTKWneuzmi0AvgaHZKN4oK3TtsMPUEKUGHbCpPPom191fQaZCS2WI2BmwEK6Tx5JMmQESq1/Srb3agV0qI+1N4BhotiRlMyD/Y9tzLcUelz3cQ7jmYVeFHAtcgN5etSUcJbPelghObdYPNHcl9fqA79FBaT4PWXi7Rs791eKIqUa94+VM/+XXeMr71BtV6srbRtdI0f4pTifdyQK6b5CiLYrGPKsAaeJMs4iZO8TestIsANYK+GO/UDKW/DIAcvxd7Kt/8SdYNOD74Cx+2Vzma8R5e3ni2sAQWnXi+JxE0gIlB7W2ePhKo3BaxasLUH3T+u251QjgyD3dmwT6x0MJoPMZ7flCjhrj6s8S78chhnC7547JSP5y1nxxgs5W/KQ9+srGYpl26Pn42TYNn/Ti3ZIkXFYOIx+x6TeSz8it9kyKkWYIY7TwaDQrw0wyOevScNTtqWDNyrfQCzFaAX241lkGHEfZOKZq8OzezY06E9lojIPrYhmjdwyyl+v3CK0gpiaAxq75sHsHPralrjBvjOymuxTuS+YZxUvyQNmkQBZZOruz7aQ6BY9O3nJN2Cz7n4T4NQkLMg9vQyLBdvoBxjcaX/Z4DxpYOJyxprkgp9datpHW/YtMO6W4rQHGn2hjuw7qcqBky2WFQWwefrmnWxAO18tKZKAA4ZgGR5WB07FjXyIeL4310wgpGa6kolwg9wJ4sIjtts5ZQp6D2zPY5QZADfYLumEh7DjEV8fATSZh5Man8p/ZZu"
            , ""
            , ""
            , ""
    };


    public static void main(String[] args) throws Exception {
        for (String testDatum : testData) {
            if (TextUtils.isEmpty(testDatum)) {
                continue;
            }
            String aesdecrypt = aesdecrypt(testDatum, "fdd3f88b2a9b3e46929ec3a8980ae940");
            Log.i("zune: ", "aesdecrypt = " + aesdecrypt);
            ToastUtils.showShort("解密后的数据：" + aesdecrypt);
        }
    }

    public static String aesdecrypt(String paramString1, String paramString2) throws Exception {
        try {
            byte[] arrayOfByte = android.util.Base64.decode(paramString1, 0);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(paramString2.substring(paramString2.length() - 16).getBytes());
            return new String(decrypt2("AES/CBC/PKCS7Padding", new SecretKeySpec(paramString2.substring(0, 16).getBytes("ASCII"), "AES"), ivParameterSpec, arrayOfByte));
        } catch (Exception exception) {
            exception.printStackTrace();
            return "{code=1,msg=\"\"}";
        }
    }

    private static byte[] decrypt2(String paramString, SecretKey paramSecretKey, IvParameterSpec paramIvParameterSpec, byte[] paramArrayOfbyte) {
        try {
            Cipher cipher = Cipher.getInstance(paramString);
            cipher.init(2, paramSecretKey, paramIvParameterSpec);
            return cipher.doFinal(paramArrayOfbyte);
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("no cipher getinstance support for ");
            stringBuilder.append(paramString);
            Log.e("AESdemo", stringBuilder.toString());
        } catch (NoSuchPaddingException noSuchPaddingException) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("no cipher getinstance support for padding ");
            stringBuilder.append(paramString);
            Log.e("AESdemo", stringBuilder.toString());
        } catch (InvalidKeyException invalidKeyException) {
            Log.e("AESdemo", "invalid key exception");
        } catch (InvalidAlgorithmParameterException invalidAlgorithmParameterException) {
            Log.e("AESdemo", "invalid algorithm parameter exception");
        } catch (IllegalBlockSizeException illegalBlockSizeException) {
            Log.e("AESdemo", "illegal block size exception");
        } catch (BadPaddingException badPaddingException) {
            Log.e("AESdemo", "bad padding exception");
            badPaddingException.printStackTrace();
        }
        return null;
    }
}
