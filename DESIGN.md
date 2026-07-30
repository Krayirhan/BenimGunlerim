# BenimGünlerim — Tasarım Standardı

## Değişmez Global UI Sözleşmesi

Bu belge uygulamanın tek tasarım kaynağıdır. Yeni ya da değişen hiçbir ekran aşağıdaki kurallardan istisna oluşturamaz; istisna ancak önce bu belge güncellenirse geçerlidir.

- Ana sekmeler (`Bugün`, `Plan`, `Rutinler`, `İlerleme`, `Ayarlar`) aynı uygulama iskeletini kullanır: durum çubuğunun altında ortak `AppTopBar`, altta ortak navigation bar. Sayfa ismi ayrıca büyük bir ekran başlığı olarak tekrar edilmez.
- Hero/header yüzeyinde **hiçbir başlık veya açıklama metni render edilmez**. Hero kullanılacaksa yalnızca tarih seçici, yüzde, sayaç ya da durum özeti gibi işlevsel içerik taşır; başlıksız özet yüzeyi `ScreenSummaryCard` kullanılır.
- Ekranlar 4dp/8dp ritmi, standart yatay 16dp boşluk ve bölüm arası 24dp boşluk kullanır. Metin ve ikonlar kapsayıcılarında dikey olarak ortalanır; font padding kapalıdır.
- Kart, satır yüzeyi, dialog ve seçim kontrollerinin temel yarıçapı 12dp; küçük seçim/chip yarıçapı 8dp'dir. Header gerektiğinde tamamen dikdörtgendir. Daha yuvarlak yüzey kullanılmaz.
- Kartlar gölgesizdir. Ayrım; 1dp düşük kontrastlı border, yüzey tonu ve 8dp boşlukla kurulur.
- Tek öğeli listede yalnızca öğenin kendi yüzeyi vardır. İki veya daha fazla öğede yalnızca bir dış kapsayıcı yüzey vardır; iç satırlar düz kalır. İç içe kart oluşturulmaz.
- Tamamlanan görev/rutin bulunduğu akışta kalır; durum yalnızca semantik renk, ikon ve metinle değişir. Kullanıcının bağlamı kaybolmaz.
- Dokunulabilir alanlar en az 48dp'dir; her anlamlı ikonun içerik açıklaması vardır. Disabled, boş, yükleniyor ve hata durumları aynı yerleşim dilini korur.
- Topbar, bottom navigation, kart kabuğu ve tipografi doğrudan yeniden çizilmez; ortak bileşen/tokens kullanılır. Ekran düzeltmesinden sonra tek öğe, çok öğe, tamamlanmış ve boş durum ayrı ayrı kontrol edilir.

> Son güncelleme: 2026-07-30  
> Amaç: BenimGünlerim’in hedeflenen görünümünü, hissini ve arayüz kararlarını herkes için aynı biçimde tanımlamak.

## 1. Tasarım Vaadi

BenimGünlerim, kullanıcıya gününü kontrol altına alıyormuş gibi hissettiren; sıcak, sakin ve anlaşılır bir günlük yaşam yardımcısı olmalıdır.

Arayüz şu hissi vermelidir:

> “Bugün yapacaklarım anlaşılır. Küçük bir adım atabilirim. Geri kalanını sonra düşünürüm.”

Uygulama yoğun bir iş yönetim paneli, sert bir performans aracı veya tam teşekküllü bir RPG oyunu gibi görünmemelidir.

## 2. Tasarım Hedefleri

- Kullanıcı Bugün ekranında birkaç saniye içinde odağını anlayabilmelidir.
- Bir görev veya rutini tamamlamak fiziksel ve görsel olarak tatmin edici olmalıdır.
- Gün kaçırıldığında arayüz suçluluk değil, yeniden başlama hissi vermelidir.
- Planlama güçlü olmalı; ancak günlük kullanım gereksiz ayrıntılarla yorulmamalıdır.
- İlerleme görünür olmalı; puan ve ödüller ana amaç haline gelmemelidir.
- Uygulama Türkçe, erişilebilir ve küçük ekranlarda rahat kullanılabilir olmalıdır.

## 3. Tasarım İlkeleri

### Küçük adım önce gelir

Her ekranda kullanıcının bir sonraki makul eylemi açık olmalıdır. Öncelik, aynı anda her şeyi göstermek değil, doğru sonraki adımı göstermektir.

### Sakin başarı hissi

