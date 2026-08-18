# Room Migration Matrisi

Bu belge, `AppDatabase` için yayın ve pre-release şema politikasını açıklar.

| Kaynak sürüm | Hedef sürüm | Durum | Politika | Kanıt |
|---:|---:|---|---|---|
| 1-5 | 7 | Desteklenmeyen pre-release aralık | Gerçek yayın olmadığı için yapay migration yazılmaz; eski geliştirme verisi destructive fallback ile temizlenebilir | `Migrations.kt`, `AppModule.kt` |
| 6 | 7 | Destekleniyor | Subtask tablosu, yeni kolonlar ve index’ler eklenir; kullanıcı verisi korunur | `MIGRATION_6_7`, `AppDatabaseMigrationTest` |
| 7 | 8+ | Kural | Her schema değişikliği için ayrı migration, schema JSON ve instrumentation testi zorunludur | `Migrations.kt` politika bölümü |

## Release Kuralı

- İlk gerçek Play track yayını v7 şemasıyla yapılır.
- v7 sonrası hiçbir sürüm `fallbackToDestructiveMigration` kapsamına eklenmez.
- Her release PR’ı migration SQL’i, `app/schemas` çıktısı ve upgrade testini birlikte taşır.
- v1-v5 cihaz verisi yalnızca geliştirme/test verisi kabul edilir; v6 verisi migration ile korunur.
- Aynı kaynak sürüm hem migration hem destructive fallback listesinde bulunamaz; Room startup tutarlılığı bu nedenle CI/device testinde doğrulanır.
