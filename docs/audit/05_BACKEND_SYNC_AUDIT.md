# 05 — Backend / Sync Readiness Audit Prompt

## Rol

Sen local-first mobil uygulamalar, backend hazırlığı, sync, auth ve premium entitlement mimarisi konusunda deneyimli bir ürün/mimari denetim uzmanısın. Benim Günlerim uygulamasında şu an backend olmasa bile gelecekte sync, hesap, yedekleme ve para kazanma için ne kadar hazır olduğunu değerlendir.

## Odak alanları

- Şu an backend var mı?
- Local-first mimari durumu
- Kullanıcı hesabı eklenebilirliği
- Cloud sync hazırlığı
- Backup/export/import ihtiyacı
- Conflict resolution riski
- Premium entitlement modeli
- Subscription/IAP hazırlığı
- Multi-device kullanım
- Veri şeması versiyonlama

## Cevaplaman gereken sorular

1. Uygulama bilinçli bir local-first mimariyle mi ilerliyor?
2. Backend yoksa bu bir avantaj mı, risk mi?
3. İleride kullanıcı hesabı eklendiğinde data modeli buna hazır mı?
4. Task, Routine, completion, preference kayıtları sync için uygun mu?
5. Conflict resolution nasıl yapılmalı?
6. Offline-first yaklaşım korunabilir mi?
7. Premium özellikler için entitlement nerede tutulmalı?
8. Kullanıcı cihaz değiştirirse verisini taşıyabilecek mi?
9. Export/import veya Google Drive backup gibi düşük maliyetli ara çözümler mümkün mü?
10. Şu an backend eklemek gereksiz şişirme mi?

## Backend yoksa değerlendir

Backend yoksa “eksik” diye doğrudan puan kırma. Şu başlıklarla değerlendir:

- MVP için local-only yeterli mi?
- Para kazanma öncesi minimum hangi backend gereklidir?
- Premium abonelik için Google Play Billing yeter mi?
- Sync olmadan kullanıcı güveni nasıl sağlanır?

## Rapor formatına ek tablo

```md
## Backend Readiness
| Alan | Hazır mı? | Risk | Öneri |
|---|---|---|---|
```
