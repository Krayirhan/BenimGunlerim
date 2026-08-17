# Dış Release Kurulum Kontrolü

Bu dosya repo dışındaki production ayarlarını tamamlamak için kullanılır. Bu ayarlar kodla otomatik uygulanamaz; GitHub ve Play Console üzerinde yetkili kullanıcı tarafından yapılmalıdır.

## GitHub Secrets

Repository secrets:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

`KEYSTORE_BASE64` üretimi:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("benimgunlerim-release.jks"))
```

## Branch Protection

`main` branch için required status checks:

- `PR quality gate`
- `Release quality gate`
- `Connected UI tests`

Ek kurallar:

- Direct push kapalı.
- PR review zorunlu.
- Stale approval dismiss açık.
- Force push kapalı.

## Tag Protection

Release tag formatı:

- `v*`

Release tag oluşturulunca release-quality job signed AAB üretmelidir.

## Play Console

Internal testing release kontrolü:

- Signed AAB yüklendi.
- Version code önceki release'ten büyük.
- Türkçe release notes girildi.
- Data Safety formu privacy policy ile uyumlu.
- Internal tester smoke test tamamlandı.

## Monitoring

Repo içinde `ErrorReporter` ve uncaught exception hook hazırdır. Karar verildi (2026-08-17): **tamamen lokal/offline tanı modeli** — uygulama hiçbir üçüncü taraf crash reporting servisine (Crashlytics, Sentry vb.) bağlı değil, `LocalErrorReporter` çökme/hata kayıtlarını yalnızca cihazda tutar.

Bu karar nedeniyle:

- Crash-free users / ANR oranı gibi merkezi metrikler bu araçla izlenemez; Play Console Android Vitals tek kaynak olur.
- Version bazlı release health, Play Console üzerinden izlenir.
- Kritik artışlarda rollback veya rollout pause uygulanır.
