# BenimGunlerim

BenimGunlerim, kullanıcının gününü küçük görevler, basit rutinler ve görünür ilerleme hissiyle yönetmesine yardımcı olan offline-first Android uygulamasıdır.

## Başlangıç

Bu repo, `BenimGunlerim_Proje_Detay_Raporu.md` ve `BenimGunlerim_Sprint_Plani.md` dosyaları referans alınarak başlatıldı.

İlk teknik kapsam:

* Kotlin
* Jetpack Compose
* Room
* DataStore
* Hilt
* Navigation Compose
* Offline-first yerel veri temeli

## Derleme

JDK 17 ve Android SDK gereklidir.

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'
$env:ANDROID_HOME='C:\Users\Acer\AppData\Local\Android\Sdk'
.\gradlew.bat assembleDebug
```

## Mevcut Durum

Çalışan başlangıç iskeleti:

* Onboarding
* Şablon seed
* Bugün ekranı
* Görev ekleme ve tamamlama
* Basit rutin ekleme ve tamamlama
* CompletionLog
* Günlük progress
* Akşam özeti kaydı
* İlerlemen ekranı
* Ayarlar başlangıç ekranı

## Sonraki Sprint

Sprint planına göre sıradaki öncelik, mevcut temel akışı daha sağlam hale getirip rutin düzenleme, bildirim ayarları ve test katmanını genişletmektir.
