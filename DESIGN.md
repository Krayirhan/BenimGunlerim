# BenimGünlerim — Tasarım Standardı

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
