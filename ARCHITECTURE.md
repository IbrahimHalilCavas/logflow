## Increment 5 — Hata Yönetimi ve Kirli Veri

### 5.1 Temel fikir
Hata yönetimi `try/catch` serpiştirmek değil, **topoloji kararıdır**: hatalı kayıt ana hattan çıkar,
ikinci bir (dead-letter) hatta gider. Böylece tek bir bozuk satır tüm çalıştırmayı öldürmez ve hiçbir kayıt
sessizce kaybolmaz.

### 5.2 İki tür hata
| | `StageException` | `PipelineException` |
|---|---|---|
| Kapsam | Tek bir kayıt | Tüm çalıştırma |
| Kurtarılabilir mi? | Evet | Hayır |
| Örnek | Bozuk satır, geçersiz tarih, negatif bayt | Config hatası, okunamayan dosya, disk dolu |
| Politika uygulanır mı? | **Evet** | **Hayır** — her zaman çalıştırmayı durdurur |

Beklenmeyen `RuntimeException` (ör. NPE) bir `StageException` gibi ele alınır; böylece bir stage hatası
hattı çökertmez ve kayıt dead-letter'a düşer.

### 5.3 Hata politikaları
| Politika | Davranış | Ne zaman |
|---|---|---|
| `FAIL_FAST` | İlk hatada dur (`PipelineException`) | Veri bütünlüğü kritikse |
| `SKIP` | Kaydı at, sayacı artır | Kayıp kabul edilebilirse |
| `DEAD_LETTER` | Kaydı ham hâliyle + hata nedeniyle dead-letter dosyasına yaz | Sonradan incelenecekse (varsayılan) |
| `RETRY(n)` | n kez yeniden dene, hâlâ başarısızsa dead-letter | Geçici (I/O, ağ) hatalar. Deterministik parse hatasında anlamsızdır |

Politika üç düzeyde verilebilir: pipeline varsayılanı, `source`, ve her `stage` için ayrı (`error_policy:`).
Stage çıktısı arabelleğe alındığı için `RETRY` yinelenen çıktı üretmez.

### 5.4 Hata türü → politika tablosu (access-dirty.log)
| Kirli veri | Yakalayan | Neden | Politika |
|---|---|---|---|
| Geçersiz UTF-8 baytları (FF FE FA) | source (`StrictLineReader`) | Satır bazında katı çözme; tek satır tüm okumayı bozmasın | DEAD_LETTER |
| Kesik / biçimi bozuk satır | `validate` | Tam satır regex'i uymuyor | DEAD_LETTER |
| Negatif bayt miktarı | `validate` (`bytes_range`) | İş kuralı ihlali | DEAD_LETTER |
| Epoch 1970 / yıl 2099 zaman damgası | `validate` (`min_timestamp`, `max_future_days`) | Mantıksız zaman | DEAD_LETTER |
| Aşırı uzun satır (100 KB) | `validate` (`max_line_length`) | Bellek/log-injection koruması | DEAD_LETTER |
| Yorum / boş satır | source (`skip_comments`) | Veri değil; hata da değil | "yok sayılan" sayacı |
| Hiçbir şey emit etmeyen stage | pipeline | Hata değil, filtre | "elenen" sayacı |
| Okunamayan dosya, disk dolu, config hatası | source/sink/loader | Kurtarılamaz | **Her zaman durdur** |

### 5.5 Dead-letter topolojisi
```
                        ┌─────────┐   ┌──────────┐   ┌─────────┐   ┌──────────┐
   access.log ─────────▶│ Source  │──▶│ validate │──▶│  parse  │──▶│   Sink   │──▶ valid-output.txt
                        └────┬────┘   └────┬─────┘   └────┬────┘   └──────────┘
                             │ okunamayan  │ StageException│ StageException
                             ▼             ▼               ▼
                        ┌──────────────────────────────────────┐
                        │  hata politikası (FAIL_FAST/SKIP/    │
                        │  DEAD_LETTER/RETRY)                  │
                        └───────────────┬──────────────────────┘
                                        │ FailedRecord(ham girdi, aşama, istisna, zaman)
                                        ▼
                                 ┌──────────────┐
                                 │ DeadLetterSink│──▶ dead-letter.txt   (ikinci, ayrı hat)
                                 └──────────────┘
```

### 5.6 RunSummary ve kayıt korunumu
Her çalıştırma sonunda (hata olsa bile `finally` içinde) özet basılır. Sayılar şu eşitliği sağlar:

`okunan + fan-out = çıkan + atlanan + elenen + dead-letter`

Chaos testi: `10 + 0 = 3 + 0 + 0 + 7`. Eşitlik tutmazsa `reconciles()` false döner ve özet "TUTMUYOR" yazar;
yani kayıp kayıt artık görünür bir hatadır. Yarıda kesilen (FAIL_FAST) çalıştırmada kontrol yapılmaz.

### 5.7 Bildirimli doğrulama kuralları
`validate` stage'ı kurallarını YAML `params` alanından okur (kodda sabit değil):
`max_line_length`, `min_timestamp`, `max_future_days`, `status_range`, `bytes_range`, `required_fields`, `validate_ip`.
Yeni kural eklemek = `ValidationRules` içine bir alan + `ValidationStage` içine bir kontrol.

### 5.8 Tasarım notları
- Kasıtlı bozuk baytlar içeren test dosyaları **metin editöründe açılıp kaydedilmemelidir**; editör baytları
  geçerli karakterlere çevirip testi bozar. Dosya kopyalanarak taşınır.
- Büyük/küçük harf eşleştirmesinde `Locale.ROOT` kullanılır (Türkçe "i" sorunu); ay adları için `Locale.ENGLISH`.
- Genişletme: yeni politika = `ErrorPolicy.Kind` + `Pipeline.handleFailure`; yeni dead-letter hedefi
  (ör. veritabanı) = `DeadLetterSink` arayüzünü gerçekleyen yeni sınıf, Pipeline değişmez.