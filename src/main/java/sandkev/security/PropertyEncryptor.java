package sandkev.security;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kevsa on 06/02/2017.
 */
public class PropertyEncryptor implements StringEncryptor {

    private final StandardPBEStringEncryptor encryptor;
    public PropertyEncryptor(StandardPBEStringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public PropertyEncryptor(String key) {
        encryptor = new StandardPBEStringEncryptor();
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        encryptor.setPassword(key);
    }

    private static final Pattern ENCRYPTED_PATTERN = Pattern.compile("ENC\\((.*)\\)");

    public String decrypt(String value) {
        Matcher matcher = ENCRYPTED_PATTERN.matcher(value);
        return matcher.matches() ? encryptor.decrypt(matcher.group(1)) : value;
    }

    /**
     * @param value the plaintext to be encrypted
     * @return the ciphertext equivalent of the plaintext, formatted as "ENC(ciphertext)".
     */
    public String encrypt(String value) {
        return "ENC(" + encryptor.encrypt(value) + ")";
    }

    public String encryptIfNotEncrypted(String value) {
        return !isEncrypted(value) ? encrypt(value) : value;
    }

    /**
     * @param value a String that may or may not be in the encrypted format - ENC(ciphertext).
     * @return true if the String matches the encypted format.
     */
    public boolean isEncrypted(String value) {
        Matcher matcher = ENCRYPTED_PATTERN.matcher(value);
        return matcher.matches();
    }

}
