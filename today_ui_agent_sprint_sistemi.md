# Benim Günlerim — Today UI/UX 9/10 Hedefli Agent Sprint Sistemi

## 0. Kullanım Amacı

Bu doküman, **Today / Bugün ana sayfasını** UI/UX, erişilebilirlik, metin kalitesi, etkileşim tasarımı ve tasarım sistemi açısından **9/10 seviyesine çıkarmak** için hazırlanmış agent-ready sprint sistemidir.

Bu plan doğrudan bir yazılım agent’ına verilecek şekilde yazılmıştır. Her sprintte:

- Amaç
- Kapsam
- Dosya / component etkisi
- Yapılacak işler
- Kabul kriterleri
- Test kriterleri
- Agent notları
- Definition of Done

bulunur.

---

# 1. Genel Hedef

Today ekranının mevcut güçlü taraflarını koruyarak aşağıdaki alanları 9/10 seviyesine çıkarmak:

| Alan | Hedef |
|---|---|
| Görsel Tasarım | Tutarlı, semantik renk sistemi, premium görünüm |
| UX | Kolay tıklanan, düşük bilişsel yüklü, net aksiyonlar |
| Accessibility | 48dp hitbox, yüksek kontrast, doğru contentDescription |
| Profesyonellik | Kusursuz Türkçe lokalizasyon, doğal mikro metinler |
| Etkileşim Tasarımı | Aktif/pasif state’lerin net ayrılması |
| Kod Kalitesi | Component bazlı, sürdürülebilir, preview/test dostu yapı |

---

# 2. Agent İçin Temel Kurallar

## 2.1 Dokunulmaması Gerekenler

Agent aşağıdaki davranışları bozmamalıdır:

```text
- Görev ekleme davranışı
- Rutin tamamlama davranışı
- Hedefli rutin progress artır/azalt mantığı
- Günü kapatma zamanı ve koşulları
- Missed day / dünü tamamlama akışı
- Snackbar/undo davranışları
- Bottom navigation yapısı
- FAB temel işlevi
- ViewModel business logic
```

## 2.2 Öncelikli Dokunulacak Alanlar

```text
- UI component yapısı
- strings.xml / Türkçe metinler
- renk token sistemi
- kart stilleri
- buton stilleri
- hitbox boyutları
- accessibility semantics
- progress bar görünümü
- passive/active visual state
```

## 2.3 Genel Kodlama Kuralları

```text
- Compose componentleri küçük ve tek sorumluluklu olmalı.
- Tıklanabilir her alan minimum 48.dp olmalı.
- Metin aksiyonları doğrudan Text olarak bırakılmamalı; 48.dp container içinde olmalı.
- Renkler hardcoded kalmamalı; token/semantic yapıdan gelmeli.
- Türkçe metinler stringResource üzerinden yönetilmeli.
- UI değişiklikleri business logic’i değiştirmemeli.
- Her sprint sonunda build alınmalı.
- Büyük refactor yapılacaksa davranış değişikliği ayrı commit olmalı.
```

---

# 3. Hedef Scorecard

| Kategori | Mevcut Risk | Hedef Puan |
|---|---|---:|
| Görsel Tasarım | Renkler güzel ama semantik değil | 9.0 |
| Kullanılabilirlik | Metin aksiyonlar ve zayıf hitbox | 9.0 |
| Erişilebilirlik | Kontrast/hitbox eksikleri | 9.0 |
| Profesyonellik | Türkçe karakter sorunları | 9.5 |
| Etkileşim Tasarımı | State ayrımı zayıf | 9.0 |
| Genel | Potansiyel yüksek | 9.0 |

---

# 4. Sprint Sistemi Genel Bakış

| Sprint | Başlık | Ana Hedef | Risk | Tahmini Süre |
|---|---|---|---|---:|
| Sprint 0 | Güvenlik Ağı | Mevcut davranışı koru | Düşük | 0.5-1 gün |
| Sprint 1 | Lokalizasyon & Metin Kalitesi | Profesyonellik puanını yükselt | Düşük | 0.5-1 gün |
| Sprint 2 | Semantic Color System | Renkleri anlamlandır | Orta | 1-2 gün |
| Sprint 3 | Card & Button System | Kart/buton standardı kur | Orta | 1-2 gün |
| Sprint 4 | Routine Interaction Redesign | Artır/azalt ve progress UX’i düzelt | Orta-yüksek | 1-2 gün |
| Sprint 5 | Header / Day Close / Missed Day Polish | Ana ekran hiyerarşisini 9’a çek | Orta | 1-2 gün |
| Sprint 6 | Accessibility & Hitbox Pass | A11y ve mobil tıklanabilirliği tamamla | Orta | 1 gün |
| Sprint 7 | QA, Preview, Regression | Son kalite kontrol | Düşük | 1 gün |

