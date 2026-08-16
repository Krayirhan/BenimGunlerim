# 04 — Data / Database Audit Prompt

## Rol

Sen kıdemli bir Android data layer, Room/DataStore ve local-first uygulama mimarisi uzmanısın. Benim Günlerim projesindeki repository, entity, DAO, DataStore, migration, local date/time ve veri tutarlılığı yapısını denetle.

## Odak alanları

- Repository yapısı
- Room entity/DAO varsa bunlar
- DataStore kullanımı
- UserPreferencesRepository
- Task/Routine modelleri
- Günlük tamamlanma kayıtları
- Streak/seri hesapları
- XP/level/achievement veri modeli hazırlığı
- Hafif Gün Modu için `lightDayModeDate`
- Brain Dump ile eklenen görevlerin defaultları
- Migration stratejisi
- Date/time/local timezone kullanımı
- Silme / soft delete / archive stratejisi

## Cevaplaman gereken sorular

1. Task ve Routine ayrımı data modelde net mi?
2. Görevler tarih bazlı güvenli tutuluyor mu?
3. Rutin tamamlanmaları günlük kayıt olarak mı tutuluyor, yoksa entity üstünde kırılgan alanlar mı var?
4. Streak hesapları güvenilir mi?
5. Kullanıcı timezone değiştirirse tarih mantığı bozulur mu?
6. Hafif Gün Modu her gün doğru sıfırlanır mı?
7. Brain Dump ile eklenen görevler doğru tarih, kategori, priority, completed=false defaultlarıyla mı ekleniyor?
8. Toplu görev ekleme transaction içinde mi?
9. Silinen görevler kalıcı siliniyor mu; undo veya soft delete gerekiyor mu?
10. Migration stratejisi var mı?
11. DataStore ne için, Room ne için kullanılmış; sorumluluklar doğru mu?
12. XP/level/achievement sistemi eklenirse data modeli hazır mı?
13. Local backup/export/import için yapı uygun mu?

## Özellikle kontrol et

- Date string mi, epoch mu, LocalDate mi kullanılıyor?
- DAO queryleri günü doğru filtreliyor mu?
- Tamamlanma kayıtları tarih bazlı mı?
- Task ve Routine ID ilişkileri sağlam mı?
- UserPreferencesRepository kişisel ayarlar için uygun mu?

## Rapor formatına ek tablo

```md
## Data Model Riskleri
| Model/Tablo/Ayar | Risk | Etki | Öneri |
|---|---|---|---|
```
