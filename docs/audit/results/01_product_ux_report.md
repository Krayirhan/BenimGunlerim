# Audit Raporu — Ürün / UX

## Genel Puan
6 / 10

## Kısa Karar
Uygulamanın ürün fikri net, Türkçe metin kalitesi ve ton disiplini beklenenin üzerinde, çekirdek döngü (görev → rutin → gün kapatma → XP) gerçekten çalışıyor ve kötü niyetli/suçlayıcı dil yok. Ancak proje kendi CLAUDE.md'sinde "Faz A-E TAMAMLANDI" dese de kod bunun tersini gösteriyor: ekran dosyalarının büyük kısmı 200 satır sınırının 2-4 katı üzerinde, `Common.kt`'nin yerini alması gereken bileşenler yine de yüzlerce satırlık "her şeyi yapan" dosyalara dönüşmüş, ölü kod (kullanılmayan `PlanTaskListComponents.kt`, `CelebrationSystem.kt`) ağaçta duruyor ve rutin hedef takibi (artır/azalt) hiçbir ekranda gerçek bir kontrol olarak mevcut değil. Ayrıca Ayarlar'daki "Kutlama efektleri" anahtarı hiçbir yeri etkilemeyen bir plasebo. Bu haliyle ürün beta/iç test için uygundur; yayın öncesi mimari borç, eksik rutin etkileşimi ve tutarsız uygulama iskeleti (RoutineDetail/OssLicenses kendi Scaffold'unu çiziyor) kapatılmadan mağazaya çıkmamalı.

