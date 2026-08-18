# Güncel Puan Tablosu — 2026-08-18

> Bu bir **yeniden puanlama** raporudur, baştan yapılan bir full audit değildir (bkz. `docs/audit/99_INCREMENTAL_REAUDIT_PROMPT.md` §12: "doğru soru eski riskleri kapatıp kapatmadığını doğrulamaktır"). Baz alınan puan tablosu `CURRENT_AUDIT_2026-08-17.md`'dir. Puanlar, aynı gün içinde iki ayrı doğrulama turunda (1: cihaz kanıtı, 2: scaffold fix + detekt temizliği) toplanan **gerçek kanıtlara** göre güncellenmiştir. Puanlama standardı `00_MASTER_AUDIT_PROMPT.md`'deki 0-10 ölçeğidir.

## Genel Puan

**7,3 / 10** (gün başı: 7,1 / 10 → cihaz doğrulaması sonrası: 7,2 → scaffold+detekt fix sonrası: **7,3**)

## Puan Değişim Tablosu

| Alan | 2026-08-17 | Tur 1 (cihaz kanıtı) | Tur 2 (scaffold+detekt fix) | Toplam Δ | Değişim Gerekçesi |
|---|---:|---:|---:|---:|---|
| **Product / UX** | 7,1 | 7,1 | **7,3** | **+0,2** | `AchievementsScreen`/`ShopScreen`'de gerçek bir kullanıcı-etkili kusur giderildi: bu iki ekranda **hiç geri butonu yoktu** (`onBack` tanımlı ama kullanılmıyordu), kullanıcı yalnızca sistem geri tuşuyla çıkabiliyordu. Artık `DetailScreenScaffold` ile ikisinde de geri butonu var. |
| **Frontend / Compose** | 7,2 | 7,2 | **7,9** | **+0,7** | Tek kalan kod-seviyeli P0 (scaffold/bottom-nav tutarsızlığı) kapandı — 5 ikincil ekran (`RoutineDetailScreen`, `OssLicensesScreen`, `AchievementsScreen`, `ShopScreen`, `PrivacyPolicyScreen`) artık ortak `DetailScreenScaffold`'a bağlı. Detekt **gerçekten** 0 issue'ya döndü (16 ihlal giderildi: hardcoded dp, LongMethod/CyclomaticComplexity, kullanılmayan import/property). `ShopScreen`'deki hardcoded Türkçe string'ler `strings.xml`'e taşındı. 10 değil 7,9 çünkü: proje genelinde hardcoded string ihlali hâlâ yaygın (örn. `PrivacyPolicyScreen` içeriği, `OnboardingScreen.kt` 834 satır) ve bunlar bu turda dokunulmadı. |
| State / ViewModel | 7,4 | 7,4 | 7,4 | 0,0 | Bu turda hedeflenmedi, kanıt yok |
| Data / Database | 8,0 | 8,0 | 8,0 | 0,0 | Bu turda hedeflenmedi, kanıt yok |
| Backend / Sync readiness | 7,0 | 7,0 | 7,0 | 0,0 | Bu turda hedeflenmedi, kanıt yok |
| Security / Privacy | 7,5 | 7,5 | 7,5 | 0,0 | Değişiklik yok |
| Performance | 6,2 | **7,8** | 7,8 | +1,6 | Cold startup gate gerçek release APK + emülatörde **PASS** (cold median 1446ms, p90 1872ms; eşik 2000/3000ms) |
| Testing / QA | 7,4 | **7,7** | 7,7 | +0,3 | `connectedDebugAndroidTest` 74/74 (önceki 73/74), scaffold fix sonrası tekrar koşuldu, hâlâ 74/74 — regresyon yok |
| Monetization / Release | 5,5 | 5,5 | 5,5 | 0,0 | Değişiklik yok — Billing/IAP hâlâ yok (bilinçli) |

**Aritmetik ortalama:** (7,3+7,9+7,4+8,0+7,0+7,5+7,8+7,7+5,5) / 9 = **7,34** → raporlanan genel puan **7,3 / 10**

## Bu Turda Kapatılan Maddeler (kod kanıtıyla doğrulanmış)

- **Scaffold/bottom-nav tutarlılığı (eski P0) — kapandı.** Yeni ortak bileşen: `DetailScreenScaffold.kt`. 5 ekran buna bağlandı.
- **Achievements/Shop'ta eksik geri butonu (bu turda keşfedilen yeni bulgu) — kapandı.** `onBack` parametreleri artık gerçekten kullanılıyor.
- **`ShopScreen.kt` hardcoded string ihlali (Demir Kural #3) — bu dosyada kapandı**, proje genelinde değil.
- **Detekt kırmızısı (be7aa9d'den beri fark edilmemiş 16 ihlal) — kapandı.** `ScreenScaffold` hardcoded dp, `RoutineDetailScreen`/`PrivacyPolicyScreen` LongMethod/CyclomaticComplexity, 6 dosyada kullanılmayan import/property.

## Yan Bulgu (kod değişikliği yapılmadı, kayıt altına alındı)

`Theme.kt` içindeki kod yorumu "Karanlık mod devre dışı bırakıldı — uygulama tutarlı ve temiz açık modda çalışır" diyor ve `BenimGunlerimTheme` her zaman `LightColors` kullanıyor. Bu, `CLAUDE.md`'deki "Faz A: Token sistemi + tema (karanlık mod) ✅ TAMAMLANDI" iddiasıyla çelişiyor — karanlık mod kodda mevcuttu ama bilinçli olarak kapatılmış görünüyor. Kullanıcı onayı olmadan dokunulmadı.

## Yayın Kararı (güncellenmiş gerekçeyle)

- **Production:** Hayır. Kod-seviyeli tüm bilinen P0'lar kapandı (scaffold, startup, silme testi). Kalan engeller kod değil, dış kanıt: Play Console/Data Safety/vitals, accessibility ve OEM notification matrisi.
- **Internal / Closed Beta:** Evet, uygun.
- **Monetization:** Hayır — Billing/IAP bilinçli olarak yok.

## Güncel P0 Listesi

1. Billing/IAP — bilinçli erteleme, ürün kararı bekliyor.
2. Play Console / accessibility / OEM notification dış kanıtı — kod değil, doğrulama eksikliği.
3. (P1, kod değil) Proje genelinde hardcoded string temizliği tamamlanmadı (`OnboardingScreen.kt` 834 satır dahil) — Demir Kural #3 ihlali hâlâ yaygın.

## Kapsam ve Güvenilirlik Notu

- Değişmeyen 5 alan (State, Data, Backend, Security, Monetization) bu turlarda **yeniden okunmadı** — `CURRENT_AUDIT_2026-08-17.md` puanları aynen taşındı.
- Product/UX, Frontend/Compose, Performance, Testing/QA puanları bu oturumda toplanan **gerçek kanıtla** (kod okuma + compile + detekt + lint + unit test + jacoco + cihazda connectedDebugAndroidTest) güncellendi.
- Scaffold/detekt fix'i sonrası regresyon kontrolü tam zincirle yapıldı: `compileDebugKotlin`, `detekt`, `lintDebug`, `testDebugUnitTest`, `jacocoDebugUnitTestCoverageVerification`, `connectedDebugAndroidTest` — hepsi geçti.
- 7,3 puanı hâlâ "her şey yeniden değerlendirildi" anlamına gelmez — yalnızca kanıtı toplanan/değişen alanlar puanlandı.