Tamamlama anlarında net state değişimi, hafif haptic geri bildirim ve ölçülü kutlama kullanılır. Konfeti, rozet ve animasyonlar kısa sürer; içerikten rol çalmaz.

### Yumuşak yönlendirme

Arayüz “yapmalısın” demez. “Yarına taşıyabilirsin”, “küçük bir adım ekleyebilirsin” ve “yeniden devam edebilirsin” gibi destekleyici bir dil kullanır.

### Bilgi hiyerarşisi dekorasyondan önemlidir

Renk, boşluk, tipografi ve kartlar kullanıcının neye bakması gerektiğini anlatmak için vardır. Süsleme, anlamın önüne geçmez.

### Tutarlılık güven yaratır

Aynı eylem, aynı ikon, aynı renk anlamı ve aynı geri bildirim biçimiyle çalışmalıdır. Ekranlar birbirinden farklı ürünler gibi görünmemelidir.

## 4. Görsel Karakter

### Genel ifade

- Açık zeminler
- Yumuşak pastel vurgular
- Yuvarlatılmış kartlar
- Cömert boşluklar
- Güçlü ama dostça tipografi
- Düşük gürültülü arka planlar
- Az sayıda, anlamlı ikon

### Kaçınılacak ifade

- Aşırı koyu veya neon renk yüzeyleri
- Her yerde gölge ve gradient kullanımı
- Bir ekranda çok sayıda vurgu rengi
- Uzun, sıkışık metin blokları
- Kart içinde kart içinde kart yapısı
- Aynı anda birden fazla ana buton
- Kullanıcıyı baskılayan kırmızı uyarılar ve skor dili

## 5. Renk Sistemi

Renkler dekoratif değil, anlam taşıyan işaretlerdir. Uygulama genelinde renk kullanımının semantik anlamı korunmalıdır.

| Rol | Kullanım | Davranış |
|---|---|---|
| Ana vurgu | Birincil eylem, seçili durum, ana ilerleme | Kullanıcıyı bir sonraki eyleme yönlendirir. |
| İkincil vurgu | Destekleyici bilgi, alternatif eylem | Ana eylemle yarışmaz. |
| Tamamlandı | Başarı, tamamlanma, pozitif durum | Sakin ve güven verici görünür. |
| Uyarı | Geciken iş, dikkat gerektiren durum | Aciliyet bildirir; suçlayıcı değildir. |
| Yüzey | Kartlar, bottom sheet, bilgi alanları | İçeriği arka plandan ayırır. |
| Arka plan | Ekran zemini | Sessiz kalır, göz yormaz. |
| Metin | Başlık, gövde, ikincil bilgi | Kontrast ve okunabilirlik önceliklidir. |

Uygulanan token kaynakları:

- `app/src/main/java/com/benimgunlerim/ui/theme/DesignTokens.kt`
- `app/src/main/java/com/benimgunlerim/ui/theme/Theme.kt`
- `app/src/main/java/com/benimgunlerim/ui/today/theme/TodayColorTokens.kt`

### Renk kuralları

1. Aynı durum, tüm ekranlarda aynı renk ailesiyle anlatılır.
2. Bir kartta en fazla bir baskın vurgu rengi kullanılır.
3. Sadece renge dayanarak anlam verilmez; metin, ikon veya şekil desteklenir.
4. Pasif öğeler düşük kontrastta kaybolmamalıdır.
5. Kırmızı tonlar yalnızca gerçek uyarı/hata durumlarında kullanılır.

## 6. Tipografi ve Metin Hiyerarşisi

Tipografi hızlı taramayı desteklemelidir. Bir ekranda aynı anda en fazla üç anlam seviyesi görünür olmalıdır: başlık, destekleyici bilgi ve eylem/metric.

| Seviye | Kullanım | Kural |
|---|---|---|
| Ekran başlığı | Hero kartı ve ana ekran adı | Kısa, güçlü, tek odaklı. |
| Bölüm başlığı | Görevler, Rutinler, Gecikenler | İçeriği gruplar; açıklayıcı olmalıdır. |
| Ana metric | Tamamlanma, seri, XP | Büyük ve belirgin; tek metric öne çıkar. |
| Gövde | Açıklama, rehber, not | Kısa cümleler, iki-üç satırı geçmeyen bloklar. |
| Etiket | Chip, küçük durum, buton | Açık ve eylem odaklı. |

