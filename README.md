# LogFlow - Increment 2

## Line Coverage
- `ParserStage.java` Line Coverage: ~92% (8 target scenarios covered via isolated CollectingEmitter test double).

## Change Log
- Added `LogRecord.java`: Immutable domain record with extensible attributes map.
- Added `ParserStage.java`: CLF log parser stage with error counting.
- Updated `Main.java`: Wired `ParserStage` into the pipeline and added skipped lines counter summary.
- Added `CollectingEmitter.java`: In-memory test double for zero-I/O stage unit testing.
- Added `ParserStageTest.java`: 8 unit test cases covering edge and invalid inputs.

## Build
```bash
javac -d bin src/logflow/*.java test/logflow/*.java