# 07 — Performance Audit Prompt

## Rol

Sen kıdemli Android performans ve Jetpack Compose optimizasyon uzmanısın. Benim Günlerim uygulamasının recomposition, list performance, animation, startup, database query ve memory açısından performans denetimini yap.

## Odak alanları

- Today ekranı recomposition
- LazyColumn/LazyList kullanımı
- Liste item key kullanımı
- Çok görev/rutin senaryosu
- Nefes animasyonu performansı
- ResetDialog animasyonu
- BrainDumpDialog büyük metin performansı
- FAB sheet performansı
- Konfetti/Lottie eklenirse riskler
- Room/DataStore queryleri
- Main thread bloklama
- Startup time
- APK boyutu ve dependency yükü

## Cevaplaman gereken sorular

1. Today ekranında gereksiz recomposition var mı?
2. Büyük listelerde UI akıcı kalır mı?
3. LazyColumn item key kullanılıyor mu?
4. State okuma alanları fazla geniş mi?
5. Animasyonlar düşük cihazlarda sorun çıkarır mı?
6. Nefes animasyonu sürekli recomposition yaratıyor mu?
7. Brain Dump uzun metinde yavaşlar mı?
8. Toplu görev ekleme main thread’i bloklar mı?
9. Database queryleri Flow ile doğru mu gözleniyor?
10. Konfetti/Lottie gibi bağımlılıklar eklenirse boyut/performance riski nedir?
11. Release build optimizasyonları hazır mı?

## Ölçüm önerileri

- Layout Inspector / recomposition count
- Macrobenchmark varsa incele
- Baseline profile düşün
- 100 görev + 50 rutin test senaryosu
- 500 satırlık Brain Dump test senaryosu
- Düşük RAM emülatör testi

## Rapor formatına ek tablo

```md
## Performance Riskleri
| Alan | Risk | Senaryo | Öneri |
|---|---|---|---|
```
