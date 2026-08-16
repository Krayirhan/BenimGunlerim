# 03 — State / ViewModel Audit Prompt

## Rol

Sen kıdemli bir Android architecture ve state management uzmanısın. Benim Günlerim uygulamasında ViewModel, UiState, event handling, StateFlow, one-shot event, dialog state ve günlük veri akışlarını denetle.

## Odak alanları

- `TodayViewModel.kt`
- UiState yapısı
- StateFlow / MutableStateFlow kullanımı
- Event handling
- One-shot UI eventleri
- Dialog/sheet visibility state’i
- FAB menüsü state’i
- ResetDialog state’i
- BrainDumpDialog state’i
- Hafif Gün Modu state’i
- Görev/rutin tamamlanma state’i
- Undo / geri al mantığı
- Process death / state restore riski

## Cevaplaman gereken sorular

1. ViewModel çok fazla sorumluluk taşıyor mu?
2. UI state ile domain state ayrılmış mı?
3. Dialog state’i ViewModel içinde mi, local UI state içinde mi? Doğru yerde mi?
4. One-shot eventler doğru modelle mi yönetiliyor?
5. Snackbar, celebration, navigation eventleri duplicate tetiklenebilir mi?
6. Görev/rutin tamamlamada race condition olabilir mi?
7. Aynı state farklı yerlerde duplicate tutuluyor mu?
8. Hafif Gün Modu günlük sıfırlama mantığı timezone/local date açısından güvenli mi?
9. Brain Dump toplu görev ekleme idempotent mi?
10. Undo/Geri al desteği var mı, yoksa yanlış tamamlamalar kullanıcıyı sıkıştırır mı?
11. UI event isimleri açık mı, yoksa ViewModel içi karmaşa var mı?
12. XP/level/achievement eventleri eklenirse mevcut yapı kaldırır mı?
13. Process death sonrası dialoglar, onboarding, light day mode, selected tab gibi state’ler doğru geri gelir mi?

## Özellikle ara

- `MutableStateFlow` çok fazla yerde dağılmış mı?
- `copy(...)` blokları çok büyük mü?
- ViewModel içinde formatlama, tarih metni, UI string üretimi fazla mı?
- Repository ile ViewModel sınırı net mi?
- State güncellemeleri transaction mantığıyla mı yapılıyor?

## Beklenen öneri tipi

Somut refactor öner:

```md
Önerilen yapı:
- TodayUiState
- TodayEvent
- TodayEffect
- TodayAction
- TodayReducer veya handler fonksiyonları
- DialogState sealed class
```

## Rapor formatına ek tablo

```md
## State Risk Haritası
| State Alanı | Mevcut Durum | Risk | Önerilen Model |
|---|---|---|---|
```
