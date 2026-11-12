/** Format and checksum checks for a single ISBN. */
public final class IsbnCheck {
    private IsbnCheck() {}

    static String validate(String input) {
        if (input.length() > 80) {
            throw new IllegalArgumentException("input is longer than 80 characters");
        }
        String isbn = input.replace(" ", "").replace("-", "").replace('x', 'X');
        if (isbn.length() == 10) {
            if (!isbn.matches("[0-9]{9}[0-9X]")) {
                throw new IllegalArgumentException("ISBN-10 needs nine digits and a digit or X");
            }
            int total = 0;
            for (int index = 0; index < 10; index++) {
                int digit = isbn.charAt(index) == 'X' ? 10 : isbn.charAt(index) - '0';
                total += digit * (10 - index);
            }
            if (total % 11 != 0) {
                throw new IllegalArgumentException("ISBN-10 checksum does not match");
            }
        } else if (isbn.length() == 13) {
            if (!isbn.matches("[0-9]{13}")) {
                throw new IllegalArgumentException("ISBN-13 needs thirteen digits");
            }
            if ((!isbn.startsWith("978") && !isbn.startsWith("979")) || isbn.startsWith("9790")) {
                throw new IllegalArgumentException("ISBN-13 needs prefix 978 or 979, excluding 9790");
            }
            if (isbn.charAt(12) - '0' != checkDigit13(isbn.substring(0, 12))) {
                throw new IllegalArgumentException("ISBN-13 checksum does not match");
            }
        } else {
            throw new IllegalArgumentException("use ten or thirteen characters after spaces and hyphens");
        }
        return isbn;
    }

    private static int checkDigit13(String firstTwelve) {
        int total = 0;
        for (int index = 0; index < 12; index++) {
            int weight = index % 2 == 0 ? 1 : 3;
            total += (firstTwelve.charAt(index) - '0') * weight;
        }
        return (10 - total % 10) % 10;
    }

    private static String equivalent13(String validIsbn10) {
        String firstTwelve = "978" + validIsbn10.substring(0, 9);
        return firstTwelve + checkDigit13(firstTwelve);
    }

    static int run(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("Usage: java -cp build IsbnCheck \"ISBN\"");
            System.out.println("Checks format and checksum. Quote spaces and hyphens.");
            return 0;
        }
        if (args.length != 1) {
            System.err.println("Usage: java -cp build IsbnCheck \"ISBN\"");
            return 2;
        }
        final String isbn;
        try {
            isbn = validate(args[0]);
        } catch (IllegalArgumentException error) {
            System.err.println("Invalid ISBN: " + error.getMessage());
            return 1;
        }
        System.out.println("ISBN-" + isbn.length() + ": " + isbn);
        System.out.println("Checksum: valid");
        if (isbn.length() == 10) {
            System.out.println("ISBN-13 equivalent: " + equivalent13(isbn));
        }
        return System.out.checkError() ? 1 : 0;
    }

    public static void main(String[] args) {
        System.exit(run(args));
    }
}