## En Güçlü 5 Taraf
1. **Türkçe metin ve ton kalitesi gerçekten iyi.** `strings.xml` (681 satır) genelinde suçlayıcı/yargılayıcı dil hiçbir yerde bulunamadı; boş durumlar ("Bugün için henüz görev yok... ekleyerek başlayabilirsin"), gün sonu ("Bugün kendin için bir adım attın") ve Hafif Gün Modu ("Bugünü iptal etmiyoruz, sadece hafifletiyoruz") DESIGN.md'nin ton tablosuyla birebir örtüşüyor.
2. **Onboarding kişiselleştirmesi sahte değil, gerçek.** `OnboardingScreen.kt:104-144` içindeki `suggestedRoutines(needId, intensityId)` ve `suggestedTaskTitle(needId)` fonksiyonları seçilen ihtiyaç/temposuna göre gerçekten farklı rutin/görev listeleri üretiyor ve bunlar `OnboardingViewModel.completeOnboarding` ile veritabanına yazılıyor.
3. **Gün kaçırma akışı destekleyici, suçlayıcı değil.** `AutoCloseMissedDayUseCase.kt` sessizce arka planda kapatıyor; `MissedDayReviewSheet.kt:125` "Dünkü emeklerin korundu" gibi güven veren bir dille özetliyor, kullanıcıyı zorunlu itiraf akışına sokmuyor.
4. **Hafif Gün Modu güvenli biçimde tarih bazlı.** `UserPreferencesRepository.kt:348-352` bir boolean değil tarih string'i tutuyor; `TodayViewModel.kt:247`'deki `lightDayModeDate == today()` karşılaştırması gün değiştiğinde otomatik sıfırlanıyor, "takılı kalma" riski yok.
5. **Ödül/level motoru temiz ve idempotent.** `RewardGrantService.kt` tek seferlik ödülleri `eventKey` bazlı `grantOnce` ile veriyor, `GameEngine.kt` seviye eşiklerini net bir tablo üzerinden hesaplıyor — domain katmanı, UI katmanının aksine disiplinli.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Rutin listesinde check-tipi ile hedef/sayaç-tipi rutinler arasında hiçbir görsel fark yok; artır/azalt kontrolü **hiçbir ekranda** mevcut değil | Kullanıcı "8 bardak su" gibi hedefli bir rutini günlük akışta ilerletemiyor — çekirdek özellik fiilen çalışmıyor | `RoutineRow.kt` (48-231) her rutine aynı check/uncheck ikonunu basıyor; `RoutinesScreen.kt:139-150` `targetType/targetValue/targetUnit`'i `RoutineRow`'a hiç iletmiyor; UI'da `increment/decrement/stepper` için sıfır eşleşme | `RoutineRow`'a hedef tipini ilet, sayaç/miktar rutinleri için +/- kontrollerini listede göster |
| P0 | Ayarlar'daki "Kutlama efektleri" anahtarı hiçbir yeri etkilemiyor | Kullanıcı animasyonu/konfeti kapattığını sanıyor ama level-up/başarım modalları ve konfeti yine tam ekran açılıyor — erişilebilirlik/hareket-azaltma vaadi kırık | `UserPreferencesRepository` alanı (`celebrationEffectsEnabled`) `TodayScreen.kt:168-196,659-677` ve `CelebrationModals.kt`'de hiç okunmuyor | Kutlama tetikleyicilerini `prefs.celebrationEffectsEnabled` ile koşullandır |
| P0 | Ana ekranlar arası ortak iskelet tutarsız | DESIGN.md "ortak scaffold yeniden üretilmez" kuralı ihlal ediliyor; kullanıcı bazı ekranlarda topbar/bottom nav'ı kaybediyor | `RoutineDetailScreen.kt:66-85` ve `OssLicensesScreen.kt` kendi `Scaffold`/`TopAppBar`'ını çiziyor; Başarımlar ve Dükkan rotaları `Destination` enum dışında olduğu için bottom nav hiç görünmüyor (`AppNavigation.kt:122, 303-308`) | Tüm rotaları tek `Destination`/scaffold sözleşmesine bağla veya bilinçli istisnaları DESIGN.md'ye yaz |
| P0 | Ekran dosyalarının büyük kısmı 200 satır sınırının 2-4,5 katı üzerinde; proje kendi mimari geçişini "TAMAMLANDI" ilan etmiş | Teknik borç dokümantasyonla gerçek kod arasında güven kaybı yaratıyor; sürdürülebilirlik zayıf | `TodaySheets.kt` 914, `OnboardingScreen.kt` 883, `TodayScreen.kt` 767, `TodayViewModel.kt` 740, `CelebrationModals.kt` 530, `ResetDialog.kt` 388, `PlanTaskListComponents.kt` 428 (ölü kod), `SettingsScreen.kt` 341, `RoutineDetailScreen.kt` 326 satır | Faz E'yi gerçekten bitir: bu dosyaları `organisms/`e böl, CLAUDE.md'deki durumu güncelle |
| P1 | Rutin arşivleme onaysız, geri alınamaz biçimde çalışıyor | Tek dokunuşla kalıcı veri kaybı; uygulamanın kendi görev-silme akışındaki "Geri Al" snackbar deseniyle tutarsız | `RoutineDetailScreen.kt:76-78` — `IconButton` doğrudan `archiveRoutine(); onBack()` çağırıyor, `confirm|AlertDialog` için sıfır eşleşme | Onay diyaloğu veya en azından geri-alınabilir snackbar ekle |
| P1 | Görev/rutin ekleme sheet'leri, callback imzasında var olan alanları (kategori, saat, hatırlatıcı, hedef miktar/birim) hiç UI olarak sunmuyor | Kullanıcı bu alanları hiç dolduramıyor; kod "kısmen bitmiş" görünümü veriyor | `AddTaskSheet.kt` yalnız başlık+öncelik render ediyor, `category/startTime/reminderTime` sabit `null`/boş geçiliyor (satır 42-44, 121); `AddRoutineSheet.kt` yalnız isim+gün render ediyor, `category` sabit `"Genel"` (satır 47) | Alanları ya UI'ya ekle ya da imzadan çıkar |
| P1 | Ölü/duplike kod ağaçta duruyor | Yanlışlıkla yeniden bağlanma riski, kafa karışıklığı, gereksiz APK boyutu; `CelebrationSystem.kt` tam ekran, "SEVİYE ATLADIN!" gibi bağırgan kopya içeren, DESIGN.md'nin "ölçülü kutlama" ilkesine aykırı bir alternatif kutlama sistemi | `PlanTaskListComponents.kt` (428 satır, hiçbir çağrı sitesi yok — `PlanScreen.kt` bunun yerine `PlanListContainers.kt`'yi kullanıyor); `ui/components/CelebrationSystem.kt` `TodayScreen.kt`'den hiç çağrılmıyor | İkisini de sil veya bilinçli olarak arşivle/dokümante et |
| P1 | Hemen her incelenen dosyada hardcoded dp/Color/Türkçe string — "Demir Kurallar" sistematik ihlal ediliyor | Token sistemi (Faz A/B) fiilen bypass edilmiş; tema/spacing değişikliği tutarsız sonuç verir | `AppTopBar.kt:63-69` 13+ hex renk; `AddTaskSheet.kt:59-116` hardcoded Türkçe ("Yeni Görev Ekle", "Kaydet" vb.) — kardeş dosya `AddRoutineSheet.kt` doğru `stringResource` kullanıyor; `LevelHeroCard.kt:41-43`, `CelebrationModals.kt` çok sayıda `Color(0xFF...)` | Detekt/ktlint'e custom kural ekleyip CI'da yakalanacak hale getir |
| P1 | `TodayViewModel` 26 bağımlılık enjekte eden, 740 satırlık "god ViewModel" | Test edilebilirlik ve değişiklik güvenliği düşük; detekt baseline'da zaten `LargeClass` olarak işaretli | `TodayViewModel.kt` constructor'ı 26 use case/repo enjekte ediyor (satır 111-144); `config/detekt/baseline.xml`: `LargeClass:TodayViewModel.kt$TodayViewModel : ViewModel` | Görev/rutin/gün-kapatma/hafif-gün akışlarını ayrı ViewModel'lere böl veya orkestratör use case'lere devret |
| P2 | Gerçek para ile satın alma (IAP/billing) kod tabanında tamamen yok, versionName 0.1.0 | Yayın/monetizasyon planı henüz teknik olarak yok; Dükkan yalnızca sanal altınla çalışıyor | `app/build.gradle.kts` bağımlılıklarında billing/IAP kütüphanesi yok; repo genelinde `billing|BillingClient|Purchase(` için sıfır eşleşme | Monetizasyon stratejisi netleşmeden mağaza listelemesi yapılmamalı; Dükkan şu an tamamen kozmetik/XP-ekonomisi içi |

