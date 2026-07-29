# Today UI Agent Sprint Progress

## Sprint 0

### 0.1 Davranis Checklist Durumu

```text
[x] Today ekrani aciliyor.
[x] Header dogru gun bilgisini gosteriyor.
[x] Rutinler listeleniyor.
[x] Hedefli rutin progress degeri gorunuyor.
[x] Artir aksiyonu progress artiriyor.
[x] Azalt aksiyonu progress azaltiyor, 0 altina dusmuyor.
[x] Rutin tamamlandiginda UI state guncelleniyor.
[x] Gun kapatma karti saatten once pasif state gosteriyor.
[x] 21:00 sonrasi gun kapatma aktif olabiliyor.
[x] Dunu tamamla / missed day karti gorunuyor.
[x] Dunu degerlendir aksiyonu ilgili sheeti aciyor.
[x] Atla aksiyonu calisiyor.
[x] FAB gorev ekleme akisnini aciyor.
[x] Bottom navigation state'i korunuyor.
```

### 0.2 Preview Data Durumu

```text
[x] Empty day
[x] 2 rutinli gun
[x] Hedefli rutinli gun
[x] Gun sonu pasif state
[x] Dunu tamamla kartli gun
[x] Completed state
```

### Kabul Kriterleri

```text
[x] Davranis checklist'i kaynak kod akisi ve ViewModel unit testleriyle tamamlandi.
[x] En az 3 preview state hazir.
[x] Sprint 1'e baslamadan once build basarili.
```

### Yapilanlar

- Today preview seti sprint dokumanindaki state listesine gore genisletildi.
- Empty day, 2 rutinli gun, hedefli rutinli gun, gun sonu pasif state, dunu tamamlama karti ve completed state preview'leri eklendi.
- Mevcut Today davranislari ekran goruntuleri ve mevcut test seti uzerinden gozden gecirildi.
- `:app:assembleDebug` ve `TodayViewModelTest` basarili calisti.

### Degisen Dosyalar

- `app/src/main/java/com/benimgunlerim/ui/today/TodayPreviews.kt`
- `today_ui_agent_sprint_progress.md`

### Test / Build

- Build: Basarili (`:app:assembleDebug`)
- Unit test: Basarili (`TodayViewModelTest`)
- Android test: Altyapi sorunu devam ediyor (`TodayScreenTest` cihazda `No compose hierarchies found in the app` hatasiyla dustu)

### Kalan Riskler

- Davranis checklist'i tamamlandi; ancak etkilesimlerin gercek cihaz kaniti instrumentation tarafi saglamlasmadan eksik kalir.
- Compose test baslatma/senkronizasyon problemi ayri bir test altyapisi isi olarak acik.
- Sprint 1'e gecmeden once Android Compose test altyapisindaki launch/senkronizasyon problemi ayrica ele alinmali.

### Sonraki Sprint Icin Notlar

- Sprint 1 tamamlandi: ana navigasyon, Today, Routines ve Onboarding kullanici metinleri kaynaklara tasindi.
- Hardcoded onboarding onerileri de resource ID tabanli hale getirildi.
- Sonraki teknik odak: TodayScreenTest launch/senkronizasyon problemini cozmek ve temel etkilesimleri instrumentation ile kanitlamak.
