import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class SimpleTest {
    public static void main(String[] args) throws IOException {
        String address = "/netty_rpc/getUser/192.168.43.143:8026";
        System.out.println(address.substring(address.lastIndexOf("/") + 1, address.length()));
    }

}
