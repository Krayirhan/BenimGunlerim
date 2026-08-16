# 02 — Frontend / Jetpack Compose Audit Prompt

## Rol

Sen kıdemli bir Android Jetpack Compose mimarı ve frontend kalite denetçisisin. Benim Günlerim uygulamasının Compose UI yapısını, component mimarisini, tema kullanımını, reusable component seviyesini, preview kalitesini ve UI sürdürülebilirliğini denetle.

## İncelenecek alanlar

- Composable dosya boyutları
- `TodayScreen.kt`
- `TodaySheets.kt`
- `ResetDialog.kt`
- `BrainDumpDialog.kt`
- Global `AppTopBar`
- Bottom navigation component
- FAB hızlı eylemler sheet’i
- Theme/color/token kullanımı
- Typography ve spacing tutarlılığı
- Dialog/bottom sheet standardı
- Preview coverage
- Accessibility ve touch target standardı
- Hardcoded string/color/dp kullanımları

## Cevaplaman gereken sorular

1. Büyük Composable dosyaları var mı?
2. UI bileşenleri mantıklı şekilde bölünmüş mü?
3. `TodayScreen.kt` hâlâ fazla sorumluluk taşıyor mu?
4. Dialog ve sheet bileşenleri tekrar kullanılabilir mi?
5. `AppTopBar` gerçekten global component mi, yoksa ekranlara kopyalanmış mı?
6. Theme tokenları kullanılıyor mu, hardcoded renkler hâlâ var mı?
7. Spacing ve corner radius tutarlı mı?
8. Cards/list/rows için ortak component standardı var mı?
9. Previewlar gerçek state varyasyonlarını gösteriyor mu?
10. Dark mode destekleniyorsa topbar, cards, dialogs, sheets doğru görünüyor mu?
11. Compose recomposition açısından gereksiz state okumaları var mı?
12. Accessibility contentDescription ve minimum 48dp touch target sağlanmış mı?

## Dosya bazlı özel kontrol

Aşağıdaki dosyaları özellikle incele, varsa eşdeğer dosyaları bul:

- `TodayScreen.kt`
- `TodaySheets.kt`
- `TodayViewModel.kt` içindeki UI state kullanımları
- `ResetDialog.kt`
- `BrainDumpDialog.kt`
- `CategoryPalette.kt`
- Topbar component dosyası
- Theme/Color/Type dosyaları

## Beklenen öneri tipi

Sadece “dosyayı böl” deme. Nasıl bölüneceğini belirt:

```md
TodayScreen.kt şu parçalara ayrılmalı:
- TodayRoute
- TodayContent
- TodayProgressCard
- TaskSection
- RoutineSection
- LightDayBanner
- ResetSuggestionCard
- TodayFabActionSheet
```

## Rapor formatına ek tablo

```md
## Component Sağlığı
| Component/Dosya | Durum | Risk | Önerilen Aksiyon |
|---|---|---|---|
```
