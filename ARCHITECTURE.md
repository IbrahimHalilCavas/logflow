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

## Increment 3 — Ordering Experiment & Trade-offs

Aynı veri kümesi üzerinde filtreleme ve projeksiyon sıraları denenmiş ve şu mimari sonuçlara ulaşılmıştır:

1. **Ayrıştırma Öncesi Filtreleme:** İmkânsızdır. Ham metin üzerinde alan bazlı (status >= 400 vb.) mantıksal süzme yapılamaz; verinin önce `ParserStage` ile tip güvenli `LogRecord` nesnesine dönüştürülmesi gerekir.
2. **Projeksiyondan Önce Filtreleme (Süzme -> Projeksiyon):**
   - **Avantajı:** Koşula uymayan kayıtlar hemen elenir. Projeksiyon aşamasına daha az veri aktarıldığı için bellek kullanımı ve CPU maliyeti düşer.
3. **Projeksiyondan Sonra Filtreleme (Projeksiyon -> Süzme):**
   - **Dezavantajı:** Elenecek olan veriler için boşuna alan seçimi ve bellek kopyalaması yapılır.
   - **Risk:** Filtreleme aşaması elenmiş alanlardan birine (örneğin `status`) ihtiyaç duyuyorsa ve bu alan projeksiyonda çıkarılmışsa filtre doğru çalışmaz.