---

# 5. Sprint 0 — Güvenlik Ağı ve Mevcut Davranış Kilidi

## Amaç

UI değişikliklerine başlamadan önce mevcut davranışları belgelemek ve refactor sırasında kırılmaları yakalayacak minimum güvenlik ağı oluşturmak.

## Kapsam

- Mevcut Today ekranı davranışları kontrol edilecek.
- Gerekirse preview/test data hazırlanacak.
- Ana kullanıcı akışları not alınacak.

## Yapılacak İşler

### 0.1 Davranış Checklist’i

Agent aşağıdaki akışların hâlâ çalıştığını sprint sonunda kontrol etmelidir:

```text
[ ] Today ekranı açılıyor.
[ ] Header doğru gün bilgisini gösteriyor.
[ ] Rutinler listeleniyor.
[ ] Hedefli rutin progress değeri görünüyor.
[ ] Artır aksiyonu progress artırıyor.
[ ] Azalt aksiyonu progress azaltıyor, 0 altına düşmüyor.
[ ] Rutin tamamlandığında UI state güncelleniyor.
[ ] Gün kapatma kartı saatten önce pasif state gösteriyor.
[ ] 21:00 sonrası gün kapatma aktif olabiliyor.
[ ] Dünü tamamla / missed day kartı görünüyor.
[ ] Dünü değerlendir aksiyonu ilgili sheet’i açıyor.
[ ] Atla aksiyonu çalışıyor.
[ ] FAB görev ekleme akışını açıyor.
[ ] Bottom navigation state’i korunuyor.
```

### 0.2 Basit Preview Data

Aşağıdaki state’ler için preview data hazırlanmalı:

```text
- Empty day
- 2 rutinli gün
- Hedefli rutinli gün
- Gün sonu pasif state
- Dünü tamamla kartlı gün
- Completed state
```

## Kabul Kriterleri

```text
[ ] Davranış checklist’i tamamlandı.
[ ] En az 3 preview state hazır.
[x] Sprint 1’e başlamadan önce build başarılı.
```

## Agent Notu

Bu sprintte tasarım değişikliği yapma. Sadece güvenlik ağı ve gözlem.

---

# 6. Sprint 1 — Lokalizasyon ve Metin Kalitesi

## Amaç

Türkçe karakter, doğal mikro metin ve profesyonellik problemlerini düzeltmek.

Bu sprint en hızlı kalite artışı sağlayan sprinttir.

## Kapsam

- strings.xml veya ilgili string resource dosyaları
- Today ekranında görünen tüm metinler
- Rutin kartı metinleri
- Header metinleri
- Gün sonu kartı metinleri
- Dünü tamamla kartı metinleri

## Yapılacak İşler

### 1.1 Türkçe Karakter Temizliği

Aşağıdaki hatalar aranıp düzeltilmelidir:

| Yanlış | Doğru |
|---|---|
| Henuz | Henüz |
| adim | adım |
| gunluk | günlük |
| Gunu | Günü |
| Su ic | Su iç |
| ic | iç |
| degerlendir | değerlendir |
| tamamla | tamamla, bağlama göre doğru |

### 1.2 Apostrof ve Tipografik Metin Temizliği

| Mevcut | Öneri |
|---|---|
| `21:00'dan sonra aktif olacak.` | `21:00’dan sonra aktif olacak.` |
| `21:00'dan sonra` | `21:00’dan sonra` |

Eğer teknik olarak curly apostrophe string yönetiminde sorun çıkarırsa düz `'` kalabilir; ancak Türkçe karakterler kesin düzeltilmelidir.

### 1.3 Mikro Metin İyileştirmeleri

| Mevcut | Önerilen |
|---|---|
| `Henüz tamamlanan yok` | `Henüz tamamlanan yok` |
| `2 adım seni bekliyor` | `Bugün 2 adım seni bekliyor` |
| `0 gunluk seri` | `0 günlük seri` |
| `Hedefe ulaşınca tamamlanır` | `Hedefe ulaştığında tamamlanır` |
| `21:00’dan sonra aktif olacak.` | `21:00’dan sonra değerlendirme yapabilirsin.` |
| `9 Mayıs, Cumartesi için gün özeti yok.` | `9 Mayıs için kısa özet henüz yok.` |
| `Atla` | `Atla` |
| `Dünü tamamla` | `Dünü tamamla` |

### 1.4 String Resource Standardı

Hardcoded text varsa stringResource’a taşınmalıdır.

Örnek:

```kotlin
Text(stringResource(R.string.today_no_completed_yet))
```

## Kabul Kriterleri

```text
[ ] Today ekranında Türkçe karakter hatası kalmadı.
[ ] `gunluk`, `Henuz`, `adim`, `Gunu` gibi formsuz metinler yok.
[ ] Tüm görünen metinler stringResource üzerinden geliyor.
[ ] Mikro metinler daha doğal hale getirildi.
[ ] Build başarılı.
```

