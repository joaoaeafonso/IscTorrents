package utils;

import java.util.Objects;

public class TorrentsUtils {

    public static String generateIdentifier(String ipAddress, int port) {
        String data = ipAddress + port;
        return Integer.toHexString(Objects.hash(data));
    }

}