Kullanıcıya görünen tüm metinler `res/values/strings.xml` üzerinden yönetilir. Composable içinde kullanıcı metni yazılmaz.

## 7. Yerleşim ve Boşluk

### Ekran iskeleti

Çoğu ana ekranda önerilen yapı:

```text
Sistem barı
  └─ Hero / ekran özeti
      └─ Ana odak veya metric
  └─ Öncelikli içerik
  └─ Destekleyici içerik
  └─ Güvenli alt boşluk
Alt navigasyon + gerekirse FAB
```

### Boşluk kuralları

- Aynı gruptaki öğeler birbirine yakın; farklı gruplar daha uzak olmalıdır.
- Liste satırlarında dokunma alanı görsel sınırdan geniş tutulmalıdır.
- Kart içindeki boşluk, kartlar arasındaki boşluktan küçük olmalıdır.
- Uzun listelerde FAB ve bottom navigation için güvenli alt alan bırakılmalıdır.
- Küçük ekranlarda bilgi gizlemek yerine sıralama sadeleştirilmelidir.

## 8. Bileşen Standardı

Ortak bileşenler `ui/components/Common.kt` içinde korunur. Yeni ekranlar mümkün olduğunca bunları kullanmalıdır.

### ScreenHeroCard

Ekranın karar vermeyi kolaylaştıran tek özet alanıdır.

- Başlık ve kısa alt başlık içerir.
- En fazla bir ana metric veya bir metric satırı taşır.
- Aynı ekranda ikinci hero kartı kullanılmaz.
- Bilgi yoğunluğu, ekranın ana eylemini gölgelememelidir.

### AppCard ve bilgi kartları

- Tek bir bilgi grubu veya tek bir eylem kümesi içerir.
- Görsel hiyerarşi için padding, başlık ve ikincil metin kullanır.
- Sadece önemli durumlarda border veya renkli yüzey kullanır.

### Butonlar

| Tür | Kullanım |
|---|---|
| Ana buton | Ekrandaki en önemli ve geri dönüşü yüksek eylem |
| İkincil buton | Alternatif ya da destekleyici eylem |
| Metin butonu | Düşük riskli, bağlamsal işlem |
| İkon butonu | Sık kullanılan, alan tasarrufu gerektiren açık eylem |
| FAB | Yeni görev ekleme gibi sürekli erişilmesi gereken ana oluşturma eylemi |

Bir görünümde birden fazla ana buton varsa, öncelik yeniden değerlendirilmelidir.

### Empty state

Boş durumlar eksikliği değil fırsatı anlatmalıdır.

- Ne olduğunu söyle
- Kullanıcının atabileceği küçük adımı öner
- Gerekirse tek bir CTA sun

Örnek: “Bugün için küçük bir adım ekleyebilirsin.”

### Alert ve hata kartları

- Sorunu açıkça belirtir.
- Mümkünse çözüm veya yeniden deneme eylemi sunar.
- Hata teknik ayrıntılarını kullanıcıya göstermez.
- Kırmızı rengi ölçülü kullanır.

## 9. Ekran Bazlı Tasarım Standardı

### Bugün

Bugün ekranı ürünün merkezi olmalıdır.

Öncelik sırası:

1. Günün durumu ve ilerleme
2. Açık görevler ve rutinler
3. Gecikenler
4. Tamamlananlar
5. Gün kapatma veya missed day

Kurallar:

- Açık işler tamamlananlardan önce gelir.
- Geciken işler görünür olmalı, ama tüm ekranı ele geçirmemelidir.
- Rutin ve görev satırları aynı görsel dilde, farklı davranışı açıkça anlatacak biçimde görünmelidir.
- Tamamlama kontrolü kolay ulaşılır ve yeterince büyük olmalıdır.
- Gün kapatma, sadece zamanı geldiğinde ana eylem gibi görünmelidir.

### Plan

Plan ekranı geleceği düzenler; Bugün ekranının kopyası değildir.

- Tarih navigasyonu hızlı ve anlaşılırdır.
- Seçili gün net biçimde görülür.
- Görev ekleme sheet’i önce minimum alanları ister, ayrıntıları isteğe bağlı tutar.
- Geciken görevleri taşıma eylemi geri dönüşü anlaşılır biçimde anlatır.

### Rutinler

