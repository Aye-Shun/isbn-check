# ISBN Check

A small Java command-line checker for ISBN-10 and ISBN-13. It removes ASCII spaces and hyphens, checks the character format and checksum, and prints the normalized number. A valid ISBN-10 also gets its ISBN-13 equivalent.

Build with a JDK 17 or newer and Make. There are no library dependencies.

```sh
make
java -cp build IsbnCheck "1-234-56789-x"
```

The synthetic example produces:

```text
ISBN-10: 123456789X
Checksum: valid
ISBN-13 equivalent: 9781234567897
```

Quote the entire argument when it contains spaces. The last character of ISBN-10 can be `X` or `x`, representing ten. ISBN-13 uses digits only, starts with `978` or `979`, and excludes the `9790` music-number range. Input is limited to 80 characters before removing separators.

The checksum uses weighted sums, modulo 11 for ISBN-10 and alternating weights of one and three for ISBN-13. Conversion adds `978` to the first nine digits and calculates a new check digit. The [ISBN Agency manual](https://www.isbn-international.org/sites/default/files/ISBN%20Manual%202012%20-corr.pdf) describes the checksum rules.

Run `make test` for unit and real command-process checks, or `make demo` for the example. Exit codes are `0` for a valid checksum or `--help`, `1` for invalid input, and `2` for the wrong argument count.

This checks format and arithmetic, without looking up book assignments or validating publisher ranges and hyphen placement.
