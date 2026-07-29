# BenimGünlerim — Ürün Felsefesi, Amaçları ve Kullanıcı Akışları

## 1. Bu Belgenin Amacı

Bu belge, BenimGünlerim’in neyi çözmeye çalıştığını, kullanıcıya nasıl bir deneyim sunacağını ve ürün geliştirme kararlarının hangi ilkelerle alınacağını tanımlar.

Bu doküman; ürün, tasarım, yazılım, içerik ve agent çalışmalarında ortak referans olarak kullanılmalıdır.

## 2. Ürünün Kısa Tanımı

BenimGünlerim, kullanıcının gününü küçük ve yönetilebilir adımlarla planlamasına, görevlerini ve rutinlerini takip etmesine ve ilerlemesini görünür biçimde hissetmesine yardımcı olan sade bir günlük yaşam uygulamasıdır.

Ürün yalnızca bir yapılacaklar listesi değildir. Görev yönetimi, rutin takibi, günlük değerlendirme ve hafif oyunlaştırmayı tek bir sakin deneyimde birleştirir.

## 3. Temel Ürün Amacı

Kullanıcı uygulamayı açtığında üç soruya hızlıca cevap bulmalıdır:

1. Bugün ne yapmam gerekiyor?
2. Şu ana kadar neleri tamamladım?
3. Günümü biraz daha iyi geçirmek için sıradaki küçük adım ne?

Ürün, kullanıcıyı daha fazla iş yapmaya zorlamayı değil, önemli işleri daha anlaşılır ve yapılabilir hale getirmeyi amaçlar.

## 4. Ürün Felsefemiz

### 4.1 Küçük adımlar önemlidir

Bir görevin tamamlanması küçük görünse bile kullanıcı için gerçek bir ilerlemedir. Arayüz ve metinler bu ilerlemeyi görünür kılmalıdır.

### 4.2 Kullanıcı suçlanmaz

Bir görev ertelendiğinde veya bir rutin kaçırıldığında uygulama cezalandırıcı, küçümseyici ya da kaygı artırıcı bir dil kullanmaz. Kullanıcıya yeniden başlama ve bir sonraki adıma geçme imkânı sunar.

### 4.3 Gün, görevlerden ibaret değildir

Ürün sadece verimliliği değil, kullanıcının enerjisini, moralini, rutinlerini ve günün genel akışını da dikkate alır.

### 4.4 İlerleme görünür olmalıdır

Tamamlanma oranı, günlük skor, seri, XP ve başarımlar; kullanıcıyı baskılamak için değil, çabasını fark ettirmek için kullanılır.

### 4.5 Sadelik özellik sayısından önemlidir

Her yeni özellik kullanıcıya “Bugün ne yapacağım?” sorusunda yardımcı olmuyorsa ürünün merkezine alınmamalıdır.

### 4.6 Kontrol kullanıcıdadır

Kullanıcı görevlerini, rutinlerini, bildirimlerini ve verilerini yönetebilmelidir. Ürün, kullanıcının hayatını onun yerine yönetmez.

## 5. Hedef Kullanıcı İhtiyacı

BenimGünlerim özellikle şu ihtiyaçlara cevap verir:

- Gününü zihninde taşımaktan yorulmak
- Büyük işleri küçük parçalara bölememek
- Rutinleri sürdürmekte zorlanmak
- Ne kadar ilerlediğini görememek
- Karmaşık üretkenlik araçlarında kaybolmak
- Sert ve suçluluk oluşturan takip sistemlerinden uzak durmak

## 6. Ana Kullanıcı Döngüsü

Ürünün ana döngüsü şudur:

```text
Planla → Bugün gör → Küçük adımı tamamla → İlerlemeni fark et → Günü değerlendir → Yarın devam et
```

Bu döngünün her adımı mümkün olduğunca kısa, anlaşılır ve geri döndürülebilir olmalıdır.

## 7. Temel Kullanıcı Akışları

