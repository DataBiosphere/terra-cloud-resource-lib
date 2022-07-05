package bio.terra.cloudres.azure.landingzones.deployment;

import java.util.Locale;
import java.util.UUID;

public class ResourceUtils {

    public static String createUniqueAzureResourceName(){
        return createUniqueAzureResourceName(23);
    }
    public static String createUniqueAzureResourceName(int length){
        return "lz" + UUID.randomUUID().toString()
                .toLowerCase(Locale.ROOT)
                .replace("-","")
                .substring(0,length-2);
    }
}