## Dosya Bazlı Bulgular

### `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt`
- Bulgu: 767 satır; hardcoded Türkçe string'ler doğrudan Kotlin içinde (`"✓ Görev tamamlandı · +10 XP"` satır 148; `"Bugünün görevleri tamamlandı 🎉"` satır 178-181; `"Görev Ekle"`, `"Aklındakileri dök ve göreve dönüştür"` satır 497-511); onlarca `dp`/`Color(...)` literali (satır 253-388 arası kontekstüel banner'lar).
- Risk: 200 satır kuralı ve "hardcoded string/renk yasak" kuralı doğrudan ihlal ediliyor; bu dosya CLAUDE.md'nin "Faz E tamamlandı" iddiasıyla çelişiyor.
- Öneri: FAB menüsü, bağlamsal reset kartı ve Hafif Gün Modu banner'ı ayrı organism dosyalarına taşınmalı; tüm string'ler `strings.xml`'e çekilmeli.

### `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt`
- Bulgu: 740 satır, 26 enjekte edilmiş bağımlılık; görev, rutin, alt-görev, gün kapatma, kaçırılan gün, hafif gün modu ve brain-dump mantığının tamamını tek sınıfta topluyor.
- Risk: Detekt baseline'da zaten `LargeClass` olarak işaretli; değişiklik riski yüksek, birim test yazımı zorlaşıyor.
- Öneri: Sorumlulukları (gün kapatma, rutin, hafif gün) ayrı ViewModel/UseCase orkestrasyonuna böl.

### `app/src/main/java/com/benimgunlerim/ui/onboarding/OnboardingScreen.kt`
- Bulgu: 883 satır; 78+ hardcoded `dp` literali, `Color.White` ve ham tema renk sabitleri (`BrandPrimary`, `CompletedGreen`) doğrudan kullanılmış; buna karşın kullanıcıya görünen tüm metinler doğru şekilde `stringResource` üzerinden geliyor (bu noktada temiz).
- Risk: 200 satır kuralı büyük farkla ihlal ediliyor; token sistemi bypass edilmiş.
- Öneri: 4 adım + 3 alt bileşen (`NeedCard`, `IntensityCard`, `SelectableRoutineRow`) `organisms/`e taşınmalı.

