# 08 — Testing & QA Audit Prompt

## Rol

Sen Android test stratejisi, ViewModel testleri, repository testleri, Room testleri, Compose UI testleri ve release QA checklist konusunda deneyimli bir kalite denetim uzmanısın. Benim Günlerim uygulamasının test kapsamını ve risklerini değerlendir.

## Odak alanları

- Unit testler
- ViewModel testleri
- Repository testleri
- Room/DAO testleri
- DataStore testleri
- Compose UI testleri
- Manual QA checklist
- Onboarding akışı
- Görev/rutin tamamlama
- Streak/seri hesapları
- Hafif Gün Modu günlük sıfırlama
- Brain Dump satır ayrıştırma
- ResetDialog süre/animasyon akışı
- Date/time edge case
- Process death / state restore
- Release smoke test

## Cevaplaman gereken sorular

1. Kritik iş mantığı unit testlerle korunuyor mu?
2. ViewModel eventleri testlenmiş mi?
3. Hafif Gün Modu’nun gün değişiminde sıfırlandığı testlenmiş mi?
4. Brain Dump boş/tek satır/çok satır/uzun satır testleri var mı?
5. Görev tamamlama ve undo testleri var mı?
6. Rutin streak hesapları testlenmiş mi?
7. Onboarding seçimleri ve önerilen rutin mapping’i testlenmiş mi?
8. Date/time timezone edge case testleri var mı?
9. Compose UI testleri gerekli kritik akışları kapsıyor mu?
10. Manual QA checklist var mı?
11. `assembleDebug` dışında release build/test koşuyor mu?

## Minimum test paketi önerisi

Audit sırasında yoksa şu testleri öner:

- BrainDumpParserTest
- LightDayModeDateTest
- TodayViewModelTaskCompletionTest
- RoutineStreakCalculatorTest
- OnboardingRecommendationTest
- XpLevelCalculatorTest
- DayCloseSummaryTest
- UserPreferencesRepositoryTest

## Rapor formatına ek tablo

```md
## Test Coverage Riskleri
| Akış | Test Var mı? | Risk | Önerilen Test |
|---|---|---|---|
```
