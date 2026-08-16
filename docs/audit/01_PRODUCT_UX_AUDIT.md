# 01 — Product & UX Audit Prompt

## Rol

Sen kıdemli bir mobil ürün tasarımcısı ve UX denetim uzmanısın. Benim Günlerim uygulamasını ürün akışı, kullanıcı yolculuğu, ekran hiyerarşisi, onboarding, sakinleşme özellikleri, oyunlaştırma ve genel kullanılabilirlik açısından acımasız ama hakkaniyetli şekilde değerlendir.

## İncelenecek ana alanlar

- Onboarding akışı
- Bugün ekranı
- Plan ekranı
- Rutinler ekranı
- İstatistik ekranı
- Ayarlar ekranı
- Global topbar ve bottom navigation
- FAB hızlı eylemler menüsü
- 1 Dakikalık Reset
- Hafif Gün Modu
- Kafam Dolu / Brain Dump
- Gün sonu kapatma
- XP / level / başarım / kutlama sistemi
- Empty state ve hata durumları
- Türkçe metin kalitesi

## Cevaplaman gereken sorular

1. Kullanıcı ilk açılışta ne yapacağını 3 saniyede anlıyor mu?
2. Onboarding gerçekten kişiselleştirme hissi veriyor mu?
3. Bugün ekranında hiyerarşi doğru mu: uyarı, ilerleme, görevler, rutinler, gün sonu?
4. Görev ve rutin ayrımı kullanıcı için net mi?
5. FAB menüsü anlaşılır mı, yoksa `+` anlamı bulanıklaşıyor mu?
6. Sakinleşme özellikleri ürünü güçlendiriyor mu, yoksa odağı dağıtıyor mu?
7. Hafif Gün Modu suçluluk azaltıyor mu, yoksa sadece banner gibi mi kalıyor?
8. Kafam Dolu akışı gerçekten zihin boşaltma hissi veriyor mu?
9. Gün sonu kapatma ritüel hissi veriyor mu?
10. XP/level/başarım sistemi motive edici mi, yoksa ucuz oyunlaştırma gibi mi?
11. Boş durumlar kullanıcıya ne yapacağını söylüyor mu?
12. Türkçe metinler doğal mı?
13. Alt navigasyon sırası doğru mu?
14. Topbar global yapı olarak tutarlı mı?
15. Para kazanma açısından kullanıcı güveni ve ürün değeri yeterli mi?

## Özellikle kontrol et

- Onboarding 4 adım kalmalı mı?
- `Atla` final ekranda gerekli mi?
- Rutin önerileri ilk seçime göre değişiyor mu?
- Bugün ekranında kart/list ayrımı doğru mu?
- Görevler ve rutinler flat list olarak yeterince okunur mu?
- Günü kapat ve FAB birbirine karışıyor mu?
- Sakinleşme araçları Today akışına doğal gömülmüş mü?
- Kullanıcı “başaramadım” değil “devam edebilirim” hissi alıyor mu?

## Rapor formatı

Master prompt formatını kullan. Ek olarak şu tabloyu ekle:

```md
## Ekran Puanları
| Ekran/Akış | Puan | En Büyük Sorun | En Büyük Güç |
|---|---:|---|---|
| Onboarding | | | |
| Bugün | | | |
| Plan | | | |
| Rutinler | | | |
| İstatistik | | | |
| Ayarlar | | | |
| Sakinleşme | | | |
| Oyunlaştırma | | | |
```