## Test Kriterleri

```text
[ ] Ekran açıldığında `Henüz`, `adım`, `günlük`, `Günü` doğru görünüyor.
[ ] Rutin kartında `0 günlük seri` doğru görünüyor.
[ ] Gün sonu kartındaki zaman metni doğal.
[ ] Dünü tamamla kartındaki açıklama kısa ve düzgün.
```

## Agent Notu

Bu sprintte layout değiştirme. Sadece metin ve string kaynakları.

---

# 7. Sprint 2 — Semantic Color System

## Amaç

Renkleri rastgele pastel bloklar olmaktan çıkarıp anlam taşıyan semantik bir sisteme dönüştürmek.

## Kapsam

- Today özel renk tokenları
- Kart renkleri
- Progress bar renkleri
- Chip renkleri
- Disabled state renkleri
- Attention / missed day renkleri

## Tasarım İlkesi

```text
Beyaz / surface = normal içerik
Yeşil = tamamlanma, başarı, progress
Amber / sarı = zamanlı, pasif ama bilgilendirici
Coral / turuncu = dikkat, dünden kalan aksiyon
Kırmızı = hata, silme, tehlike
Mor = duygu / moral ama moral skoruna göre değişebilir
Gri = disabled / pasif
```

## Yapılacak İşler

### 2.1 TodayColors Oluştur

Yeni dosya önerisi:

```text
ui/today/theme/TodayColors.kt
```

Örnek yapı:

```kotlin
@Immutable
data class TodayColors(
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val border: Color,

    val success: Color,
    val successContainer: Color,
    val successBorder: Color,

    val warning: Color,
    val warningContainer: Color,
    val warningBorder: Color,

    val attention: Color,
    val attentionContainer: Color,
    val attentionBorder: Color,

    val danger: Color,
    val dangerContainer: Color,

    val routineAccent: Color,
    val routineContainer: Color,
    val routineBorder: Color,

    val disabledBackground: Color,
    val disabledText: Color,
    val disabledBorder: Color,
)
```

### 2.2 Önerilen Light Tokenlar

```kotlin
fun lightTodayColors() = TodayColors(
    surface = Color.White,
    surfaceMuted = Color(0xFFF8FAFC),
    textPrimary = Color(0xFF1F2937),
    textSecondary = Color(0xFF4B5563),
    textTertiary = Color(0xFF6B7280),
    border = Color(0xFFE5E7EB),

    success = Color(0xFF16A34A),
    successContainer = Color(0xFFEAF8F0),
    successBorder = Color(0xFFC7EBD4),

    warning = Color(0xFF9A6B13),
    warningContainer = Color(0xFFFFF8E7),
    warningBorder = Color(0xFFE8D39B),

    attention = Color(0xFFE76F51),
    attentionContainer = Color(0xFFFFF1EC),
    attentionBorder = Color(0xFFFFC9BB),

    danger = Color(0xFFDC2626),
    dangerContainer = Color(0xFFFEE2E2),

    routineAccent = Color(0xFF22A06B),
    routineContainer = Color(0xFFF2FBF5),
    routineBorder = Color(0xFFD2EBDD),

    disabledBackground = Color(0xFFF3F4F6),
    disabledText = Color(0xFF9CA3AF),
    disabledBorder = Color(0xFFE5E7EB),
)
```

### 2.3 MaterialTheme Extension

```kotlin
val LocalTodayColors = staticCompositionLocalOf { lightTodayColors() }

val MaterialTheme.todayColors: TodayColors
    @Composable get() = LocalTodayColors.current
```

### 2.4 Hardcoded Renkleri Azalt

Aşağıdaki türde renkleri token’a taşı:

```text
- Header card background
- Routine card background/border
- Day close card background/border
- Missed day card background/border
- Progress bar background/fill
- Chip background/border/text
- Disabled text/background
```

## Kabul Kriterleri

```text
[ ] TodayColors dosyası oluşturuldu.
[ ] Today ekranındaki ana renkler semantic tokenlardan geliyor.
[ ] Başarı, dikkat, pasif ve bilgi state’leri renk olarak ayrıldı.
[ ] Renkler sadece dekoratif değil, anlam taşıyor.
[ ] Build başarılı.
```

## Test Kriterleri

```text
[ ] Rutin kartı başarı/alışkanlık hissini yeşil accent ile veriyor.
[ ] Günü kapat pasif state aktif CTA gibi görünmüyor.
[ ] Dünü tamamla kartı dikkat çekiyor ama FAB ile yarışmıyor.
[ ] Kırmızı sadece hata/silme gibi durumlarda kullanılıyor.
```

## Agent Notu

