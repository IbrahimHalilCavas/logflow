# LogFlow Pipeline Engine

LogFlow, bildirimsel (declarative) YAML yapılandırması ile çalışan, modüler ve yüksek performanslı bir log işleme boru hattı mimarisidir.

---

## Increment 5 — Error Handling, Validation, and Dead-Letter Channel

Artım 5 kapsamında sistem; veri hatalarını sessizce yutmak yerine, zengin tanılama bağlamıyla yöneten ve ikincil hata kanallarına yönlendiren topolojik bir hata yönetim mimarisine dönüştürülmüştür.

### Öne Çıkan Mimari Özellikler
1. **Hata Modeli & Ayrımı (`StageException` vs. `PipelineException`):**
   - **`StageException`**: Kayıt düzeyindeki veri/format hataları için fırlatılır. Kurtarılabilir hatadır; boru hattını çökertmez.
   - **`PipelineException`**: Dosya bulunamaması veya yapılandırma hataları gibi sistem düzeyindeki kritik hatalar için kullanılır.
2. **Topolojik Hata Politikaları (`ErrorPolicy`):**
   - **`DEAD_LETTER`**: Hatalı kayıtları yakalar, gerekçesiyle birlikte ikincil `dead-letter.txt` dosyasına sevk eder.
   - **`FAIL_FAST`**: İlk hatada boru hattını güvenli bir şekilde durdurur.
   - **`SKIP`**: Hatalı kayıtları atlayarak sayaçları günceller.
3. **Bildirimsel Doğrulama (`ValidationStage`):**
   - 100 KB üzeri aşırı uzun satır kontrolü
   - Geçersiz UTF-8 / bozuk bayt dizilimi engelleme
   - Mantıksız zaman damgaları (1970 Epoch / 2099 Gelecek Tarih) denetimi
   - Negatif bayt miktarı ve kesilmiş (truncated) satır koruması
4. **Çalıştırma Özeti (`RunSummary`):**
   - İşlem sonunda okunan toplam kayıt, ana hedefe aktarılan, atlanan ve dead-letter kanalına yönlendirilen döküm hesabını eksiksiz sunar.

---

## Kaos Testi Sayısal Mutabakatı (`configs/chaos-test.yaml`)

`data/access-dirty.log` girdi dosyası üzerindeki doğrulama sonuçları:

- **Okunan Toplam Kayıt:** 10
- **Ana Hedefe Yönlendirilen (Valid Output):** 3 (`GET /index.html`, `POST /api/login`, `GET /images/logo.png`)
- **Atlanan (Skipped):** 0
- **Dead-Letter Kanalına Aktarılan (Bozuk Kayıtlar):** 7
- **Sayısal Mutabakat:** $10 = 3 + 0 + 7$ (Tam tutarlı)

---

## Projeyi Derleme ve Çalıştırma

### 1. Derleme
```powershell
javac -d bin (Get-ChildItem -Path "src\logflow\*.java", "test\logflow\*.java").FullName