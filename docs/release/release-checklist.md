# Release Checklist — BenimGunlerim

Bu döküman, her Play Store release için tekrarlanacak adımları içerir.

## 1. Signing Setup (İlk Kez)

```powershell
# Keystore üret (bir kez yapılır, sonucu güvenli sakla)
keytool -genkey -v -keystore benimgunlerim-release.jks `
  -alias benimgunlerim `
  -keyalg RSA -keysize 2048 `
  -validity 10000
```

Ardından proje kökünde **keystore.properties** dosyası oluştur (repoya girmiyor):

```properties
storeFile=/path/to/benimgunlerim-release.jks
storePassword=<storePassword>
keyAlias=benimgunlerim
keyPassword=<keyPassword>
```

`app/build.gradle.kts` içindeki `signingConfigs.release` bloğuna bu dosyayı oku:

```kotlin
val keystoreProps = Properties().also { props ->
    val f = rootProject.file("keystore.properties")
    if (f.exists()) props.load(f.inputStream())
}
signingConfigs {
    create("release") {
        storeFile = file(keystoreProps["storeFile"] as String)
        storePassword = keystoreProps["storePassword"] as String
        keyAlias = keystoreProps["keyAlias"] as String
        keyPassword = keystoreProps["keyPassword"] as String
    }
}
```

> **CI:** `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`
> env var'larını GitHub Actions secret'a yükle ve build.gradle.kts'te bunları oku.

---

## 2. Her Release Öncesi Kontrol

- [ ] `versionCode` bir arttırıldı (`app/build.gradle.kts` → `versionCode`).
- [ ] `versionName` semantic: `MAJOR.MINOR.PATCH`.
- [ ] `CHANGELOG.md` veya `docs/release/YYYYMMDD.md` güncellendi.
- [ ] `git status` temiz (commit/tag yapılmadan önce).

## 3. Kalite Kapıları

```powershell
.\gradlew.bat testDebugUnitTest   # unit testler yeşil
.\gradlew.bat lintDebug           # lint yeşil
.\gradlew.bat lintRelease         # release lint yeşil
.\gradlew.bat assembleRelease     # APK üretildi
.\gradlew.bat bundleRelease       # AAB üretildi (Play Store)
```

APK yolu: `app/build/outputs/apk/release/app-release.apk`  
AAB yolu: `app/build/outputs/bundle/release/app-release.aab`

## 4. Smoke Test (Her Release)

Fiziksel cihaz veya emülatörde:

- [ ] Uygulama açılıyor (crash yok).
- [ ] Bugün ekranı yükleniyor.
- [ ] Görev ekle → tamamla akışı çalışıyor.
- [ ] Rutin ekle → rutinler sekmesi açılıyor.
- [ ] Bildirim izni verilince bildirim geliyor.
- [ ] Ayarlar ekranı açılıyor, sessiz saatler toggle çalışıyor.
- [ ] Uygulama kapat → yeniden aç → veri kayıp yok.

## 5. Play Store Yükleme

1. **Internal Testing**: AAB'yi Play Console → Internal Testing → Create release ekranına yükle.
2. Sürüm notlarını Türkçe gir.
3. İnceleme gerektirmez, test cihazlarına hemen push edilir.
4. Smoke test geçtikten sonra Production track'e tanıt.

## 6. Post-Release

- [ ] Git tag: `git tag -a v0.1.0 -m "Release 0.1.0" ; git push origin v0.1.0`
- [ ] `docs/release/YYYYMMDD.md` commit'lendi.
- [ ] Bir sonraki sprint için `versionCode`/`versionName` draft'ı yapıldı.

---

> **Güvenlik:** `keystore.properties` ve `.jks` dosyaları asla repoya commit edilmez.
> `.gitignore` içinde `*.jks` ve `keystore.properties` olduğunu doğrula.