Renkleri değiştirirken layout davranışını değiştirme. Önce token sistemini kur, sonra componentleri bu tokenlara bağla.

---

# 8. Sprint 3 — Card & Button System

## Amaç

Today ekranındaki kart ve buton stillerini standartlaştırmak. Kartlar rastgele renklendirilmiş bloklar gibi değil, tutarlı bir sistemin parçaları gibi görünmeli.

## Kapsam

- TodayCard componenti
- TodayCardVariant enum’u
- Button variantları
- Text-only aksiyonların container içine alınması
- Disabled state görsel dili

## Yapılacak İşler

### 3.1 TodayCardVariant Oluştur

```kotlin
enum class TodayCardVariant {
    Default,
    Routine,
    Warning,
    Attention,
    Disabled,
    Success
}
```

### 3.2 TodayCard Componenti

```kotlin
@Composable
fun TodayCard(
    variant: TodayCardVariant,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = MaterialTheme.todayColors
    val style = when (variant) {
        TodayCardVariant.Default -> CardStyle(
            background = colors.surface,
            border = colors.border,
            contentAlpha = 1f,
        )
        TodayCardVariant.Routine -> CardStyle(
            background = colors.routineContainer,
            border = colors.routineBorder,
            contentAlpha = 1f,
        )
        TodayCardVariant.Warning -> CardStyle(
            background = colors.warningContainer,
            border = colors.warningBorder,
            contentAlpha = 1f,
        )
        TodayCardVariant.Attention -> CardStyle(
            background = colors.attentionContainer,
            border = colors.attentionBorder,
            contentAlpha = 1f,
        )
        TodayCardVariant.Disabled -> CardStyle(
            background = colors.disabledBackground,
            border = colors.disabledBorder,
            contentAlpha = 0.78f,
        )
        TodayCardVariant.Success -> CardStyle(
            background = colors.successContainer,
            border = colors.successBorder,
            contentAlpha = 1f,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(style.background)
            .border(1.dp, style.border, RoundedCornerShape(24.dp))
            .padding(20.dp)
            .alpha(style.contentAlpha),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}
```

### 3.3 Button Hiyerarşisi

Button variantları:

```text
Primary     = ana aksiyon, ekranda tek baskın öğe
Secondary   = önemli ama ana olmayan aksiyon
Ghost       = düşük öncelikli aksiyon
IconAction  = rutin artır/azalt, seçenekler
Disabled    = yapılamayan aksiyon
```

### 3.4 Text-only Aksiyonları Kaldır

Aşağıdaki aksiyonlar sadece Text olarak kalmamalı:

```text
- Artır
- Azalt
- Atla
- Değerlendir
```

Her biri minimum 48.dp yüksekliğe sahip container içinde olmalı.

### 3.5 TodayGhostButton

```kotlin
@Composable
fun TodayGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(text)
    }
}
```

### 3.6 TodaySecondaryButton

```kotlin
@Composable
fun TodaySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(14.dp),
    ) {
        Text(text)
    }
}
```

## Kabul Kriterleri

```text
[ ] TodayCardVariant sistemi var.
[ ] Today ekranındaki ana kartlar TodayCard veya benzeri standart wrapper kullanıyor.
[ ] Text-only aksiyonlar 48.dp hitbox içine alındı.
[ ] Dünü tamamla butonları FAB ile görsel olarak yarışmıyor.
[ ] Günü kapat pasif kartı aktif CTA gibi görünmüyor.
[ ] Build başarılı.
```

## Test Kriterleri

```text
[ ] Değerlendir butonu rahat tıklanıyor.
[ ] Atla aksiyonu rahat tıklanıyor.
[ ] Pasif kart aktif karttan görsel olarak ayrılıyor.
[ ] Kartlar arası köşe, padding ve border tutarlı.
```

## Agent Notu

Bu sprintte görsel sistem kur. Business logic’e dokunma. Mevcut callbacks aynı kalmalı.

---

# 9. Sprint 4 — Routine Interaction Redesign

## Amaç

Rutin kartında sık kullanılan `Artır / Azalt` etkileşimini mobil standartlara uygun hale getirmek ve progress görünürlüğünü artırmak.

## Mevcut Sorun

- `Artır` ve `Azalt` sadece metin aksiyon gibi duruyor.
- Hitbox zayıf.
- Progress bar çok ince ve düşük kontrastlı.
- `Azalt`, değer 0 iken aktif gibi görünebiliyor.
- Hedefli rutinlerde high interaction cost var.

## Kapsam

- RoutineRow
- Hedefli rutin progress kontrolü
- Increase/decrease button componentleri
- Progress bar componenti
- ContentDescription / semantics

## Yapılacak İşler

### 4.1 RoutineIconActionButton Oluştur

