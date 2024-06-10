package fdu.capstone.util;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;

/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */
public class HashUtils
{

    public static String  getMD5HashWithSalt(String context,String salt) {

        HashFunction hashFunction = Hashing.md5();
        HashCode hashCode = hashFunction.newHasher().putString(context + salt, Charset.defaultCharset()).hash();
        return getMD5Hash(getMD5Hash(context)+salt);

    }

    private static String getMD5Hash(String context){
        HashFunction hashFunction = Hashing.md5();
        HashCode hashCode = hashFunction.newHasher().putString(context , Charset.defaultCharset()).hash();

        return hashCode.toString();
    }

}
