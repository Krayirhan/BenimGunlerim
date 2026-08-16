# Benim Günlerim — Full Project Audit Prompt Pack

Bu paket, Benim Günlerim projesini ürün, frontend, state yönetimi, data/database, backend hazırlığı, güvenlik/gizlilik, performans, test/QA ve monetizasyon/release açısından acımasız ama hakkaniyetli şekilde değerlendirmek için hazırlanmıştır.

## Kullanım

1. Bu klasörü repoda `docs/benim_gunlerim_full_project_audit/` altına koy.
2. Önce `00_MASTER_AUDIT_PROMPT.md` dosyasını oku.
3. Ardından alan bazlı audit dosyalarını sırayla çalıştır.
4. İlk audit turunda kod değiştirtme; sadece rapor üret.
5. Çıkan raporları `docs/benim_gunlerim_full_project_audit/results/` altında sakla.
6. En sonda `10_FINAL_SCORECARD_TEMPLATE.md` ile birleşik proje skor kartı çıkar.

## Önerilen çalışma sırası

1. `03_STATE_VIEWMODEL_AUDIT.md`
2. `04_DATA_DATABASE_AUDIT.md`
3. `02_FRONTEND_COMPOSE_AUDIT.md`
4. `01_PRODUCT_UX_AUDIT.md`
5. `06_SECURITY_PRIVACY_AUDIT.md`
6. `07_PERFORMANCE_AUDIT.md`
7. `08_TESTING_QA_AUDIT.md`
8. `09_MONETIZATION_RELEASE_AUDIT.md`
9. `10_FINAL_SCORECARD_TEMPLATE.md`

## Ana kural

İlk turda amaç düzeltmek değil, gerçeği görmektir. Kod değiştirme, dosya silme, refactor yapma. Sadece kanıtlı bulgu, risk, puan ve uygulanabilir yapılacaklar listesi üret.