- Aktif rutin sayısı ve bugünkü durum üst bölümde özetlenir.
- Check tipi ve hedef tipi rutinler görsel olarak ayırt edilir.
- Artır/azalt eylemleri küçük görünse bile yeterli dokunma alanına sahip olur.
- Hedef progress değeri her zaman metinle okunabilir kalır.
- Arşivleme veya atlama gibi ikincil eylemler ana tamamlanma eylemiyle yarışmaz.

### İlerlemen ve başarımlar

- Grafikler ve metricler kullanıcının bir şey yapmasına yardım etmiyorsa sadeleştirilir.
- Başarımlar kutlanır; henüz açılmamış olanlar baskı unsuru gibi kullanılmaz.
- Günlük performans, “iyi/kötü insan” yargısı değil bir gözlem olarak sunulur.

### Ayarlar

- Ayarlar net bölümlere ayrılır: deneyim, bildirim, gizlilik, veri.
- Riskli eylemler (veri silme, içe aktarma) onay gerektirir.
- Yerel veri ve dışa aktarma uyarıları görünür, sade ve anlaşılırdır.

## 10. Durumlar ve Geri Bildirim

Her kritik bileşen aşağıdaki durumları tasarlamalıdır:

| Durum | Beklenen davranış |
|---|---|
| Yükleniyor | İçeriğin yapısını koruyan sakin placeholder veya kısa loading durumu |
| Boş | Kullanıcıya küçük bir sonraki adımı gösteren empty state |
| Başarılı | State değişimi ve ölçülü olumlu geri bildirim |
| Hata | Açık mesaj ve mümkünse tekrar dene eylemi |
| Pasif | Neden pasif olduğunu anlatan düşük baskılı açıklama |
| Tamamlandı | Sonucu gösteren, fakat tekrar açılabilir/okunabilir durum |

### Hareket

- Animasyonlar kısa ve işlevsel olmalıdır.
- State değişimi anlaşılsın diye kullanılır; bekletmek için kullanılmaz.
- Hareket azaltma tercihleri gelecekte desteklenmelidir.
- Kutlama efekti kullanıcı ayarlarından kapatılabilir olmalıdır.

## 11. Erişilebilirlik Standardı

Tasarım tamamlanmış sayılmaz; aşağıdakiler doğrulanmadan teslim edilmez:

- Her anlamlı ikon için `contentDescription`
- En az rahat dokunma alanı
- Renk dışı durum göstergesi
- Yeterli metin/zemin kontrastı
- Büyük font ölçeğinde taşmayan hiyerarşi
- TalkBack ile mantıklı odak sırası
- Tablet ve yatay ekran kontrolü

Bir bileşen görsel olarak güzel ancak dokunulamaz, okunamaz veya sesli erişilemezse tasarım hedefini karşılamaz.

## 12. İçerik Tonu

Kullanılacak dil:

- Sade
- Kısa
- Somut
- Destekleyici
- Yargılamayan

Kaçınılacak dil:

- “Yine yapmadın” gibi suçlayıcı cümleler
- Aşırı coşkulu ve sürekli ünlem kullanan metinler
- Teknik hata kodları
- Kullanıcıyı skoruyla tanımlayan ifadeler

| Durum | Tercih edilen ifade |
|---|---|
| Boş gün | “Bugün için küçük bir adım ekleyebilirsin.” |
| Tamamlama | “Bir adım daha tamamlandı.” |
| Erteleme | “Bunu daha uygun bir güne taşıyabilirsin.” |
| Kaçırılan rutin | “Yarın yeniden devam edebilirsin.” |
| Gün sonu | “Bugünü geride bıraktın.” |

## 13. Tasarım Karar Kontrol Listesi

Yeni bir ekran, bileşen veya değişiklik aşağıdaki sorulara olumlu cevap vermelidir:

- Kullanıcı bir sonraki adımı kolayca görüyor mu?
- Ana eylem açıkça öne çıkıyor mu?
- Bilgi hiyerarşisi, renk ve boşlukla anlaşılır mı?
- Metin kısa, Türkçe ve yargılamayan mı?
- Hata, boş ve tamamlanmış durumlar tasarlandı mı?
- Büyük font, TalkBack ve dokunma alanı düşünüldü mü?
- Ortak token ve bileşenler kullanıldı mı?
- Bu değişiklik Bugün deneyimini sadeleştiriyor mu?

## 14. İlgili Belgeler

- [Ürün felsefesi ve kullanıcı akışları](docs/product/benimgunlerim-urun-felsefesi-ve-akislari.md)
- [Güncel proje ve sprint durumu](docs/PROJECT_STATUS.md)
- [Kalite kapıları](docs/production/quality-gates.md)
- [Agent ve Graphify kuralları](AGENTS.md)

