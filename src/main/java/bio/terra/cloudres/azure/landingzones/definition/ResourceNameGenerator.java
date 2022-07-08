package bio.terra.cloudres.azure.landingzones.definition;

import com.azure.core.util.logging.ClientLogger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ResourceNameGenerator {
    private final ClientLogger logger = new ClientLogger(ResourceNameGenerator.class);
    private final String landingZoneId;
    private int sequence;

    public ResourceNameGenerator(String landingZoneId) {
        this.landingZoneId = landingZoneId;
        sequence = 0;
    }

    public synchronized String nextName(int length){
        String name = prepareStringAsAzureResourceName(generateHashFromSeed(), length);
        sequence = sequence + 1 ;
        return name;
    }

    public synchronized void resetSequence(){
        sequence = 0;
    }

    private String generateHashFromSeed() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw  logger.logExceptionAsError(new RuntimeException(e));
        }

        final byte[] hashbytes = messageDigest.digest(
                parseStringToHash().getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hashbytes);
    }

    private  String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
    private String prepareStringAsAzureResourceName(String hash, int length){
        int end = length;
        if (length < 5 ){
            end = 5;
        }
        return "lz"+hash.substring(0,end-2);
    }

    private String parseStringToHash(){
        return String.format("%s%s",landingZoneId, sequence);
    }

}
