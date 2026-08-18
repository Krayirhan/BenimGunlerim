# BenimGünlerim — Güncel Proje Durumu

> Son güncelleme: 2026-07-30  
> Bu belge, mevcut kod ve son doğrulamalarla uyumlu güncel kaynak belgedir. Tarihli audit ve arşiv planları tarihsel kayıttır; bugünkü durum için önce bu belge okunmalıdır.

## 1. Mevcut Konum

BenimGünlerim yeni başlanmış bir proje değildir. Temel ürün akışları ve teknik mimarisi kurulmuş, internal beta adayı seviyesinde olan ve UI/QA sertleştirmesi devam eden offline-first Android uygulamasıdır.

Mevcut ürün döngüsü:

```text
Planla → Bugün gör → Görevi/rutini tamamla → İlerlemeni fark et → Günü değerlendir
```

## 2. Tamamlanan Ürün Kapsamı

- Onboarding ve örnek veri akışı
- Bugün, Plan, Rutinler, İlerleme, Başarımlar ve Ayarlar ekranları
- Görev ekleme, düzenleme, taşıma, tamamlama ve silme
- Alt görevler
- Rutin oluşturma, düzenleme, tamamlama, ilerleme ve arşivleme
- Completion log ve günlük durum kayıtları
- Günlük skor, seri, XP, altın ve başarımlar
- Gün kapatma, missed day değerlendirme ve bekleyen görev taşıma
- Bildirim ve reminder restore altyapısı
- Room database, schema export ve migration altyapısı
- JSON import/export ve transaction temelli restore
- Hilt, ViewModel, UseCase, Repository ve Compose mimarisi
- Unit test, lint, debug build ve release kalite kapıları
- Graphify entegrasyonu ve agent çalışma kuralları

## 3. Son Doğrulamalar

| Kontrol | Sonuç |
|---|---|
| `:app:compileDebugKotlin` | Başarılı |
| `:app:testDebugUnitTest --tests com.benimgunlerim.ui.today.TodayViewModelTest` | Başarılı |
| Sprint 0 Today davranışları | Kaynak akışı ve unit testlerle doğrulandı |
| Graphify incremental update | Tamamlandı |
| Mojibake taraması | Başarılı |
| Lokal kalite kapısı (`check-local.ps1`) | Başarılı |
| Release kalite kapısı (`check-release.ps1`) | Başarılı; signed AAB üretildi |
| App instrumentation | 74 testten 73 başarılı; 1 görev silme Compose idling timeout |
| Notification smoke matrix | Başarılı; 4 test |
| Startup performance gate | Başarısız; cold median 6228 ms |

Android Compose instrumentation artık emülatörde çalışıyor. 74 app testinden 73’ü başarılıdır; kalan görev silme smoke testi Compose idling timeout nedeniyle deterministik hale getirilmelidir. Bu, temel davranışların unit testlerle doğrulanmadığı anlamına gelmez; tek bir cihaz akışında kanıt eksikliği anlamına gelir.

## 4. Sprint Durumu

### Sprint 0 — Temel davranış ve davranış kilidi

**Durum: Tamamlandı, UI instrumentation kanıtı açık.**

- Görev ve rutin aksiyonları çalışıyor.
- Rutin progress artırma/azaltma ve tamamlanma state’i çalışıyor.
- Gün kapatma zamanı ve kapalı gün mutasyon kilidi çalışıyor.
- Dünü değerlendir ve atla akışları bağlı.
- Today ViewModel testleri başarılı.
- Kalan iş: Compose test launch/senkronizasyon problemi.

### Sprint 1 — Lokalizasyon ve metin kalitesi

**Durum: Tamamlandı (2026-07-30).**

- Ana navigasyon, Today, Routines ve Onboarding metinleri `strings.xml` içine taşındı.
- Onboarding önerileri resource ID tabanlı hale getirildi.
- Türkçe karakter ve encoding kontrolü başarılı.

### Sprint 2 — Semantic color system

**Durum: Büyük ölçüde uygulanmış, son tutarlılık kontrolü açık.**

- Ortak renk/token yapısı mevcut.
- Ekranlar arası semantic token kullanımı ve hardcoded renkler son kez taranmalı.

### Sprint 3 — Card ve button system

**Durum: Büyük ölçüde uygulanmış, regresyon kontrolü açık.**

- `Common.kt` içinde ortak kart, hero, alert ve arka plan bileşenleri kullanılıyor.
- Today, Plan, Routines, Progress ve Settings ekranlarında görsel regresyon kontrolü yapılmalı.

### Sprint 4 — Rutin etkileşimi

**Durum: Temel davranış tamamlandı, gerçek cihaz testi açık.**

- Artır/azalt ve hedef progress akışları mevcut.
- Tamamlanma ve ödül geçişleri mevcut.
- Instrumentation ve edge-case testleri genişletilmeli.

### Sprint 5 — Header, gün kapatma ve missed day polish

**Durum: Temel akış tamamlandı, UX son kontrolü açık.**

- Header, gün kapatma ve missed day bileşenleri mevcut.
- Metin, erişilebilirlik ve farklı ekran boyutları doğrulanmalı.

### Sprint 6 — Erişilebilirlik ve hitbox

**Durum: Açık.**

- TalkBack content description ve role semantics
- Minimum touch target
- Font scaling
- Kontrast
- Tablet/landscape

### Sprint 7 — QA, preview ve regression

**Durum: Açık.**

- Preview matrisi genişletilmeli.
- Kritik akışlar gerçek cihazda test edilmeli.
- FAB/bottom navigation ve küçük ekran taşmaları kontrol edilmeli.

## 5. Production Öncesi Açıklar

Öncelikli açıklar:

1. Compose instrumentation test başlatma/senkronizasyon problemi
2. Tüm Room schema sürümleri için migration matrisi
3. Bildirim timezone, boot, permission denied ve OEM testleri
4. Privacy policy ve analytics consent kararının ürün akışına bağlanması
5. Crash/ANR monitoring ve Play Console vitals süreci
6. ViewModel ve accessibility test kapsamının genişletilmesi
7. Startup/büyük veri performans ölçümlerinin düzenli gate’e bağlanması
8. Release secret’ları, branch/tag protection ve internal testing doğrulaması

## 6. Önerilen Uygulama Sırası

```text
1. Compose instrumentation sorununu çöz
2. Sprint 1 localization/metin temizliğini bitir
3. Today/Plan/Routines UI regresyonlarını doğrula
4. Accessibility ve cihaz uyumluluğunu tamamla
5. Migration + notification + privacy açıklarını kapat
6. Crash/ANR + performans + release kontrollerini çalıştır
7. Play Console internal beta yayınla
```

## 7. Doküman Kullanım Kuralı

- Güncel ürün ve teknik durum: bu belge
- Ürün felsefesi ve kullanıcı akışları: `docs/product/benimgunlerim-urun-felsefesi-ve-akislari.md`
- Sprint görev detayları: `today_ui_agent_sprint_sistemi.md`
- Sprint gerçekleşme kaydı: `today_ui_agent_sprint_progress.md`
- Tarihli audit’ler: `docs/audit/` — tarihsel snapshot
- Eski planlar: `docs/archive/` — aktif backlog değildir