```kotlin
@Composable
fun RoutineIconActionButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (enabled) MaterialTheme.todayColors.successContainer
                else MaterialTheme.todayColors.disabledBackground
            ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) MaterialTheme.todayColors.success
            else MaterialTheme.todayColors.disabledText,
        )
    }
}
```

### 4.2 Artır / Azalt Metinlerini İkona Çevir

Eski:

```text
Azalt       progress       Artır
```

Yeni:

```text
[-]         progress       [+]
```

veya:

```text
[-]    0 / 2 litre    [+]
```

### 4.3 Enabled/Disabled Kuralları

```kotlin
val canDecrease = currentValue > 0f
val canIncrease = currentValue < target
```

Kurallar:

```text
[ ] currentValue == 0 ise azalt disabled.
[ ] currentValue >= target ise artır disabled veya tamamlandı state’e geçiyor.
[ ] Disabled button hem görsel hem functional disabled.
```

### 4.4 RoutineProgressBar Oluştur

```kotlin
@Composable
fun RoutineProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(MaterialTheme.todayColors.successContainer)
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.todayColors.success)
        )
    }
}
```

Progress height minimum 8.dp olmalı. 10.dp de tercih edilebilir.

### 4.5 RoutineRow Yeni Layout

```text
○  Su iç                                      ⋯
   0 / 2 litre · 0 günlük seri

   ███░░░░░░░░░░░░░░░░
   Hedefe ulaştığında tamamlanır

   [−]                                  [+]
```

### 4.6 Check-type Rutin İçin Alt Açıklama

Örnek:

```text
Kitap
Günlük rutin · 0 günlük seri
```

veya eğer preferredTime varsa:

```text
Kitap
Bugün 20:00 · 0 günlük seri
```

## Kabul Kriterleri

```text
[ ] Artır/Azalt metinleri ikon butona dönüştü.
[ ] Her iki buton minimum 48.dp.
[ ] Azalt, currentValue 0 iken disabled.
[ ] Artır, hedefe ulaşıldığında disabled veya tamamlandı state ile uyumlu.
[ ] Progress bar minimum 8.dp ve görünür.
[ ] Hedefli rutin metni doğru: `0 / 2 litre · 0 günlük seri`.
[ ] Check-type rutin açıklaması eksik kalmıyor.
[ ] ContentDescription eklendi.
[ ] Build başarılı.
```

## Test Kriterleri

```text
[ ] Su iç artır butonuna basınca değer artıyor.
[ ] Su iç azalt butonuna basınca değer azalıyor.
[ ] Değer 0 iken azalt butonu tıklanmıyor.
[ ] Progress bar artan değeri görsel olarak yansıtıyor.
[ ] TalkBack için artır/azalt açıklamaları mevcut.
```

## Agent Notu

Bu sprintte rutin business logic’i değiştirme. Sadece UI control ve callback kullanımını güncelle.

---

# 10. Sprint 5 — Header, Günü Kapat ve Dünü Tamamla Polish

## Amaç

Ana ekranın en görünür 3 kartını 9/10 seviyesine çekmek:

- Header / Dashboard Hero
- Günü Kapat kartı
- Dünü Tamamla kartı

## Kapsam

- TodayHeaderCard
- CloseDayCard / DayCloseCard
- MissedDayBanner / DünüTamamlaCard
- Moral chip semantic coloring
- Section labels
- CTA hierarchy

---

## 10.1 Header Polish

### Mevcut Yapı

```text
10 Mayıs, Pazar                 %0
Henüz tamamlanan yok
2 adım seni bekliyor
[Seri 0] [Moral %29]
Progress
```

### Hedef Yapı

```text
10 Mayıs, Pazar                 %0
Henüz tamamlanan yok
Bugün 2 adım seni bekliyor
[Seri 0] [Moral düşük]
Progress
```

### Moral Chip Semantiği

Moral değeri için renk ve label eşleşmesi:

| Aralık | Label | Renk |
|---:|---|---|
| 0-30 | Moral düşük | Warning / amber |
| 31-60 | Moral orta | Purple / info |
| 61-100 | Moral iyi | Success / green |

Eğer yüzde gösterilmek istenirse:

```text
Moral düşük · %29
```

Ama chip içinde çok metin kalabalıklaşırsa sadece `Moral düşük` kullanılmalı.

### Kabul Kriterleri

```text
[ ] Header metni doğal: `Bugün 2 adım seni bekliyor`.
[ ] Moral chip değere göre semantik renk alıyor.
[ ] Header progress bar görünür.
[ ] Header kartı normal surface üzerinde premium görünüyor.
```

---

## 10.2 Günü Kapat Kartı Polish

### Problem

Kart pasifken çok canlı görünürse kullanıcı tıklanabilir sanabilir.

### Hedef

Pasif ama değerli bir bilgilendirme kartı:

```text
🌙 Gün sonu
Günü kapat
21:00’dan sonra değerlendirme yapabilirsin.
```

