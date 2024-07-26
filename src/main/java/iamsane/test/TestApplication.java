package iamsane.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestApplication {
    public static void main(String[] args) throws IOException, InterruptedException {
        CrptApi client = new CrptApi(TimeUnit.MINUTES, 10);
        client.doPost(DocumentDto.builder().build());
        client.close();
    }
}