### `app/src/main/java/com/benimgunlerim/ui/today/TodaySheets.kt`
- Bulgu: 914 satır — incelenen en büyük dosya; gün kapatma sihirbazı (4 adım) burada yaşıyor, çok sayıda ham `dp` literali içeriyor.
- Risk: Tek dosyada hem UI hem çok adımlı state mantığı birleşmiş; bakım maliyeti yüksek.
- Öneri: Her adımı (özet/mood/yansıma/yarın) ayrı composable dosyalarına böl.

### `app/src/main/java/com/benimgunlerim/ui/components/organisms/RoutineRow.kt` ve `ui/routines/RoutinesScreen.kt`
- Bulgu: `RoutinesScreen.kt:139-150` `RoutineRow`'a `targetType/targetValue/targetUnit` iletmiyor; `RoutineRow` her rutini aynı check/uncheck ikonuyla gösteriyor. Artır/azalt (`increment/decrement`) kontrolü UI katmanının hiçbir yerinde yok (yalnızca domain modelinde `targetType` alanı var).
- Risk: Sayaç/miktar tipi rutinler (örn. "2 litre su iç") günlük akışta fiilen ilerletilemiyor — ürünün kendi DESIGN.md'sinde vaat ettiği "Check tipi ve hedef tipi rutinler görsel olarak ayırt edilir" ve "Artır/azalt eylemleri... yeterli dokunma alanına sahip olur" kuralları karşılanmıyor.
- Öneri: Liste satırına hedef tipine göre progress/sayaç UI'sı ve +/- kontrolü ekle.

### `app/src/main/java/com/benimgunlerim/ui/routines/RoutineDetailScreen.kt`
- Bulgu: 326 satır; sabit yükseklik kullanımı (`Modifier.height(if (completed) 32.dp else 16.dp)` satır 209, `.height(50.dp)` satır 274) DESIGN.md'nin "sabit yükseklik yasak, IntrinsicSize.Min kullan" kuralını ihlal ediyor; arşivleme onaysız (`archiveRoutine(); onBack()` satır 76-78).
- Risk: Görsel taşma riski (büyük font/tablet), kalıcı veri kaybı riski.
- Öneri: `IntrinsicSize.Min` kullan; arşivlemeye onay diyaloğu ekle.