### 7.1 İlk kullanım akışı

Amaç: Kullanıcıyı uzun bir kurulumla yormadan ilk faydayı göstermek.

1. Kullanıcı karşılanır.
2. Uygulamanın yaklaşımı kısa biçimde anlatılır: küçük adımlar, görevler ve rutinler.
3. Kullanıcı isterse örnek görev veya rutin şablonlarından birini seçer.
4. Kullanıcı ilk görevini ekler.
5. Uygulama kullanıcıyı doğrudan Bugün ekranına götürür.

İlk kullanımın başarı ölçütü, kullanıcının ilk dakikalar içinde en az bir anlamlı görev oluşturabilmesidir.

### 7.2 Görev oluşturma akışı

1. Kullanıcı görevi mümkün olduğunca az alanla ekler.
2. İsterse tarih, alt görev, kategori ve hatırlatıcı belirler.
3. Görev seçilen güne eklenir.
4. Kullanıcı görevi Bugün veya Plan ekranında görür.

Görev oluşturma akışı hızlı olmalıdır. Ayrıntılar ilk adımda zorunlu hale getirilmemelidir.

### 7.3 Günlük görev tamamlama akışı

1. Kullanıcı Bugün ekranını açar.
2. Görevlerin ve rutinlerin öncelikli bir özeti gösterilir.
3. Kullanıcı bir görevi tamamlar.
4. Arayüz tamamlanmayı net biçimde gösterir.
5. Günlük ilerleme, seri veya ödül bilgisi güncellenir.
6. Kullanıcı sıradaki küçük adıma yönlendirilir.

Tamamlama anı ürünün en önemli duygusal anıdır. Hızlı tepki, görünür geri bildirim ve sakin bir başarı hissi verilmelidir.

### 7.4 Rutin oluşturma ve sürdürme akışı

1. Kullanıcı bir rutin adı belirler.
2. Rutin sıklığını seçer.
3. İsterse hatırlatıcı ve hedef tanımlar.
4. Rutin Bugün akışında görünür.
5. Kullanıcı her tamamlamayı işaretler.
6. Uygulama geçmişi ve seriyi gösterir.

Rutinler katı bir zorunluluk gibi değil, kullanıcının hayatına destek olan tekrarlar gibi sunulmalıdır.

### 7.5 Planlama akışı

Plan ekranı, kullanıcının yalnızca bugünü değil, yakın geleceği de düzenlemesine yardımcı olur.

- Tarih seçilir.
- O güne ait görevler görüntülenir.
- Yeni görev eklenir veya mevcut görev düzenlenir.
- Bekleyen görev başka bir güne taşınabilir.
- Kullanıcı tekrar Bugün ekranına döner.

Planlama, Bugün ekranından kopuk bir yönetim alanı olmamalı; Bugün deneyimini beslemelidir.

### 7.6 Gün sonu akışı

1. Kullanıcı günün tamamlanan ve bekleyen işlerini görür.
2. Günlük ilerleme ve skor özetlenir.
3. Eksik kalan işler suçlayıcı olmayan bir dille gösterilir.
4. Kullanıcı isterse günü kapatır.
5. Sonraki gün için açık ve taşınması gereken işler belirlenir.

Gün sonu amacı performans değerlendirmesi yapmak değil, günü kapatıp zihinsel yükü azaltmaktır.

### 7.7 İlerleme ve başarımlar akışı

İlerleme ekranı kullanıcıya şu soruların cevabını vermelidir:

- Son günlerde ne kadar istikrarlıyım?
- Hangi görev veya rutinlerde ilerliyorum?
- Hangi başarımları kazandım?
- Bir sonraki küçük hedefim ne olabilir?

Ödüller davranışın yerine geçmez; davranışın fark edilmesini sağlar. Sistem kullanıcıyı sürekli ödül toplamaya değil, sürdürülebilir ilerlemeye yönlendirmelidir.

### 7.8 Bildirim akışı