Teknik mimari ve veri katmanı açıklamaları bu belgenin kapsamı dışındadır; bunlar kod, Graphify grafiği ve production dokümanlarında tutulur.
## Today kart standardı (2026-07-30)

Today ekranındaki kartlar düşük radius ile, köşeye yakın fakat tamamen köşeli olmayan tek bir görsel dil kullanır. Ortak kart radius değeri 12dp'dir; görev satırları flat kalır, rutinler iki veya daha fazla öğe olduğunda düşük radius'lu tek dış kapsayıcı içinde gösterilir. Tek öğeli görev veya rutin bölümünde ikinci bir yüzey katmanı kullanılmaz. Today header ilerleme grafiği yalnızca bugünkü görev ve rutinlerin tamamlanma oranını gösterir; dairesel grafiğin merkezinde yüzde bulunur. Header özet kartı bilerek radius'suz, dikdörtgen bir yüzeydir. Gün sonu alanı pasif, aktif ve tamamlanmış durumların tamamında gece temalı koyu yüzey kullanır; eylem metni günü değerlendirme/kapatma amacını açıkça belirtir.
## Uygulama çerçevesi standardı (2026-07-30)

Ana uygulama sekmelerinin tamamı — Bugün, Plan, Rutinler, İlerleme ve Ayarlar — tek, ortak scaffold içinde aynı `AppTopBar` ve bottom navigation bileşenini kullanır. Topbar; profil, uygulama adı, bildirim aksiyonu ve net alt ayırıcıdan oluşur. Sayfalar kendi içerik başlıklarını taşıyabilir; ancak uygulama düzeyindeki navigasyon çerçevesini yeniden oluşturmaz.
## 15. Global UI Kuralları

### Uygulama çerçevesi

- Ana uygulama sayfaları aynı `AppTopBar` ve bottom navigation içinde çalışır.
- Topbar status bar inset'ini tüketir; içerik sistem çubuğunun altından başlar.
- Bottom navigation ve FAB birbirinin dokunma alanını kapatmaz.
- Sayfalar ortak scaffold'ı yeniden üretmez.

### Geometri ve yüzey

- Tüm ölçüler 4dp/8dp grid mantığına uyar.
- Ekran yatay padding'i 20–24dp, bölüm aralığı 20–24dp, kart iç padding'i 16–20dp'dir.
- Ortak kart radius'u 12dp'dir; küçük öğeler 8dp, özel hero alanları en fazla 16dp kullanır.
- Tek öğeli listelerde ikinci dış yüzey/kart katmanı kullanılmaz.
- Metin, ikon, checkbox ve menü gerçek ölçüleri üzerinden aynı eksende merkezlenir.
- Varsayılan kart ve satırlarda gölge/elevation kullanılmaz; ayrım border, yüzey tonu ve boşlukla sağlanır.

### Tipografi ve etkileşim

- Tüm metinler ortak `AppTypography` ve `strings.xml` üzerinden yönetilir.
- Font padding kapalıdır; baseline ve satır yüksekliği ortak typography token'larıyla belirlenir.
- Her tıklanabilir alan en az 48dp olmalıdır.
- Loading, empty, error, disabled, partial ve completed durumları tasarlanmış olmalıdır.
- Tamamlanan öğeler beklenmedik başka bir bölüme taşınmamalı; aynı bağlamda durum değiştirmelidir.

### Responsive ve erişilebilirlik

- 360dp, 411dp ve tablet genişlikleri ile büyük font ölçeği kontrol edilir.
- Her anlamlı ikon `contentDescription` taşır; dekoratif ikon açıkça null olur.
- Kontrast, TalkBack sırası, touch target ve landscape görünümü teslim öncesi doğrulanır.
- Animasyonlar kısa, işlevsel ve sistem animasyon azaltma tercihine saygılıdır.

## 16. Tasarım Teslim Kontrolü

- Açık/kapalı, loading/empty/error ve completed durumları görüldü.
- Tek ve çok öğeli liste davranışı kontrol edildi.
- Sistem status bar, ortak topbar, bottom navigation ve FAB çakışmıyor.
- Metin baseline'ları, ikonlar ve çevre yüzeyler ölçülebilir eksenlerde hizalı.
- Gölge, radius, spacing, renk ve typography token dışına çıkılmadı.
