# Plan Sayfası Sprint Planı (2026-04-30)

Bu plan, Plan sayfasını mevcut 7.2/10 seviyesinden 9.0 seviyesine taşımak için hazırlanmıştır. Odak; gerçek planlama kabiliyeti, daha güvenli görev yönetimi, daha net tasarım dili ve test edilebilirliktir.

## Hedef Durum

Plan sayfası yalnızca bu haftanın görev listesi değil, kullanıcının geçmiş gecikmeleri toparladığı, bugünü netleştirdiği ve gelecek günleri hızlıca planladığı ana planlama merkezi olmalıdır.

Başarı hedefleri:

- Kullanıcı 2 dokunuşla bugün, yarın, gelecek hafta veya özel tarihe görev planlayabilir.
- Görev eklerken başlık dışında saat, hatırlatıcı, öncelik, kategori ve not girebilir.
- Geciken görevleri tek tek veya toplu şekilde bugüne/yarına/seçili güne taşıyabilir.
- Silme işlemleri geri alınabilir olur.
- Plan ekranı hata, boş durum, yoğun liste ve erişilebilirlik senaryolarında güven verir.
- Plan ekranı görsel olarak Today ekranından ayrışır: daha düzenleyici, sakin ve taranabilir olur.

## Kapsam Dışı

- Yeni backend veya cloud sync.
- Rutin oluşturma akışının baştan tasarlanması.
- Tüm uygulama temasını yeniden yapmak.
- Yeni oyunlaştırma ekonomisi.

## Sprint 0 - Hızlı Düzeltme ve Stabilizasyon

Süre: 0.5-1 gün

Amaç: Kullanıcıya görünen küçük ama net kalite sorunlarını kapatmak.

İşler:

- `PlanScreen` içindeki çift `LaunchedEffect(Unit)` / `uiEffects.collect` tekrarını kaldır.
- Plan sayfası string ve gün kısaltmalarında mojibake kontrolü yap.
- Silme, ekleme ve hata snackbar mesajlarının tek kez gösterildiğini test et.
- Detekt baseline'daki PlanScreen uyarılarını gözden geçir, yeni uyarı eklenmediğini doğrula.

Kabul kriterleri:

- Görev ekleme/silme sonrası snackbar tek kez görünür.
- `./gradlew testDebugUnitTest` başarılıdır.
- `scripts/check-mojibake.ps1` Plan ile ilgili kaynaklarda temiz sonuç verir.

## Sprint 1 - Tarih Navigasyonu ve Planlama Omurgası

Süre: 2-3 gün

Durum: Tamamlandı (2026-04-30)

Amaç: Plan sayfasını gerçek gelecek planlama ekranına çevirmek.

İşler:

- Haftalık picker'a önceki hafta / sonraki hafta kontrolleri ekle.
- "Bugün" hızlı dönüş aksiyonu ekle.
- Özel tarih seçimi için Material DatePicker veya Compose date picker akışı ekle.
- Seçili haftada her gün için görev sayısı gösterecek hafif badge ekle.
- `PlanUiState` içine haftalık özet için gerekli alanları ekle veya yeni use-case tasarla.
- Hızlı tarih geçişlerinde `flatMapLatest` davranışı için unit test ekle.

Kabul kriterleri:

- Kullanıcı ileri/geri haftaya geçebilir.
- Kullanıcı takvimden özel tarih seçebilir.
- Bugün dışına gidildiğinde "Bugüne dön" aksiyonu görünür.
- Haftalık gün hücreleri boş/dolu gün ayrımını gösterir.
- Tarih değişimi görev listesini doğru yeniler.

## Sprint 2 - Gelişmiş Görev Ekleme

Süre: 2-4 gün

Durum: Tamamlandı (2026-04-30)

Amaç: Plan ekranında görev ekleme akışını Today ekranındaki veri modeliyle aynı seviyeye getirmek.

İşler:

- Add sheet alanlarını genişlet:
  - Başlık
  - Not
  - Planlanan tarih
  - Saat
  - Hatırlatıcı aç/kapat
  - Öncelik
  - Kategori
- Başlık validasyonunu netleştir: boş, 80 karakter limiti, trim davranışı.
- Saat validasyonunda mevcut `TimeInputValidator` kullanılmalı.
- `PlanViewModel.addTask` parametrelerini genişlet.
- `AddTaskUseCase` çağrısının note/startTime/category/priority/reminderTime ile beslendiğini test et.

Kabul kriterleri:

