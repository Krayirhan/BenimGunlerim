# 11 — Agent Run Order

Bu dosya, audit promptlarının hangi sırayla ve nasıl çalıştırılacağını belirtir.

## Genel çalışma kuralı

İlk turda hiçbir ajan kod değiştirmeyecek. Sadece rapor üretecek.

## Sıralı audit planı

### 1. State / ViewModel Audit

Dosya: `03_STATE_VIEWMODEL_AUDIT.md`

Neden önce bu? Çünkü uygulama büyüdükçe en büyük kırılma riski state karmaşasıdır.

Çıktı:

`results/03_state_viewmodel_report.md`

### 2. Data / Database Audit

Dosya: `04_DATA_DATABASE_AUDIT.md`

Neden ikinci? Görev, rutin, streak, light day, brain dump, XP ve achievement mantığı data modelin üstünde durur.

Çıktı:

`results/04_data_database_report.md`

### 3. Frontend / Compose Audit

Dosya: `02_FRONTEND_COMPOSE_AUDIT.md`

Neden üçüncü? Tasarım iyi görünse bile component yapısı sürdürülebilir değilse uzun vadede hız düşer.

Çıktı:

`results/02_frontend_compose_report.md`

### 4. Product / UX Audit

Dosya: `01_PRODUCT_UX_AUDIT.md`

Neden dördüncü? Görsel ve ürün akışı artık belli bir seviyede; teknik gerçeklik görüldükten sonra ürün kararı daha sağlıklı verilir.

Çıktı:

`results/01_product_ux_report.md`

### 5. Security / Privacy Audit

Dosya: `06_SECURITY_PRIVACY_AUDIT.md`

Neden önemli? Brain Dump ve sakinleşme özellikleri kişisel veri ve güvenli ürün dili riski doğurur.

Çıktı:

`results/06_security_privacy_report.md`

### 6. Performance Audit

Dosya: `07_PERFORMANCE_AUDIT.md`

Çıktı:

`results/07_performance_report.md`

### 7. Testing / QA Audit

Dosya: `08_TESTING_QA_AUDIT.md`

Çıktı:

`results/08_testing_qa_report.md`

### 8. Monetization / Release Audit

Dosya: `09_MONETIZATION_RELEASE_AUDIT.md`

Çıktı:

`results/09_monetization_release_report.md`

### 9. Final Scorecard

Dosya: `10_FINAL_SCORECARD_TEMPLATE.md`

Tüm raporlar okunduktan sonra final skor kartı çıkar.

Çıktı:

`results/10_final_scorecard.md`

## Agent’a verilecek ortak kısa komut

```text
Bu repoyu, docs/benim_gunlerim_full_project_audit/00_MASTER_AUDIT_PROMPT.md kurallarına uyarak ve ilgili alan promptunu esas alarak incele. İlk turda kod değiştirme. Sadece kanıtlı, dosya bazlı, puanlı ve önceliklendirilmiş bir audit raporu üret. Raporu docs/benim_gunlerim_full_project_audit/results/ altına kaydet.
```
