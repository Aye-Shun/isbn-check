import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class IsbnCheckTest {
    private static int checks;

    private IsbnCheckTest() {}

    private static void equal(Object actual, Object expected) {
        if (!expected.equals(actual)) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
        checks++;
    }

    private static void rejects(String input) {
        try {
            IsbnCheck.validate(input);
        } catch (IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("Accepted invalid ISBN: " + input);
    }

    private record Output(int status, String stdout, String stderr) {}

    private static Output cli(String... args) throws IOException, InterruptedException {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        List<String> command = new ArrayList<>(List.of(java, "-cp", "build", "IsbnCheck"));
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).start();
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        return new Output(process.waitFor(), stdout.replace("\r\n", "\n"), stderr);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Synthetic payloads test the checksum rules, without claiming book assignments.
        equal(IsbnCheck.validate("1-234-56789-x"), "123456789X");
        equal(IsbnCheck.validate(" 1 234 56789 X "), "123456789X");
        equal(IsbnCheck.validate("0000000000"), "0000000000");
        equal(IsbnCheck.validate("9781234567897"), "9781234567897");
        equal(IsbnCheck.validate("9791234567896"), "9791234567896");
        equal(IsbnCheck.validate("978 0 0000 0000 2"), "9780000000002");
        for (String invalid : List.of("", "123456789", "12345678900", "1234567890",
                "12345678X9", "123456789Y", "9781234567898", "979123456789X",
                "9771234567898", "9790000000001", "１２３４５６７８９Ｘ",
                "123456789\tX", "123456789\nX", "ISBN:123456789X", " ".repeat(81))) {
            rejects(invalid);
        }

        Output valid10 = cli("1-234-56789-x");
        equal(valid10.status(), 0);
        equal(valid10.stdout(), "ISBN-10: 123456789X\nChecksum: valid\nISBN-13 equivalent: 9781234567897\n");
        equal(valid10.stderr(), "");
        Output valid13 = cli("9791234567896");
        equal(valid13.status(), 0);
        equal(valid13.stdout(), "ISBN-13: 9791234567896\nChecksum: valid\n");
        equal(valid13.stderr(), "");
        Output bad = cli("1234567890");
        equal(bad.status(), 1);
        equal(bad.stdout(), "");
        equal(bad.stderr().startsWith("Invalid ISBN:"), true);
        for (String[] invocation : List.of(new String[0], new String[]{"123456789X", "extra"},
                new String[]{"--help", "extra"})) {
            Output usage = cli(invocation);
            equal(usage.status(), 2);
            equal(usage.stdout(), "");
            equal(usage.stderr().startsWith("Usage:"), true);
        }
        Output help = cli("--help");
        equal(help.status(), 0);
        equal(help.stdout().startsWith("Usage:"), true);
        equal(help.stderr(), "");
        Output blank = cli("");
        equal(blank.status(), 1);
        equal(blank.stdout(), "");
        System.out.println(checks + " checks passed, including 8 CLI processes.");
    }
}