### State Kuralları

| Durum | Görsel |
|---|---|
| Saatten önce | Disabled/Warning muted |
| Saatten sonra aktif | Daha belirgin, CTA içerir |
| Gün kapatılmış | Success state |

### Saatten Önce

```text
- Background: warningContainer veya disabledBackground
- Border: warningBorder muted
- Icon: warning, alpha 0.75
- Button yok
- Tıklanınca snackbar olabilir: `Gün değerlendirmesi 21:00’dan sonra açılacak.`
```

### Saatten Sonra

```text
- Background: surface
- Accent: warning veya success
- Button: `Günü kapat`
- Button 48dp height
```

### Gün Kapatılmış

```text
- Success accent
- Özet metni
- `Güncellle` veya `Özeti görüntüle` secondary action
```

### Kabul Kriterleri

```text
[ ] Pasif kart aktif CTA gibi görünmüyor.
[ ] Pasif kartın metni okunabilir.
[ ] Saatten sonra CTA belirginleşiyor.
[ ] Gün kapatılmış state success olarak ayrılıyor.
```

---

## 10.3 Dünü Tamamla Kartı Polish

### Problem

Dünü tamamlama butonu FAB ile yarışmamalı. Kart dikkat çekmeli ama ana primary action olmamalı.

### Hedef Yapı

```text
Dünü tamamla
9 Mayıs için kısa özet henüz yok.

[Değerlendir]      [Atla]
```

### Stil Kuralları

| Eleman | Stil |
|---|---|
| Kart | Attention variant, soft coral |
| Başlık | Attention color, readable |
| Açıklama | textSecondary |
| Değerlendir | Secondary outline veya soft filled |
| Atla | Ghost button, 48dp hitbox |

### Buton Hiyerarşisi

FAB ekrandaki ana primary action olduğu için:

```text
Değerlendir = Secondary
Atla = Ghost / tertiary
```

### Kabul Kriterleri

```text
[ ] Dünü tamamla kartı dikkat çekiyor ama FAB ile yarışmıyor.
[ ] Değerlendir butonu minimum 48dp.
[ ] Atla butonu minimum 48dp.
[ ] Açıklama kısa ve doğal.
[ ] Bottom nav/FAB kart butonlarını kapatmıyor.
```

---

## Sprint 5 Genel Kabul Kriterleri

```text
[ ] Header 9/10 polish seviyesinde.
[ ] Moral chip semantik renklendirildi.
[ ] Günü kapat pasif/aktif/kapalı state’leri ayrıldı.
[ ] Dünü tamamla kartı secondary action hiyerarşisine çekildi.
[ ] Button hitboxlar korunuyor.
[ ] Build başarılı.
```

## Agent Notu

Bu sprintte görsel hiyerarşi ana odak. FAB primary kalmalı. Diğer CTA’lar onu bastırmamalı.

---

# 11. Sprint 6 — Accessibility ve Hitbox Pass

## Amaç

Today ekranını erişilebilirlik ve mobil kullanım standartlarına uygun hale getirmek.

## Kapsam

- Minimum dokunma alanları
- contentDescription
- Role semantics
- Kontrast kontrolü
- Disabled state
- Large font dayanıklılığı

## Yapılacak İşler

### 6.1 Hitbox Standardı

Tüm tıklanabilir elemanlar:

```text
Minimum 48.dp x 48.dp
```

Kontrol edilecekler:

```text
[ ] Check circle
[ ] Routine more menu
[ ] Artır button
[ ] Azalt button
[ ] Değerlendir button
[ ] Atla button
[ ] FAB
[ ] Bottom nav itemları
[ ] Header chipleri eğer tıklanabilirse
```

### 6.2 contentDescription Listesi

| Eleman | contentDescription |
|---|---|
| FAB | `Yeni görev ekle` |
| Su iç check | `Su iç rutinini tamamla` |
| Su iç artır | `Su iç miktarını artır` |
| Su iç azalt | `Su iç miktarını azalt` |
| Rutin more | `Rutin seçenekleri` |
| Dünü değerlendir | `Dünkü gün özetini değerlendir` |
| Dünü atla | `Dünkü özeti atla` |
| Günü kapat | `Gün sonu değerlendirmesini aç` |

### 6.3 Role Semantics

Check circle:

```kotlin
.semantics {
    role = Role.Checkbox
    contentDescription = if (done) unmarkText else markText
}
```

Buttons:

```kotlin
.clickable(
    role = Role.Button,
    onClickLabel = ...
)
```

### 6.4 Kontrast Pass

Metin renkleri:

```text
Primary text:   #1F2937
Secondary text: #4B5563
Tertiary text:  #6B7280
Disabled text:  #9CA3AF
```

Kontrol edilecek metinler:

