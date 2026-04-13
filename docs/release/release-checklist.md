# Release Checklist - BenimGünlerim

Bu checklist her Play Store release'i için tekrarlanır. Tüm maddeler tamamlanmadan production track'e geçilmez.

## 1. Signing Kurulumu

Keystore bir kez üretilir ve güvenli bir yerde saklanır:

```powershell
keytool -genkey -v -keystore benimgunlerim-release.jks `
  -alias benimgunlerim `
  -keyalg RSA -keysize 2048 `
  -validity 10000
```

Lokal release için proje kökünde `keystore.properties` oluşturulur. Bu dosya repoya girmez.

```properties
storeFile=/path/to/benimgunlerim-release.jks
storePassword=<storePassword>
keyAlias=benimgunlerim
keyPassword=<keyPassword>
```

CI release için GitHub Actions secret'ları:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

`KEYSTORE_BASE64` üretmek için:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("benimgunlerim-release.jks"))
```

## 2. Versioning

- [ ] `versionCode` artırıldı.
- [ ] `versionName` semantic versioning ile güncellendi.
- [ ] Release notu hazırlandı.
- [ ] `git status` temiz.

## 3. Zorunlu Kalite Kapısı

```powershell
.\scripts\check-release.ps1
```

Bu komut şunları doğrular:

- Release signing yapılandırılmış.
- Unit testler geçiyor.
- Release lint geçiyor.
- Release APK build ediliyor.
- Release AAB build ediliyor.

`verifyReleaseSigning` başarısızsa release durur.

## 4. Cihaz Smoke Test

Fiziksel cihaz veya emülatörde:

- [ ] Uygulama açılıyor, crash yok.
- [ ] Onboarding tamamlanıyor.
- [ ] Bugün ekranı yükleniyor.
- [ ] Görev ekleme, düzenleme, tamamlama ve silme çalışıyor.
- [ ] Alt görev ekleme ve tamamlama çalışıyor.
- [ ] Rutin ekleme, düzenleme ve arşivleme çalışıyor.
- [ ] Bildirim izni verilince görev bildirimi geliyor.
- [ ] Sessiz saatlerde bildirim gösterilmiyor.
- [ ] Cihaz yeniden başlatıldıktan sonra task/routine/daily reminder'lar tekrar planlanıyor.
- [ ] Ayarlar ekranı ve tema seçimi çalışıyor.
- [ ] Uygulama kapatılıp açıldığında veri kaybı yok.

## 5. Backup ve Gizlilik Kontrolü

- [ ] `android:allowBackup` kararı ürün ve gizlilik politikasıyla uyumlu.
- [ ] Kullanıcıya görev, rutin ve günlük verilerinin yedekleme davranışı açıklanıyor.
- [ ] Export edilen JSON dosyasının kişisel içerik taşıdığı kullanıcıya bildiriliyor.
- [ ] JSON import/restore test verisiyle doğrulandı.
- [ ] Veri silme akışı confirmation ile korunuyor.

## 6. Play Store Akışı

1. Signed AAB dosyasını Internal Testing track'e yükle.
2. Türkçe release notlarını gir.
3. Internal tester cihazında smoke test çalıştır.
4. Crash/ANR sinyallerini kontrol et.
5. Firebase Crashlytics içinde test non-fatal event'in düştüğünü doğrula.
6. Firebase Crashlytics içinde test crash event'inin düştüğünü doğrula.
7. Play Console Android Vitals içinden ANR rate'i kontrol et.
8. Crash-free users oranının ilk 24 saatte %99.5 üstünde kaldığını doğrula.
9. Aynı startup/import/data-loss stack trace'i 3+ kullanıcı etkiliyorsa rollout pause et.
10. Sorun yoksa staged rollout ile production'a ilerle.

## 7. Post-Release

- [ ] Git tag oluşturuldu: `vX.Y.Z`.
- [ ] Release notu commit'lendi.
- [ ] Play Console vitals kontrol edildi.
- [ ] Crash-free users / sessions ilk 24 saatte takip edildi.
- [ ] ANR rate bad behavior threshold altında kaldı.
- [ ] 24/48 saat release health sorumlusu atandı.
- [ ] Bir sonraki release için version planı açıldı.

## 8. Güvenlik Kuralı

`keystore.properties`, `.jks` ve secret değerleri repoya asla commit edilmez. `.gitignore` içinde bu dosyalar korunmalıdır.

## 9. Supply Chain ve Repo Kontrolleri

- [ ] GitHub secret scanning veya eşdeğer secret scan job'ı yeşil.
- [ ] Dependency review job'ı pull request üzerinde yeşil.
- [ ] Dependabot veya eşdeğer dependency update otomasyonu aktif.
- [ ] `master` ve release tag'leri için required checks tanımlı.
- [ ] Direct push ve force-push politikası repo ayarlarında doğrulandı.
- [ ] Release workflow artifact'leri incelendi: unit test, coverage, lint, connected test, logcat.
