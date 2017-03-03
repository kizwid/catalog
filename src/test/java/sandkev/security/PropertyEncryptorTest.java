package sandkev.security;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by kevsa on 06/02/2017.
 */
public class PropertyEncryptorTest {

    private PropertyEncryptor encryptor;

    @Before
    public void setUp() throws Exception {
        encryptor = new PropertyEncryptor("secret");
    }

    @Test
    public void canEncrypt(){
        doEncryptDecrypt("kevin");
    }

    @Test(expected = EncryptionOperationNotPossibleException.class)
    public void willThrowexceptionIfNotValidEncryption(){
        doDecrypt("", "ENC(not valid encryption)");
    }

    @Test
    public void willIgnoreNonEncryptedText(){
        doDecrypt("not valid encryption", "not valid encryption");
    }

    @Test
    public void isDifferentEachTime(){
        String plainText = "foo";
        Set<String> results = new HashSet<String>();
        int numberOfTimes = 10;
        for(int n = 0; n < numberOfTimes; n++){
            results.add(encryptor.encrypt(plainText));
        }
        assertEquals("was not different each time", numberOfTimes, results.size());
    }

    private void doEncryptDecrypt(String plainText) {
        String encrypted = encryptor.encrypt(plainText);
        doDecrypt(plainText, encrypted);
    }

    private void doDecrypt(String plainText, String encrypted) {
        String decrypted = encryptor.decrypt(encrypted);
        System.out.println(plainText + " -> " + encrypted);
        assertEquals("failed to decrypt", plainText, decrypted);
    }


}