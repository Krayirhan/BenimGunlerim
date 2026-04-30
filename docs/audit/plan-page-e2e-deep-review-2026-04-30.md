# Plan Sayfası Uçtan Uca Derin İnceleme (2026-04-30)

## 1) Yönetici Özeti

Plan sayfası; günlük/haftalık görev planlama senaryosunu sade bir akışla çözen, mimari olarak temiz ve test edilebilir bir yapı sunuyor. Özellikle `PlanViewModel` içinde use-case odaklı çağrı akışı ve Compose tarafında açık UI bölümleri güçlü.

**Genel Puan: 8.4 / 10**

---

## 2) İnceleme Kapsamı (E2E)

Bu değerlendirme aşağıdaki alanları kapsar:

- UI/UX akışları (gün seçimi, görev ekleme, toggle, silme, overdue yönetimi)
- ViewModel durum yönetimi ve business akışı
- Domain/use-case entegrasyonu
- Hata toleransı ve dayanıklılık
- Test kapsamı ve test kalitesi
- Erişilebilirlik, lokalizasyon, performans ve sürdürülebilirlik

---

## 3) Puanlama Modeli

| Kategori | Ağırlık | Puan (10) | Ağırlıklı Katkı |
|---|---:|---:|---:|
| Mimari/Katman Ayrımı | 20% | 8.8 | 1.76 |
| Ürün Akışı / UX | 20% | 8.3 | 1.66 |
| Durum Yönetimi / Doğruluk | 15% | 8.4 | 1.26 |
| Test Kapsamı / Test Kalitesi | 20% | 8.5 | 1.70 |
| Hata Yönetimi / Dayanıklılık | 10% | 7.8 | 0.78 |
| Erişilebilirlik / Lokalizasyon | 10% | 8.0 | 0.80 |
| Performans / Ölçeklenebilirlik | 5% | 8.8 | 0.44 |
| **Toplam** | **100%** |  | **8.40 / 10** |

---

## 4) Uçtan Uca Akış Analizi

## 4.1 Giriş ve Tarih Navigasyonu

- `PlanScreen`, `selectedDate` ve `weekStart` ile haftalık seçim davranışını açık şekilde sunuyor.
- Hero kartta bugün/yarın/dün etiketleri bağlamsal algıyı güçlendiriyor.
- Haftalık picker (7 gün) akışı görev planlama için hızlı ve düşük sürtünmeli.

**Değerlendirme:** Kullanıcıyı yormayan, amaca uygun ve hızlı gezinme.

## 4.2 Görev Ekleme Akışı

- FAB → modal bottom sheet → başlık gir → kaydet akışı net.
- Kaydet sonrası snackbar geri bildirimi mevcut.
- Boş başlık guard’ı ViewModel’de bulunuyor.

**Değerlendirme:** Temel ihtiyaçlar için yeterli; ileri validasyon (örn. max length, duplicate önerisi) eklenebilir.

## 4.3 Görev Tamamlama/Silme/Overdue Yönetimi

- Toggle ve silme akışları hem gün içi hem overdue listelerde tutarlı.
- Overdue görevleri seçili güne taşıma fonksiyonu önemli bir ürün değeri.
- Haptic geri bildirim etkileşimi olumlu.

**Değerlendirme:** Plan sayfasının pratik kullanılabilirliğini artıran güçlü alan.

---

## 5) Kod ve Mimari Değerlendirmesi

## 5.1 PlanViewModel

- `StateFlow` + `combine` + `flatMapLatest` kurgusu sade ve doğru.
- UI aksiyonları use-case seviyesine delege edilmiş; iş mantığı dağılmamış.
- `latestTasksById` cache’i ID tabanlı aksiyonları stabil hale getiriyor.

**Artı:** Okunabilir, testlenebilir, bağımlılıkları iyi ayrılmış.

## 5.2 PlanScreen

- `PlanHeroCard`, `WeekDatePicker`, `DayTasksCard`, `PlanOverdueCard` gibi parçalara ayrılmış yapı iyi.
- Tasarım dili (renkler, badge, kart yapısı) tutarlı.

**Gelişim noktası:** UI event tarafında error-state/snackbar ayrımı daha merkezi bir effect modeliyle (Today sayfasındaki gibi) güçlendirilebilir.

---

## 6) Test Kapsamı ve Güven Seviyesi

`PlanViewModelTest` içinde ana akışlar var:

- Tarih seçimi (`selectedDate`, `weekStart`)
- Boş başlık ekleme guard’ı
- Geçerli görev ekleme
- Toggle/sil/taşı use-case çağrıları

**Güven notu:** Ana fonksiyonel akış için yeterli; ancak aşağıdaki testlerle kalite daha da artar:

1. Snapshot akışı hata verdiğinde UI davranışı
2. Çok hızlı tarih değişimlerinde (flatMapLatest) yarış durumları
3. Büyük overdue listesinde performans regresyonu
4. Snackbar/effect davranışının tekilleştirilmesi

---

## 7) Riskler ve Açıklar

1. **Hata sunumu standardı:** Plan ekranında Today ekranındaki gibi belirgin bir “snapshot error + retry” paterni yok.
2. **Edge-case test açığı:** Timezone/date sınırlarında seçili gün davranışı için spesifik test görünmüyor.
3. **İleri UX validasyonları:** Add sheet tarafında giriş doğrulaması minimum seviyede.

---

## 8) İyileştirme Yol Haritası (Önceliklendirilmiş)

### P0 (hemen)
- Plan ekranı için `uiEffect` tabanlı merkezi hata/mesaj yönetimi ekle.
- Snapshot hata senaryolarını kapsayan ViewModel testleri yaz.

### P1 (kısa vade)
- Add task formuna kullanıcı-dostu validasyonlar (maks karakter, trim uyarısı, anlamlı hata metni).
- Date/time edge-case senaryoları için ek test matrisi.

### P2 (orta vade)
- Plan ekranı için performans benchmark (özellikle 100+ görev/overdue listesi).
- Accessibility testlerini (TalkBack odak sırası ve içerik açıklaması) genişlet.

---

## 9) Sonuç

Plan sayfası, ürünün planlama hedefini **etkin ve teknik olarak olgun** bir seviyede karşılıyor. Mevcut hali güçlü; hata yönetimi standardizasyonu ve edge-case test genişliğiyle birlikte **9.0+ seviyesine çıkma potansiyeli yüksek**.
