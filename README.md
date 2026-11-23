# Emirp Number

Emirp Number is a JavaFX desktop app that finds emirps in the inclusive range from 1 to an upper limit. An emirp is a prime whose decimal digits reverse to a different prime. For example, 13 and 31 are both emirps. The reverse can exceed the search limit.

The default search covers 1 through 10,000 with four worker threads. The app accepts positive 32-bit integer limits and supports up to 64 workers, with at most one worker per number. A completed search displays the emirps and totals for the full range. Stopping a search clears its partial results.

## Example

With an upper limit of 100 and four workers, the emirps are **13, 17, 31, 37, 71, 73, 79, and 97**.

| Emirps | Non-emirps | Numbers checked |
| ---: | ---: | ---: |
| 8 | 92 | 100 |

## Build

The project targets Java 17 and uses JavaFX 21.0.6. The Maven wrapper provides the `javafx:run` goal for the desktop app and the `test` goal for the test suite.
