package se.kth.id2203.Util;

import com.google.common.hash.Hasher;

import static com.google.common.hash.Hashing.murmur3_32;

/**
 * Created by vilhelm on 2017-02-17.
 */
public class MurmurHasher {

    public static int keyToHash(String data) {
        Hasher murmurHasher = murmur3_32().newHasher();
        murmurHasher.putBytes(data.getBytes());
        int hashedKey = murmurHasher.hash().asInt();
        System.out.println("Key is: " + data);
        System.out.println("Hashed key is: " + hashedKey);
        return hashedKey;
    }

}
