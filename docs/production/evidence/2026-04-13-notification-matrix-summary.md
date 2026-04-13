# Notification Matrix Evidence (2026-04-13)

Kaynak: fiziksel cihaz smoke testi (`scripts/run-notification-smoke-matrix.ps1`)

Koşu özeti:

- Cihaz sınıfı: Samsung SM-S711B
- Android sürümü: 16
- Test sınıfı: `NotificationMatrixSmokeTest`
- Toplam test: 4
- Failures: 0
- Errors: 0

Doğrulanan başlıklar:

- Notification channel oluşturma
- Scheduler schedule/cancel akışları
- Receiver manifest kaydı
- BootReceiver ilgisiz action güvenliği

Ham `dumpsys notification` ve `dumpsys alarm` çıktıları commit edilmez; sadece sanitize edilmiş özet repoda tutulur.