# Gizlilik Politikası Taslağı

Bu metin BenimGünlerim için kullanıcıya açık gizlilik politikasının teknik taslağıdır. Play Store yayını öncesinde ürün, hukuk ve mağaza metinleriyle uyumlu son sürüme dönüştürülmelidir.

## Toplanan Veriler

BenimGünlerim offline-first çalışır. Görevler, rutinler, alt görevler, günlük kapanış notları, ruh hali, ilerleme kayıtları ve uygulama tercihleri cihazda saklanır.

Uygulama varsayılan olarak bu kişisel içerikleri kendi sunucusuna göndermez.

## Yerel Saklama

Aşağıdaki veriler cihazda yerel olarak tutulur:

- Görev başlıkları ve notları
- Rutin adları ve rutin hedefleri
- Alt görevler
- Tamamlama kayıtları
- Günlük değerlendirme ve özet alanları
- Bildirim, tema ve uygulama tercihleri
- Oyunlaştırma ilerlemesi

## Yedekleme

Android sistem yedeklemesi açıksa uygulama verileri Google/Android yedekleme mekanizması kapsamında bulut yedeklemeye veya cihazdan cihaza aktarıma dahil olabilir.

Yedekleme kapsamı:

- Room veritabanı
- DataStore kullanıcı tercihleri

Geçici cache verileri ve yeniden oluşturulabilir bildirim policy cache'i yedeklenmez.

## Dışa Aktarma ve Geri Yükleme

Kullanıcı Ayarlar ekranından verilerini JSON dosyası olarak dışa aktarabilir. Bu JSON dosyası kullanıcı tarafından girilen kişisel içerikleri içerebilir. Dosyanın paylaşımı ve saklanması kullanıcının kontrolündedir.

Kullanıcı bir JSON yedeğini geri yüklediğinde mevcut yerel görev, rutin, ilerleme ve tercih verileri yedek içeriğiyle değiştirilir.

## Analitik

Analitik ayarı açıksa uygulama kullanım alanlarının anonim ölçümü yapılabilir. Görev başlığı, rutin adı, not, günlük özet metni gibi kullanıcı tarafından yazılan içerikler analitik event olarak gönderilmemelidir.

Analitik ayarı kapatıldığında temel uygulama özellikleri çalışmaya devam eder.

## Hata Raporlama

Uygulama non-fatal hataları sınırlı yerel tanı kaydı olarak saklayabilir. Hata context alanları kişisel serbest metin içermemelidir.

## İzinler

Uygulama şu Android izinlerini kullanır:

- Bildirim göndermek için `POST_NOTIFICATIONS`
- Cihaz yeniden başlatıldıktan sonra hatırlatmaları tekrar planlamak için `RECEIVE_BOOT_COMPLETED`
- İsteğe bağlı haptik geri bildirim için `VIBRATE`

## Veri Silme

Kullanıcı Ayarlar ekranından yerel verilerini temizleyebilir. Bu işlem cihazdaki görev, rutin, ilerleme kayıtları ve onboarding durumunu siler.

## İletişim

Production yayını öncesinde destek e-posta adresi ve veri sorumlusu bilgisi bu bölüme eklenmelidir.