Bildirimler yardımcı olmalı, rahatsız edici olmamalıdır.

- Bildirimler kullanıcının seçimine bağlıdır.
- Sessiz saatler desteklenir.
- Aynı hatırlatma tekrar tekrar baskı oluşturmamalıdır.
- Bildirim metni kısa, açık ve yargılamayan olmalıdır.

Örnek ton:

> “Bugünkü küçük adımın seni bekliyor.”

Kaçınılması gereken ton:

> “Yine görevini yapmadın.”

## 8. İçerik ve Metin İlkeleri

### Kullanılacak dil

- Sade
- Sıcak
- Kısa
- Destekleyici
- Somut
- Türkçe karakterleri doğru kullanan

### Kaçınılacak dil

- Suçlayıcı ifadeler
- Aşırı motivasyon klişeleri
- Kullanıcıyı başarısız ilan eden mesajlar
- Gereksiz teknik terimler
- Sürekli ünlem ve aşırı coşku

### Metin örnekleri

| Durum | Tercih edilen ifade | Kaçınılacak ifade |
|---|---|---|
| Hiç görev yok | “Bugün için küçük bir adım ekleyebilirsin.” | “Bugün hiçbir şey yapmadın.” |
| Görev tamamlandı | “Güzel, bir adım daha tamamlandı.” | “Mükemmel! Harikasın!!!” |
| Görev ertelendi | “Bunu daha uygun bir güne taşıyabilirsin.” | “Yine erteledin.” |
| Gün tamamlandı | “Bugünü geride bıraktın.” | “Bugün başarılı/başarısız oldun.” |
| Rutin kaçırıldı | “Yarın yeniden devam edebilirsin.” | “Serin bozuldu, her şey bitti.” |

## 9. Ürünün Sınırları

BenimGünlerim’in her şeyi yapan bir üretkenlik platformuna dönüşmesi istenmez.

Şimdilik ürünün merkezine alınmaması gerekenler:

- Karmaşık proje yönetimi
- Takım ve kurumsal çalışma özellikleri
- Yoğun sosyal rekabet
- Aşırı RPG mekanikleri
- Kullanıcıyı sürekli çevrimiçi olmaya zorlayan sistemler
- Gereksiz veri toplama ve agresif analitik

## 10. Başarı Kriterleri

Ürün başarılı sayılırsa kullanıcı:

1. Uygulamayı açtığında gününü anlayabilir.
2. Yeni görev veya rutin eklemeyi kolay bulur.
3. Bir görevi tamamladığında ilerlemesini hisseder.
4. Bir günü kaçırdığında uygulamaya geri dönmekten çekinmez.
5. Geçmiş ilerlemesini anlamlı biçimde görebilir.
6. Bildirimleri faydalı bulur ve kontrolün kendisinde olduğunu hisseder.

## 11. Geliştirme Kararları İçin Kısa Filtre

Yeni bir özellik veya tasarım kararı öncesinde şu sorular sorulmalıdır:

- Kullanıcı bugün ne yapacağını daha hızlı anlayacak mı?
- Bu değişiklik küçük adımı tamamlamayı kolaylaştırıyor mu?
- Kullanıcıya gerçek ilerlemesini gösteriyor mu?
- Dil ve davranış suçluluk oluşturuyor mu?
- Özellik mevcut offline-first yaklaşımı bozuyor mu?
- Bu özellik ürünün sadeliğini koruyor mu?

Bu soruların çoğuna olumlu cevap verilmiyorsa özellik ertelenmelidir.

## 12. Ürün Yönü

BenimGünlerim’in hedefi en fazla özelliğe sahip uygulama olmak değildir. Hedefi, kullanıcının her gün geri dönmek isteyeceği kadar anlaşılır, sakin ve faydalı bir yardımcı olmaktır.

Ürün cümlemiz:

> **Gününü küçük adımlarla düzenle, ilerlemeni fark et ve yarına daha hafif başla.**

