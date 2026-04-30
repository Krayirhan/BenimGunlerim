# Benim Günlerim — Today UI Hibrit Uygulama Master Planı (9/10 Görsel Denge Odaklı)

## 0) Bu Doküman Neden Var?

Bu plan, mevcut iki yaklaşımı birleştirir:

- Kapsamlı sprint/refactor yaklaşımı (sürdürülebilirlik, test, modülerlik).
- Hızlı görsel kalite yaklaşımı (UI dengesini erken yükseltme).

Ana hedef:

1. **Önce kullanıcıya görünen kaliteyi hızlıca yükseltmek** (görsel dengeyi 9/10 seviyesine yaklaştırmak).
2. **Sonra kod tabanını modüler ve güvenli hale getirmek**.
3. **En sonda test/erişilebilirlik kapısını güçlendirerek product-ready kaliteye çıkmak**.

---

## 1) Nihai Hedefler

### 1.1 Ürün hedefi

- Today ekranı ilk bakışta sade, okunabilir, premium ve güven veren bir yapıda olmalı.
- Kullanıcı 3 saniye içinde şunları anlayabilmeli:
  - Bugün durumum ne?
  - İlk hangi işi yapmalıyım?
  - Gün sonu kapanışa ne kadar yakınım?

### 1.2 Teknik hedef

- Tema/renk sistemi merkezi olmalı (hardcoded renk minimuma inmeli).
- UI dosyaları bakım dostu boyuta inmeli.
- Ana akışlar (task, routine, overdue, close day) test edilebilir ve güvenilir olmalı.

### 1.3 Ölçülebilir hedefler

- Görsel UI dengesi: **7.9 → 8.8+**
- Genel Today puanı: **8.2 → 9.0’a yakın**
- `TodayScreen.kt`: anlamlı parçalanma sonrası belirgin küçülme
- Compose UI test kapsamı: kritik akışlar için güçlü güvenlik ağı

---

## 2) Hibrit Yol Haritası (Öncelik Sırası)

> Kritik prensip: Önce görünür kalite, sonra mimari derinlik.

### Faz A — Görsel Sistem Önceliği (Hemen Etki)

1. **Today Theme Tokens** (renk, yüzey, border, metin rolleri)
2. **Hiyerarşi sadeleştirme** (odak akışı, metin gürültüsü azaltma)
3. **Katman kontrast standardı** (background → section → item)
4. **Ritim sistemi** (spacing, radius, min-height kuralları)

### Faz B — Modülerlik ve Sürdürülebilirlik

5. `TodayScreen` parçalama (`Route`, `Content`, `Sections`, `Rows`)
6. `TodaySheets` parçalama (`AddTask`, `TaskDetail`, `CloseDay`)
7. Action/state model sadeleştirme

### Faz C — Kalite Kapanışı

8. Accessibility sistematik geçiş
9. UI test matrisi
10. Preview matrix + regresyon checklist

---

## 3) Faz A Detayı — Görsel Dengeyi 9’a Yaklaştıran Asıl Kısım

## A1) Today Tokens Sistemi (Mutlaka İlk İş)

### Amaç

- Bugün ekranındaki görsel kararları tek bir merkezden yönetmek.
- Renk ayarını ekran içi hex değiştirme seviyesinden çıkarıp tasarım sistemi seviyesine taşımak.

### Önerilen dosya yapısı

```text
ui/today/theme/
  TodayColorTokens.kt
  TodayLayoutTokens.kt
  TodayTokensProvider.kt
```

### A1.1 Color token modeli

Örnek kapsam:

- `backgroundTop`, `backgroundBottom`
- `headerSurface`, `headerBorder`, `headerOnSurface`
- `taskSectionSurface`, `taskSectionBorder`, `taskSectionAccent`
- `routineSectionSurface`, `routineSectionBorder`, `routineSectionAccent`
- `itemSurface`, `itemBorder`, `itemRail`
- `chipSurface`, `chipBorder`, `chipText`
- `overdueSurface`, `overdueBorder`, `overdueAccent`
- `completedSurface`, `completedBorder`, `completedAccent`
- `hintText`, `secondaryText`, `primaryText`

### A1.2 Light/Dark setleri

- Light ve Dark ayrı tasarlanmalı.
- Light’ın basit koyu versiyonu Dark için kullanılmamalı.
- Dark modda section-item ayrımı özellikle manuel doğrulanmalı.

### A1.3 Kullanım kuralı

- `TodayScreen` içinde doğrudan hex yok.
- Hex yalnızca token tanım dosyalarında.

### A1.4 Kabul kriteri

- Today ekranındaki hardcoded renklerin neredeyse tamamı token’a taşınmış olmalı.

---

## A2) Görsel Hiyerarşi Sadeleştirme

### Problem

Aynı anda çok fazla metin ve blok dikkat çekiyor.

### Hedef

Ekranda görsel dikkat sırası sabit:

1. Header
2. Sıradaki işler (primary)
3. Overdue (varsa)
4. Tamamlananlar (ikincil)
5. Close Day (gün sonuna yakınsa yükseltilmiş)

### Uygulama

- `guidance` metnini bir kademe ikincilleştir.
- `swipe hint` metnini daha az baskın yap.
- Tamamlananlar varsayılan özet görünümde başlasın.
- Overdue 3+ ise özet kart + aç/kapa yaklaşımı.

### Kabul kriteri

- İlk bakışta ana odak dağılımı net: kullanıcı “şimdi ne yapacağını” hızlı görür.

---

## A3) Katman Kontrast Standardı

### Katman modeli