```text
[ ] Rutinler başlığı
[ ] Rutin açıklamaları
[ ] Hedefe ulaştığında tamamlanır
[ ] Gün sonu açıklaması
[ ] Dünü tamamla açıklaması
[ ] Disabled button text
[ ] Bottom nav labels
```

### 6.5 Large Font Kontrolü

Aşağıdaki durumlar büyük fontta bozulmamalı:

```text
[ ] Header kartı
[ ] Rutin kartı
[ ] Progress control row
[ ] Günü kapat kartı
[ ] Dünü tamamla kartı
[ ] Bottom nav
```

## Kabul Kriterleri

```text
[ ] Tüm tıklanabilir alanlar minimum 48.dp.
[ ] Icon buttonlarda contentDescription var.
[ ] Check circle Role.Checkbox kullanıyor.
[ ] Renk tek başına anlam taşımıyor; metin/ikon destekli.
[ ] Kontrastı düşük metin kalmadı.
[ ] Font büyüyünce layout kırılmıyor.
[ ] Build başarılı.
```

## Test Kriterleri

```text
[ ] TalkBack açıkken rutin tamamla aksiyonu anlaşılır.
[ ] TalkBack açıkken artır/azalt aksiyonları anlaşılır.
[ ] Dünü değerlendir ve atla aksiyonları okunur.
[ ] Disabled azalt butonu disabled olarak algılanır.
```

## Agent Notu

Bu sprint UX kalitesini ciddi artırır. Görsel değişiklik küçük olabilir ama puan etkisi büyüktür.

---

# 12. Sprint 7 — QA, Preview, Regression ve Final Polish

## Amaç

Tüm değişiklikleri birlikte kontrol etmek ve ekranı release-ready hale getirmek.

## Kapsam

- UI preview matrix
- Manual QA
- Regression checklist
- Small visual polish
- Bottom padding / FAB overlap kontrolü

## Yapılacak İşler

### 7.1 Preview Matrix

Aşağıdaki preview’lar hazırlanmalı veya güncellenmeli:

```text
[ ] TodayEmptyPreview
[ ] TodayTwoRoutinesPreview
[ ] TodayTargetRoutinePreview
[ ] TodayDayCloseDisabledPreview
[ ] TodayDayCloseActivePreview
[ ] TodayClosedDayPreview
[ ] TodayMissedDayPreview
[ ] TodayLargeFontPreview
[ ] TodayDarkModePreview
```

### 7.2 Manual QA Checklist

```text
[ ] 360dp küçük ekran
[ ] 411dp standart ekran
[ ] Büyük font
[ ] Dark mode
[ ] Light mode
[ ] Keyboard açıkken FAB ve kartlar
[ ] 10+ rutin
[ ] 5+ missed/overdue senaryosu
[ ] Gün sonu pasif state
[ ] Gün sonu aktif state
[ ] Gün kapatılmış state
[ ] TalkBack smoke test
```

### 7.3 FAB / Bottom Nav Overlap Kontrolü

LazyColumn bottom padding yeterli olmalı.

Öneri:

```kotlin
contentPadding = PaddingValues(
    start = 16.dp,
    end = 16.dp,
    top = 18.dp,
    bottom = 160.dp,
)
```

veya bottom nav + FAB ölçüsüne göre dinamik safe padding.

### 7.4 Small Polish

Kontrol edilecekler:

```text
[ ] More menu dots sağ kenara yapışık değil.
[ ] Section başlıkları kartlara uygun mesafede.
[ ] Progress bar vertical alignment düzgün.
[ ] Header chipleri aynı yükseklik ve padding değerinde.
[ ] Kart radius değerleri tutarlı.
[ ] Kart padding değerleri tutarlı.
```

## Kabul Kriterleri

```text
[ ] Tüm preview’lar çalışıyor.
[ ] Manual QA checklist tamamlandı.
[ ] FAB kart butonlarını kapatmıyor.
[ ] Bottom nav ile içerik çakışmıyor.
[ ] Build başarılı.
[ ] Agent final raporu oluşturdu.
```

---

# 13. Agent İçin Uygulama Sırası

Agent işleri şu sırayla yapmalıdır:

```text
1. Sprint 0: Davranış kilidi ve preview state
2. Sprint 1: Türkçe metin/lokalizasyon
3. Sprint 2: TodayColors semantic token sistemi
4. Sprint 3: TodayCard + TodayButton sistemleri
5. Sprint 4: Routine interaction redesign
6. Sprint 5: Header / day close / missed day polish
7. Sprint 6: Accessibility + hitbox pass
8. Sprint 7: QA + preview + final polish
```

Bu sıra değiştirilmemelidir. Özellikle:

```text
- Renk tokenları kurulmadan card polish yapılmamalı.
- Button sistemi kurulmadan routine artır/azalt redesign yapılmamalı.
- Accessibility pass en sona yakın yapılmalı.
```

