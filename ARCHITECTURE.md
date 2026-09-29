# Architecture - Increment 1

## Components and Connectors
- **Source (`FileLineSource`)**: Reads the file line by line and passes each record to the pipeline via `Emitter`.
- **Connector (`Pipeline`)**: Coordinates data flow from source to sink.
- **Sink (`ConsoleSink`)**: Receives strings from the pipeline and prints them to standard output.

## Two-Box Diagram
+--------------------+                     +--------------------+
|   FileLineSource   | ---> [ Pipeline ] ---> |    ConsoleSink     |
|      (Source)      |                     |       (Sink)       |
+--------------------+                     +--------------------+