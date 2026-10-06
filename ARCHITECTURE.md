# Architecture - Increment 2

## Components and Connectors
- **Source (`FileLineSource`)**: Reads file line-by-line.
- **Stage (`ParserStage`)**: Parses CLF string inputs into strongly-typed `LogRecord` instances. Invalid lines are counted and skipped.
- **Connector (`Pipeline`)**: Coordinates data flow between stages.
- **Sink (`ConsoleSink`)**: Displays formatted record strings to standard output.

## Extensibility Note
`LogRecord` contains an immutable `Map attributes`. This map acts as an open extension point so future stages (weeks 5-7) can enrich records without modifying the parser.

## Architecture Diagram

+----------------+      +---------------+      +---------------+
| FileLineSource | ---> |  ParserStage  | ---> |  ConsoleSink  |
|    (Source)    |      |    (Stage)    |      |    (Sink)     |
+----------------+      +---------------+      +---------------+