# Performans Politikası

Bu doküman release öncesi performans kapısını standardize eder.

## Ölçüm Kapsamı

- Startup ölçümü en az bir fiziksel cihazda alınır.
- Ölçümler cold ve warm startup için ayrı raporlanır.
- Her ölçüm en az 10 iterasyon içerir.
- Rapor: median, p90, min, max.
- Macrobenchmark modülü (`:benchmark`) startup ve frame timing ölçümleri üretir.

## Threshold'lar

- Cold startup median: `< 2000 ms`
- Cold startup p90: `< 3000 ms`
- Warm startup median: `< 900 ms`
- Startup max spike: `< 4000 ms`

## Gate Komutları

```powershell
./scripts/check-performance-gate.ps1
```

Macrobenchmark için:

```powershell
./gradlew.bat :benchmark:connectedCheck
```

Tek ölçüm için:

```powershell
./scripts/measure-startup-perf.ps1 -StartupMode cold -FailOnThreshold
./scripts/measure-startup-perf.ps1 -StartupMode warm -SkipInstall -FailOnThreshold
```

## Uygulama Başlangıç İlkeleri

- Startup'ta zorunlu olmayan iş yükleri arkaplanda çalıştırılır.
- Reminder restore DB/IO işlemleri `Dispatchers.IO` üzerinde kalır.
- Debug StrictMode release'e taşınmaz.
- ANR riski taşıyan işler WorkManager veya gecikmeli iş akışına alınır.

## Büyük Veri Senaryoları (Manual Deep Gate)

- 1000+ task
- 10000 completion log
- 365 daily state
- 500 routine

Bu senaryolarda Today/Progress/Routines açılış ve scroll jank gözlemlenir.

## CI Deep Gate

- `.github/workflows/android.yml` içinde `performance-deep-gate` job'ı nightly (`schedule`) ve manuel (`workflow_dispatch`) tetiklenir.
- Job çıktıları benchmark artifact olarak saklanır.