---

# 14. Agent’a Verilecek Ana Prompt

Aşağıdaki prompt doğrudan coding agent’a verilebilir:

```text
Benim Günlerim projesinde Today/Bugün ana sayfasını UI/UX açısından 9/10 seviyesine çıkarmak istiyorum.

Lütfen aşağıdaki sprint sistemini sırayla uygula. Business logic’i bozma; öncelik UI componentleri, Türkçe metinler, semantic color system, button/card sistemi, routine interaction redesign, accessibility ve hitbox düzeltmeleri.

Kurallar:
- Görev/rutin/gün kapatma davranışlarını değiştirme.
- Tıklanabilir her alan minimum 48.dp olmalı.
- Tüm metinler stringResource üzerinden yönetilmeli.
- Türkçe karakter hatası kalmamalı.
- Renkler hardcoded değil semantic token sisteminden gelmeli.
- Pasif state aktif gibi görünmemeli.
- FAB primary action olarak kalmalı; diğer butonlar secondary/ghost olmalı.
- Her sprint sonunda build al ve kısa rapor üret.

Sprintleri şu sırayla tamamla:
1. Davranış kilidi ve preview state
2. Lokalizasyon ve metin temizliği
3. TodayColors semantic color system
4. TodayCard ve TodayButton sistemi
5. Routine interaction redesign: Artır/Azalt ikon butonları, 48dp hitbox, 8-10dp progress bar
6. Header, Günü Kapat ve Dünü Tamamla polish
7. Accessibility ve hitbox pass
8. QA, preview matrix ve final polish

Her sprint sonunda:
- Değişen dosyaları listele
- Kabul kriterlerini işaretle
- Build/test durumunu bildir
- Kalan riskleri yaz
```

---

# 15. Sprint Bazlı Agent Output Formatı

Agent her sprint sonunda şu formatta rapor vermeli:

```text
## Sprint X Raporu

### Yapılanlar
- ...

### Değişen Dosyalar
- ...

### Kabul Kriterleri
[ ] ...
[x] ...

### Test / Build
- Build: Başarılı / Başarısız
- Test: Başarılı / Başarısız / Eklenmedi

### Görsel / UX Etki
- ...

### Kalan Riskler
- ...

### Sonraki Sprint İçin Notlar
- ...
```

---

# 16. Final Definition of Done

Tüm sprintler tamamlandığında aşağıdaki maddeler sağlanmış olmalıdır:

```text
[ ] Today ekranında Türkçe karakter hatası yok.
[ ] Tüm görünen metinler doğal ve profesyonel.
[ ] TodayColors semantic token sistemi var.
[ ] Kart renkleri anlamlı state’lere bağlı.
[ ] TodayCard variant sistemi var.
[ ] Button hiyerarşisi net: primary / secondary / ghost / icon / disabled.
[ ] FAB primary action olarak kalıyor.
[ ] Dünü tamamla butonu FAB ile yarışmıyor.
[ ] Günü kapat pasif state aktif gibi görünmüyor.
[ ] Rutin artır/azalt aksiyonları 48.dp ikon buton.
[ ] Azalt 0 değerinde disabled.
[ ] Progress bar minimum 8.dp ve görünür.
[ ] Tüm tıklanabilir alanlar minimum 48.dp.
[ ] contentDescription eksikleri tamamlandı.
[ ] Check/routine toggle Role.Checkbox kullanıyor.
[ ] Düşük kontrastlı metin kalmadı.
[ ] Large font ile layout kırılmıyor.
[ ] Bottom nav ve FAB içerikle çakışmıyor.
[ ] Preview matrix hazır.
[ ] Manual QA checklist tamamlandı.
[ ] Build başarılı.
```

---

# 17. 9/10 Sonuç Kriteri

Bu plan tamamlandığında ekran şu seviyede olmalıdır:

```text
- Kullanıcı ilk bakışta bugünün durumunu anlar.
- Ana aksiyonun ne olduğu nettir.
- Rutin artır/azalt aksiyonu zahmetsizdir.
- Pasif kartlar pasif gibi görünür.
- Dünü tamamlama destekleyici, cezalandırıcı olmayan bir dille sunulur.
- Renkler sadece güzel değil, anlamlıdır.
- Metinler profesyonel ve Türkçedir.
- Zayıf görüşlü kullanıcılar temel metinleri okuyabilir.
- Mobil dokunma alanları hata oranını azaltır.
- Kod tarafında component sistemi sürdürülebilirdir.
```

Hedef final puan:

```text
Görsel Tasarım: 9.0 / 10
Kullanılabilirlik: 9.0 / 10
Erişilebilirlik: 9.0 / 10
Profesyonellik: 9.5 / 10
Etkileşim Tasarımı: 9.0 / 10
Genel: 9.0 / 10
```
