# GitHub Branch/Tag Protection ve Secrets Checklist

## Branch Protection
- [ ] main/master branch için protection aktif mi?
- [ ] PR merge için required status checks (CI, lint, test, coverage) zorunlu mu?
- [ ] Force push ve direct push devre dışı mı?
- [ ] Dismiss stale reviews aktif mi?

## Tag Protection
- [ ] v* tag'leri için protection aktif mi?
- [ ] Sadece release pipeline veya yetkili kullanıcılar tag oluşturabiliyor mu?

## Secrets
- [ ] KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD gibi secrets tanımlı mı?
- [ ] Secret scanning aktif mi?
- [ ] Secrets sadece gerekli workflow'larda mı kullanılıyor?

## Otomasyon
- [ ] Protection ve secrets ayarları release öncesi manuel veya API ile doğrulanıyor mu?
- [ ] Play Console internal test için otomatik doğrulama ve kanıt akışı var mı?

> Bu dosya, dış sistem kanıtı ve güvenlik için release sürecinde gözden geçirilmelidir.
