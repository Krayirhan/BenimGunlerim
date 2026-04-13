# Gizlilik ve Yedekleme Politikası

Bu doküman uygulamanın production davranışını teknik ekip için netleştirir.

## Yerel Veri

BenimGünlerim offline-first çalışır. Görevler, rutinler, tamamlama kayıtları, günlük özetleri ve kullanıcı tercihleri cihazda saklanır.

## Android Backup Davranışı

Mevcut manifest ayarı `android:allowBackup="true"` olduğu için Android yedekleme kapsamı açıktır.

Yedeklenen veriler:

- DataStore: ayarlar, tema, bildirim tercihleri, oyun/ilerleme durumu
- Room database: görevler, rutinler, completion log kayıtları, günlük durumlar

Yedeklenmeyen veriler:

- `reminder_policy_cache`
- Cache/temp dosyaları

## Production Gereksinimleri

- Kullanıcıya backup davranışı privacy policy içinde açıklanmalıdır.
- Export edilen JSON dosyasının kullanıcı tarafından girilen kişisel içerik taşıyabileceği belirtilmelidir.
- Error reporting context alanları kullanıcı notu, görev başlığı veya serbest metin taşımamalıdır.
- Analytics kullanıcı iznine bağlı kalmalıdır.

## Değişiklik Kararı

Eğer görev/rutin/günlük verilerinin cloud backup'a girmesi istenmiyorsa şu dosyalar güncellenmelidir:

- `app/src/main/AndroidManifest.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/main/res/xml/full_backup_content.xml`

Bu karar değişirse release checklist ve kullanıcıya açık gizlilik metni de aynı release içinde güncellenmelidir.
