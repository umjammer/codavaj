/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.codavaj.Main;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Testcase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/05/09 umjammer initial version <br>
 */
class Testcase {

    @ParameterizedTest
    @CsvSource({
        "6/en,6/en",
        "8/en,8/en",
        "11/en,8/en",
        "12/en,8/en",
        "13/en,8/en",
        "8/jp,8/jp",
        "11/jp,8/jp",
        "12/jp,8/jp",
        "13/jp,8/jp",
    })
    void test(String version, String expected) throws Exception {

        String[] names = {
            "vavi/test/codavaj/Test1.java",
            "vavi/test/codavaj/Test2.java",
            "vavi/test/codavaj/Test3.java",
            "vavi/test/codavaj/Test4.java",
            "vavi/test/codavaj/Test5.java",
        };

        Main.main(new String[] { "codavaj", String.format("src/test/resources/javadoc/%s/apidocs", version), "tmp/test" });

        for (String name : names) {
            Checksum checksum1 = new CRC32();
            Checksum checksum2 = new CRC32();

            Path path1 = Paths.get("src/test/resources/codavaj", expected, name);
            byte[] bytes1 = Files.readAllBytes(path1);
            checksum1.update(bytes1, 0, bytes1.length);

            Path path2 = Paths.get("tmp/test", name);
            byte[] bytes2 = Files.readAllBytes(path2);
            checksum2.update(bytes2, 0, bytes2.length);

            if (checksum1.getValue() != checksum2.getValue()) {
System.err.println(path1 + ", " + path2 + " (" + version + ")");
System.err.println(checksum1.getValue() + " ," + checksum2.getValue());

                ProcessBuilder pb = new ProcessBuilder().command("diff", "-ru", "-x", ".DS_Store", "src/test/resources/codavaj/" + expected, "tmp/test");
                pb.inheritIO();
                pb.start();
            }

            assertEquals(checksum1.getValue(), checksum2.getValue());
        }
    }
}

/* */