- Plan sayfasından eklenen görev Today detayında aynı bilgilerle görünür.
- Hatırlatıcı seçildiyse scheduler tetiklenir.
- Geçersiz saat veya boş başlık kullanıcıyı sheet içinde durdurur.
- Kaydet sonrası sheet kapanır ve görev seçili güne eklenir.

## Sprint 3 - Görev Detayı, Düzenleme ve Güvenli Silme

Süre: 3-4 gün

Durum: Tamamlandı (2026-04-30)

Amaç: Plan sayfasında eklenen görevlerin yönetilebilir olmasını sağlamak.

İşler:

- Görev satırına detay açma davranışı ekle.
- Today tarafındaki edit/detail sheet bileşenleri uygunsa ortaklaştır veya Plan için hafif varyant oluştur.
- Görev düzenleme alanları:
  - Başlık
  - Not
  - Tarih
  - Saat
  - Hatırlatıcı
  - Öncelik
  - Kategori
- Silme işlemine undo snackbar ekle.
- Direkt delete ikonunu daha az riskli hale getir: overflow menü veya onay/undo standardı.
- Silme ve geri alma için ViewModel testleri ekle.

Kabul kriterleri:

- Kullanıcı Plan ekranından görevi açıp düzenleyebilir.
- Silinen görev kısa süre içinde geri alınabilir.
- Tamamlanmış görev silmede de davranış tutarlıdır.
- Yanlışlıkla silme riski azalır.

## Sprint 4 - Geciken Görevleri Toparlama

Süre: 2-3 gün

Durum: Tamamlandı (2026-04-30)

Amaç: Overdue bölümünü pasif liste olmaktan çıkarıp günlük planlama aracına çevirmek.

İşler:

- Overdue kartına toplu aksiyonlar ekle:
  - Hepsini bugüne al
  - Hepsini yarına taşı
  - Seçili güne taşı
- Çok fazla overdue varsa listeyi kademeli göster:
  - İlk 3/5 görev
  - "Tümünü göster"
- Overdue görevlerde tarih metnini insan okunur yap:
  - Dün
  - 3 gün önce
  - 2026-04-20
- Taşıma işlemleri için başarı/hata snackbar'ları ekle.
- Toplu taşıma use-case yoksa domain katmanında ekle veya mevcut use-case güvenli şekilde döngüyle kullanılsın.

Kabul kriterleri:

- 10+ geciken görev ekranı boğmaz.
- Toplu taşıma sonrası overdue listesi doğru azalır.
- Hata durumunda kullanıcı neyin tamamlanamadığını anlar.

## Sprint 5 - Görsel Tasarım ve Bilgi Mimarisi

Süre: 2-3 gün

Durum: Tamamlandı (2026-04-30)

Amaç: Plan ekranının daha sakin, taranabilir ve planlama odaklı görünmesi.

İşler:

- Hero kartı kompakt hale getir.
- Üste şu hiyerarşiyi kur:
  - Başlık: Plan
  - Seçili tarih / hafta aralığı
  - Bugüne dön / tarih seç aksiyonları
- Gün özeti satırı ekle:
  - Toplam görev
  - Tamamlanan
  - Geciken
- Kart radius değerlerini daha sakin hale getir: 16-20dp bandı.
- Renk rolleri:
  - Mint: seçili gün ve ana aksiyon
  - Sky: normal görev bölümü
  - Coral: gecikenler
  - Amber: öncelik/hatırlatıcı
  - Purple: yardımcı içgörü
- Aynı bilgiyi tekrar eden tarih etiketlerini azalt.
- Görev satırında saat, öncelik ve kategori küçük meta/pill olarak göster.

Kabul kriterleri:

- İlk ekranda başlık, tarih navigasyonu ve görevlerin başlangıcı birlikte görünür.
- Boş gün ekranı net CTA içerir.
- Overdue alanı görsel olarak dikkat çeker ama ekranı domine etmez.
- Renkler dekorasyon değil durum anlamı taşır.

## Sprint 6 - Erişilebilirlik, Performans ve Edge Case Testleri

Durum: Tamamlandı (2026-04-30)

Süre: 2-3 gün

Amaç: Plan sayfasını yoğun veri, font scaling ve erişilebilirlik altında güvenli hale getirmek.

İşler:

- TalkBack contentDescription kontrolleri:
  - Tarih hücreleri
  - Tamamla checkbox'ı
  - Taşı, sil, düzenle aksiyonları
- Touch target kontrolü: minimum 48dp.
- Font scaling smoke testi: 1.3x ve 1.5x.
- Büyük veri senaryosu:
  - 100 görev
  - 50 overdue
  - Uzun başlıklar