### `app/src/main/java/com/benimgunlerim/ui/components/organisms/AddTaskSheet.kt`
- Bulgu: Hardcoded Türkçe string'ler (`"Yeni Görev Ekle"` satır 59, `"Görev Başlığı"` satır 70, `"Öncelik"`, `"Yüksek/Normal/Düşük"`, `"İptal"`, `"Kaydet"`); `date/startTime/category/reminderTime` alanları callback imzasında var ama hiçbir input render edilmiyor.
- Risk: Kardeş dosya `AddRoutineSheet.kt` doğru `stringResource` kullanırken bu dosya kullanmıyor — tutarsızlık; kullanıcı saat/kategori/hatırlatıcı ayarlayamıyor.
- Öneri: String'leri `strings.xml`'e taşı; eksik alanları ekle veya "gelişmiş alanlar" olarak (Today'deki gibi) sun.

### `app/src/main/java/com/benimgunlerim/ui/components/layout/AppTopBar.kt`
- Bulgu: 13+ hardcoded hex renk (`Color(0xFF2A372E)` vb., satır 63-69) ve hardcoded content description'lar (`"Profil"` satır 99, `"Bildirimler"` satır 164) — `strings.xml` yerine literal.
- Risk: Global, tüm ana ekranlarda kullanılan bir bileşende token/tema tutarsızlığı; TalkBack açıklamaları çeviri sürecinin dışında kalıyor.
- Öneri: `MaterialTheme.colorScheme.*` ve `stringResource` kullan.

### `app/src/main/java/com/benimgunlerim/ui/settings/SettingsScreen.kt`
- Bulgu: 341 satır; "Kutlama efektleri" anahtarı (satır 77-82) `UserPreferencesRepository`'de doğru saklanıyor ama hiçbir kutlama tetikleyicisinde okunmuyor; ayrıca `"Başlangıç Rehberi (Onboarding)"`, `"Açık kaynak lisansları"` gibi hardcoded string'ler var (satır 118-126). "Tüm verileri temizle" akışı ise doğru şekilde `AlertDialog` ile onay istiyor (satır 150-212) — bu kısım iyi çalışıyor.
- Risk: Kullanıcıya yanlış kontrol vaadi (erişilebilirlik/hareket azaltma beklentisi kırılıyor).
- Öneri: Kutlama tetikleyicilerini `prefs.celebrationEffectsEnabled` ile koşullandır; kalan hardcoded string'leri taşı.

### `app/src/main/java/com/benimgunlerim/ui/components/CelebrationSystem.kt`
- Bulgu: `ConfettiOverlay`/`LevelUpOverlay`/`AchievementUnlockOverlay` — tam ekranı karartan (`Color.Black.copy(alpha=0.6f)`), büyük harfli ("SEVİYE ATLADIN!", "BAŞARIM AÇILDI!") bir kutlama sistemi; `TodayScreen.kt` bunun yerine daha ölçülü `CelebrationModals.kt`'yi çağırıyor, bu dosya hiç kullanılmıyor.
- Risk: Ölü kod; DESIGN.md'nin "konfeti/rozet/animasyonlar kısa sürer, içerikten rol çalmaz" ilkesine aykırı bir alternatif hâlâ derleniyor ve yanlışlıkla bağlanma riski taşıyor.
- Öneri: Dosyayı sil veya bilinçli olarak `@Deprecated`/arşiv klasörüne taşı.

### `app/src/main/java/com/benimgunlerim/ui/plan/PlanTaskListComponents.kt`
- Bulgu: 428 satır, hiçbir çağrı sitesi yok (`PlanScreen.kt` `PlanListContainers.kt`'yi kullanıyor); içinde ham `dp`/`Color` literalleri ve sabit yükseklik (`Modifier.height(40.dp)` satır 195) var.
- Risk: Ölü kod, kafa karıştırıcı; gelecekte yanlışlıkla import edilirse token ihlallerini geri getirir.
- Öneri: Sil.

## Kullanıcı Deneyimi Etkisi
Genel akış (görev ekle → tamamla → rutin işaretle → günü kapat) sorunsuz ve duygusal olarak doğru tonda çalışıyor; bu, ürünün en güçlü tarafı. Ancak iki somut kullanıcı-görünür kırılma var: (1) rutin hedef takibi listede fiilen çalışmıyor — su/adım gibi sayaç rutinleri olan kullanıcılar günlük akışta ilerleme kaydedemiyor, bu da "alışkanlık uygulaması" vaadinin çekirdeğini zedeliyor; (2) "Kutlama efektlerini" kapatan bir kullanıcı yine de tam ekran level-up/başarım modalı görüyor — bu, ayarların güvenilirliğine dair güveni kırar, özellikle hareket duyarlılığı olan kullanıcılar için erişilebilirlik sorunu. Rutin arşivleme onaysız oluşu da (diğer akışlarda "Geri Al" alışkanlığı kurulmuşken) beklenmedik veri kaybına yol açabilir.

## Teknik Borç Etkisi
CLAUDE.md, Faz A-E'nin (token sistemi → moleküller → organizmalar → ekran yeniden yazımı → `Common.kt` silme) 30 Temmuz 2026'da tamamlandığını belgeliyor. Kod tabanı bunu büyük ölçüde doğrulamıyor: dokuz ekran/bileşen dosyası 200 satır sınırının üzerinde (bazıları 4x üzerinde), hardcoded dp/renk/string ihlalleri neredeyse her incelenen dosyada mevcut, ve iki tam dosya (`PlanTaskListComponents.kt`, `CelebrationSystem.kt`) hiç kullanılmadığı halde ağaçta duruyor. Detekt baseline dosyası da bunu doğruluyor — `LargeClass:TodayViewModel.kt`, çok sayıda `LongMethod`/`CyclomaticComplexMethod` girişi hâlâ "bilinen borç" olarak bastırılmış durumda, yani CI bu sorunları görmezden geliyor. Bu, dokümantasyon ile kod arasındaki güveni zedeliyor ve yeni katılan bir geliştiricinin/ajanın yanlış varsayımlarla ilerlemesine yol açar.

## Release / Monetizasyon Riski
- **Monetizasyon**: `build.gradle.kts`'de billing/IAP kütüphanesi yok, `versionName = "0.1.0"` — ürün henüz gelir modeline sahip değil; Dükkan tamamen sanal altın ekonomisiyle çalışıyor. Yayın öncesi bir gelir stratejisi (freemium, reklamsız, vb.) belirlenmeden mağaza listelemesi erken olur.
- **Veri/gizlilik**: `AndroidManifest.xml` minimal ve makul izinler istiyor (INTERNET, POST_NOTIFICATIONS, VIBRATE, RECEIVE_BOOT_COMPLETED); "Tüm verileri temizle" akışı doğru onay diyaloğuyla korunuyor (`SettingsScreen.kt:150-212`); dışa aktarma ekranında PII uyarısı var (`settings_data_export_warning`). Bu alan iyi durumda.
- **Erişilebilirlik vaadi**: DESIGN.md §10 "Kutlama efekti kullanıcı ayarlarından kapatılabilir olmalıdır" diyor; kod bunu ihlal ediyor (bkz. P0 madde 2). Play Store incelemesi veya erişilebilirlik denetimi bu tutarsızlığı yakalayabilir.
- **Fonksiyonel eksiklik**: Hedef tipi rutinlerde artır/azalt kontrolünün UI'da hiç bulunmaması, "rutin takibi" temel vaadinin bir alt kümesinin yayında çalışmadığı anlamına geliyor — bu bir P0 release blokeri olarak değerlendirilmeli.

## Önceliklendirilmiş Yapılacaklar
### P0 — Yayın öncesi şart
- Rutin listesinde hedef/sayaç tipi rutinler için gerçek artır/azalt kontrolü ekle (`RoutineRow.kt`, `RoutinesScreen.kt`).
- "Kutlama efektleri" ayarını gerçekten kutlama tetikleyicilerine bağla (`TodayScreen.kt`, `CelebrationModals.kt`).
- Tüm ana rotalarda (Rutin Detay, Başarımlar, Dükkan, OSS Lisansları) ortak `AppTopBar`/bottom nav sözleşmesini uygula veya istisnaları DESIGN.md'ye açıkça yaz.
- Rutin arşivlemeye onay/geri-alma ekle.

### P1 — Kısa vadede gerekli
- `TodayScreen.kt`, `TodayViewModel.kt`, `TodaySheets.kt`, `OnboardingScreen.kt`, `SettingsScreen.kt`, `RoutineDetailScreen.kt`, `ResetDialog.kt`, `CelebrationModals.kt` dosyalarını 200 satır kuralına uygun şekilde böl.
- `AddTaskSheet.kt`/`AddRoutineSheet.kt`'teki eksik alanları tamamla veya kaldır; hardcoded string'leri `strings.xml`'e taşı.
- Ölü kodu (`PlanTaskListComponents.kt`, `CelebrationSystem.kt`) sil.
- `AppTopBar.kt`, `LevelHeroCard.kt`, `CelebrationModals.kt` içindeki hardcoded renkleri `MaterialTheme.colorScheme`'e taşı.

### P2 — Polish / ileri iyileştirme
- `WeekPicker.kt`'deki önceki/sonraki hafta ikon butonlarını 48dp'ye çıkar (şu an 36dp).
- Dört ayrı mood string seti (`mood_*`, `today_mood_*`, `progress_mood_*`, `today_close_step1_mood_*`) arasındaki büyük/küçük harf tutarsızlığını ("Çok Kötü" / "Çok kötü") gider, tek kaynağa indir.
- Monetizasyon stratejisini netleştir (freemium/reklamsız/bağış) ve teknik plana bağla.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: Rutin artır/azalt kontrolünü listeye ekle (P0, en yüksek kullanıcı etkisi).
- Gün 2-3: Kutlama ayarı bağlantısını düzelt; rutin arşivlemeye onay ekle.
- Gün 3-4: Uygulama iskeleti tutarsızlığını gider (Rutin Detay, Başarımlar, Dükkan, OSS Lisansları ortak scaffold'a bağla).
- Gün 4-5: Ölü kodu sil (`PlanTaskListComponents.kt`, `CelebrationSystem.kt`); `AddTaskSheet.kt` hardcoded string'lerini taşı.
- Gün 5: Regresyon testi — görev/rutin/gün kapatma akışlarını manuel doğrula.

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki P0 maddelerinin tamamı + `TodayViewModel.kt`'in en az iki sorumluluğunu (hafif gün modu, kaçırılan gün) ayrı sınıflara çıkar.
- Hafta 2: Kalan büyük dosyaları (`TodayScreen.kt`, `TodaySheets.kt`, `OnboardingScreen.kt`, `SettingsScreen.kt`, `RoutineDetailScreen.kt`, `ResetDialog.kt`) 200 satır hedefine göre böl; hardcoded dp/renk/string taramasını detekt/ktlint özel kuralıyla CI'ya bağla; CLAUDE.md'deki "Faz E TAMAMLANDI" ibaresini gerçek duruma göre güncelle.

## Final Karar
**Refactor.** Ürün fikri, ton ve çekirdek akış yayına hazır olgunlukta; ancak rutin hedef takibinin UI'da fiilen çalışmaması, ayarlardaki kutlama anahtarının plasebo olması ve uygulama iskeletinin tutarsızlığı kullanıcı tarafından fark edilecek gerçek kırılmalar. Bunlara ek olarak projenin kendi mimari sözleşmesiyle (CLAUDE.md/DESIGN.md) kod arasındaki uçurum, önce mimari/temizlik borcunun kapatılmasını, ardından yayın adayı bir sürümün çıkarılmasını gerektiriyor.

## Ekran Puanları
| Ekran/Akış | Puan | En Büyük Sorun | En Büyük Güç |
|---|---:|---|---|
| Onboarding | 7 | 883 satırlık tek dosya, token sistemi bypass edilmiş | Kişiselleştirme gerçek — seçimler sonraki adımları ve kaydedilen veriyi gerçekten değiştiriyor |
| Bugün | 6 | 767 (ekran) + 740 (VM) + 914 (sheets) satırlık üç dev dosyaya dağılmış sorumluluk | Bilgi hiyerarşisi (uyarı → ilerleme → görev → rutin → gün kapat) DESIGN.md'ye uygun sırada |
| Plan | 7 | Görev ekleme sheet'inde kategori/saat/hatırlatıcı alanları planlanmış ama UI'da yok | Hafta seçici net, seçili gün güçlü vurgulanıyor; boş durum iyi |
| Rutinler | 4 | Hedef tipi rutinler için artır/azalt kontrolü UI'da hiç yok — çekirdek özellik çalışmıyor | Boş durum ve rutin tanımı açıklayıcı metinleri iyi |
| İstatistik | 7 | `LevelHeroCard` tek metric kuralını biraz zorluyor (level+XP+seri+altın aynı kartta) | Tek hero kartı kuralı genel olarak korunuyor, dört eşit ağırlıklı metrik kart düzenli |
| Ayarlar | 5 | "Kutlama efektleri" anahtarı hiçbir şeyi etkilemiyor (plasebo) | "Tüm verileri temizle" doğru onay diyaloğuyla korunuyor |
| Sakinleşme | 8 | Hiçbir tıbbi/terapi iddiası yok, ölçülü ton | 1 Dakikalık Reset ve Kafam Dolu akışları tıbbi iddia taşımıyor, sıcak ve kısa |
| Oyunlaştırma | 5 | Level-up/başarım modalları tam ekran, kapatılamıyor (ayar etkisiz) ve iki paralel/duplike kutlama sistemi (`CelebrationModals.kt` canlı, `CelebrationSystem.kt` ölü) var | Başarımlar baskı unsuru olmadan sunuluyor, `MiniCelebrationBanner` ölçülü ve otomatik kapanıyor |