- Layer 0: App background
- Layer 1: Section cards (task/routine)
- Layer 2: Item cards (task/routine rows)
- Layer 3: Chips / inline controls

### Kontrast kuralı (örnek)

- Section border kontrastı item border’dan biraz düşük.
- Item background daima section’dan daha nötr ve daha okunur.
- Chip ler item içinde kaybolmayacak opaklıkta.

### Kabul kriteri

- “Kartlar arka planla kayboluyor” geri bildirimi kapanmalı.

---

## A4) Layout Matematiği (Ritim Sistemi)

Mevcut `rememberTodayLayoutMetrics()` doğru yönde. Bir adım ileri:

- `compact` ve `regular` profil ayrımı.
- Sabit kalan `dp` değerlerini de token sistemine almak.
- Section, row, chip, control boyutlarında tek grid sistemi.

Öneri:

- Base unit: 4dp
- Ana spacingler: 8/12/16/24
- Radius seti: 8/12/16/20/28

### Kabul kriteri

- Farklı ekran genişliklerinde kart oranları profesyonel şekilde korunur.

---

## 4) Faz B Detayı — Refactor ve Modülerlik

## B1) TodayRoute Ayrımı

`hiltViewModel()` ve side-effect toplama route katmanında kalmalı.
`TodayScreen` mümkün olduğunca saf UI bileşeni olmalı.

## B2) TodayScreen Parçalama

Önerilen parçalar:

- `TodayHeaderCard`
- `TodaySummaryBlock`
- `TodayOverdueSection`
- `TodayTasksSection`
- `TodayRoutinesSection`
- `TodayCompletedSection`
- `TodayCloseDayCard`
- `TodayMissedDayBanner`
- `TodayCommon` (ItemRow, MetaTag, CheckCircle vb.)

## B3) TodaySheets Parçalama

- `AddTaskSheet` alt alanları
- `TaskDetailSheet` alt sectionlar
- `CloseDaySheet` step componentleri

## B4) Action/State sadeleştirme

- Callback sayısı azaltılmalı.
- Alt component API yüzeyi küçülmeli.
- UI logic mapper yapısı tercih edilmeli.

---

## 5) Faz C Detayı — Quality Gate

## C1) Accessibility

- Tıklanabilir tüm kritik alanlarda `role` ve `onClickLabel`.
- Swipe aksiyonlarının görünür alternatifi.
- Touch target tutarlılığı.

## C2) UI testleri

Minimum:

- Empty state
- FAB → AddTaskSheet
- Time validation
- Task toggle + undo
- Delete + undo
- Overdue bulk actions
- CloseDay step flow
- Snapshot retry

## C3) Preview matrisi

- Empty / Loading / Error
- Dense task/routine
- Overdue heavy
- Completed heavy
- Closed day / missed day
- Dark mode / large font

---

## 6) Sprint Takvimi (Hibrit)

### Sprint H1 (5-7 gün) — Görsel Sistem ve Denge

- Today tokens kurulumu
- Katman kontrast standardı
- Hiyerarşi sadeleştirme
- Layout metrics profil güncellemesi

Beklenen etki:

- Görsel denge puanı hızlı artar.

### Sprint H2 (5-7 gün) — Screen/Sheets Modülerliği

- TodayRoute + TodayContent
- Today component split
- Sheets split
- Action/state sadeleştirme

### Sprint H3 (4-6 gün) — Quality Gate

- A11y sistematik geçiş
- UI test genişletme
- Preview matrix
- Regresyon checklist

---

## 7) Riskler ve Önlemler

### Risk 1: Refactor sırasında davranış kırılması

Önlem:

- Her alt adım sonrası build + kritik test seti
- Eski-yeni ekran davranış checklist kıyası

### Risk 2: Görselde iyileşme yerine tutarsızlık

Önlem:

- Token dışı renk kullanımını yasaklamak
- Her PR’da kontrast checklist uygulamak

### Risk 3: Scope büyümesi

Önlem:

- Sprint içinde “must” ve “nice-to-have” ayrımı
- Her sprint sonunda net done listesi

---

## 8) Definition of Done (Hibrit)

- [ ] Today renk/tema kararları token merkezli
- [ ] Section/item/chip katman ayrımı net
- [ ] Görsel hiyerarşi sade ve odaklı
- [ ] Layout ritmi cihaz genişliğine tutarlı tepki veriyor
- [ ] TodayScreen ve TodaySheets modüler
- [ ] Kritik UI akışları test altında
- [ ] A11y minimum standartlar tamam
- [ ] Dark mode + large font doğrulandı
- [ ] “Kartlar kayboluyor / çok kalabalık” tipi kullanıcı geri bildirimi kapanmış

---

## 9) Hızlı Başlangıç Backlog’u (İlk 10 İş)

1. `TodayColorTokens` oluştur
2. `TodayLayoutTokens` oluştur (compact/regular)
3. `TodayScreen` hex renklerini token’a taşı
4. Section/item/chip kontrast değerlerini standardize et
5. Completed section varsayılanını özet moda al
6. Overdue 3+ için summary card davranışı ekle
7. `TodayRoute` oluştur
8. `TodayContent` çıkar
9. `CloseDaySheet` step componentlerine ayır
10. `TodayScreenTest` kritik senaryoları genişlet

---

## 10) Sonuç

Bu hibrit planın farkı:

- Sadece refactor değil, **önce görünür kaliteyi** iyileştirir.
- Sadece görsel tweak değil, **uzun vadeli mühendislik altyapısı** kurar.
- Sonunda Today ekranını hem kullanıcı hem ekip açısından premium bir seviyeye taşır.