- Compose UI smoke testleri ekle:
  - Plan ekranı açılır
  - Tarih değiştirir
  - Görev ekler
  - Overdue aksiyonu görünür
- Snapshot hata senaryosu için error state ve retry davranışı ekle.

Kabul kriterleri:

- Accessibility smoke test geçer.
- Uzun görev başlıkları layout'u bozmaz.
- Büyük listede ekran kullanılabilir kalır.
- Veri yükleme hatasında kullanıcı retry görebilir.

## Sprint 7 - Final Sertleştirme ve Kabul

Durum: Tamamlandı (2026-04-30)

Süre: 1 gün

Amaç: Sprint 0-6 çıktısını üretim öncesi küçük kırılmalara karşı sertleştirmek.

Tamamlananlar:

- Plan ekleme formundaki geçici alanlar konfigürasyon değişimlerinde korunacak şekilde saveable state'e taşındı.
- Plan düzenleme formundaki başlık, not, tarih, saat, kategori, öncelik ve hatırlatma alanları saveable state'e taşındı.
- Görev tamamlama erişilebilirlik metinleri hardcoded metinden string resource kullanımına çekildi.
- Final doğrulama kapıları tekrar çalıştırıldı.

Kabul:

- Plan ekranı derlenir.
- Unit testler geçer.
- Lint geçer.
- Plan ekranı ve test dosyalarında yeni mojibake izi yoktur.
- Sprint 0-7 durumları dokümanda tamamlandı olarak görünür.

## Sprint 8 - Kapanış Temizliği

Durum: Tamamlandı (2026-04-30)

Amaç: Sprint sonrası kalan kalite borçlarını kapatmak.

Tamamlananlar:

- Mojibake kontrol scripti ASCII-only regex pattern yapısına taşındı ve parse hatası giderildi.
- Detekt baseline yeni Plan/Today durumuna göre güncellendi; `detekt` kapısı tekrar geçer hale getirildi.
- Plan yoğun veri davranışı için 120 günlük görev + 30 overdue görev senaryosunu kapsayan unit test eklendi.
- Plan task liste, overdue ve row bileşenleri `PlanTaskListComponents.kt` dosyasına taşındı.
- `PlanScreen.kt` yaklaşık 59 KB'den 48 KB'ye düşürüldü.
- Android test kaynakları derleme kapısından geçirildi.

## Önerilen Uygulama Sırası

1. Sprint 0
2. Sprint 1
3. Sprint 2
4. Sprint 3
5. Sprint 4
6. Sprint 5
7. Sprint 6
8. Sprint 7
9. Sprint 8

Tasarım sprinti teknik sprintlerden sonra konumlandırıldı çünkü tarih navigasyonu, gelişmiş görev alanları ve overdue davranışı netleşmeden görsel yerleşim kalıcı olmaz.

## Öncelik Matrisi

| Öncelik | İş | Neden |
|---|---|---|
| P0 | Çift effect collect düzeltmesi | Doğrudan kullanıcıya çift mesaj olarak yansıyabilir |
| P0 | Hafta ileri/geri ve tarih seçimi | Plan sayfasının ana ürün vaadi |
| P0 | Gelişmiş görev ekleme | Planlama verisi eksik kalıyor |
| P1 | Görev düzenleme | Eklenen planın yönetilebilir olması gerekir |
| P1 | Undo delete | Veri kaybı riskini azaltır |
| P1 | Overdue toplu aksiyonlar | Günlük toparlama hızını artırır |
| P2 | Görsel sadeleştirme | Algıyı ve taranabilirliği güçlendirir |
| P2 | Büyük veri/perf testleri | Uzun vadeli kalite güvencesi |

## Test Kapısı

Her sprint sonunda minimum:

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- Plan ile ilgili ViewModel testleri
- Değişen akış için en az bir UI smoke veya instrumentation test
- `scripts/check-mojibake.ps1`

Sprint 5 ve 6 sonunda ek olarak:

- Font scaling manuel kontrolü
- TalkBack odak sırası manuel kontrolü
- 100+ görev manuel veya seed edilmiş test

## 9.0 Tanımı

Plan sayfası aşağıdaki noktaya geldiğinde 9.0 kabul edilir:

- Serbest tarih planlama vardır.
- Görev ekleme Today ile veri olarak eşdeğerdir.
- Görev düzenleme ve güvenli silme vardır.
- Overdue yönetimi toplu aksiyonları destekler.
- Boş, hata, yoğun liste ve erişilebilirlik durumları tasarlanmıştır.
- Renk, kart ve tipografi düzeni daha sakin ve taranabilirdir.
- Unit ve UI smoke testleri ana akışları korur.
