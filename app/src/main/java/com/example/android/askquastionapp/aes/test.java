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
            , "e8SxhtqUWDEc6UzSU6UNo+XxFj5EmX1s/bDTwtkjZ/ThQvX22gto96pwUCghBqpFOIs/ekY8A8MOwWXZnpa85RQ34qYqIkANAXRcZd8o5uXD/1lKYds2zlITgEgJhm1/TyPXTaGWa6PU26FLwOj78+/0h8LwCJXWd+jHhtQOVybw8MIiXoMgHA4S+BLzozVLT5a2dBqpUsO0BMdKvz/vBhaiiDQ4dRB/yTSCGcUoDTb8H6UYasltqXOZ2YYrM6dtpZAmV+lGaf4Nqa9QBzrSy0M6fWdoVKyS+n3Und5+FcEQjm+k3Am0YE20W10H63EjVn7OK4EQLAmrKns3eloeVUv7wdohyPWSxct5RY+Aa6q6CqQyxKGgGNxdBLkLs/P2njrNB/OqhPryyX1/TkEYp7J9oouKhdU8yhq5Wbxg2R30hxCU4yXpVKwE6EPmgB89BoV/YUD/SQFtSODbvxQ+v+W+oGSunIkdML7eBGnsacIYGoXt63JCEEAJXH7nigN8r8lvEiDRlGZ9nlJyEDKsPcxEgb2WmO//uDZ8Y+Else6ziI3I05aSdREZ5ZCvatrebxFLX0veXK5TfPVglWjcWl7uPiy+KsYlbJEQkek6crkNp+59m1Oms7OTXFw7jfaLytM6NfYGEvxIV318Cm2mC2FOZgGL9LmWRQ/s0NhrX38OWkUZTnzRSV+tV5nW3vPRVVDfs4jjJli396hWAwC7wTrVUCPnN5auktNA5TqiFvrdnfIZfSTfDMQdvJorEn29IWfB+VohzspvprfusLmS5rsXOSPhVA0SGPdCJ5OUgZ6EArYxiwheTQbY8nqtTyG9M1N7ZwUmtzawtamVv+NJh1qsl9J3f56Qooa7RbrX3Y/ByltgneQJdFTZYZwGLMuA3iTWKOIBIYmgbr2a13QfZCA3Sy3ilPPtl6TIxGqTIrJv6QRi4RAeFx2gDYOX/t7YiR/eTPmgchHMXbROeeNZsJy8vBMnZw6uYxWEWrgjMwqG28HHWzKg+zeD9HVo1yl7OvNIiOnJM5ChWLEuyiXr+HfHIhRCzrjX0DGsip7IWZE/53P/ZzSxWtJEWCujoAOHLyLSNT4EJNG5LXcFu9KrR0zUyBh88JKkspZ7Ir4pacVMaGuiRSeEZtDf6URSoXKqf0J5BsafRmoEcOF9EySvBJjMReF4mKBODzYHJqnhLBS9s+vmtO2ixnXIOL6Vlr1oYE6SWWPHVhI9iYN8aAh316ENpTxgtG/sVuwlE1MwR940nDjFmSGKJQx3rMJk23LxKNy+eervoZDj/heZ/czSsk4g5zmWkVm+8Ws6zc8dNHNaDLLzW7kqFqDZWjQk5O0iy/kB6OwC0SgoLwOSb2KH23TshLpUIVrPDORBRDZJsnW6qsBBNyCaB5Xe1/CkSjIuMkIy0bv1ENn2LmCHam88kg=="
            , ""
            , ""
            , ""
    };


    public static void main(String[] args) throws Exception {
        for (String testDatum : testData) {
            if (TextUtils.isEmpty(testDatum)) {
                continue;
            }
            String aesdecrypt = aesdecrypt(testDatum, "d0538ddf6e4ee2a486f4d1383d55dd74");
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
