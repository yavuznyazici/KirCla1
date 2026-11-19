# KirCla

Ekranı tam kırmızı yapan ve parlaklığı "nefes alır gibi" yükseltip alçaltan basit bir Android uygulaması.

## Davranış

Uygulama açılır açılmaz ekran komple kırmızı olur ve şu döngü sürekli tekrarlanır:

| Aşama | Süre (varsayılan) |
|---|---|
| %20 → %100 parlaklık | 15 sn |
| %100'de bekleme | 4 sn |
| %100 → %20 parlaklık | 15 sn |
| Döngü başa döner | — |

Ekrana **çift dokununca** ayarlar açılır. Ayarlardan geri dönünce döngü baştan (%20'den) başlar.

## Ayarlar

- **Ekran rengi** — kırmızı (varsayılan), turuncu, kehribar, yeşil, camgöbeği, mavi, eflatun, beyaz
- **Parlaklık değişim süresi** — her bir yükselme/alçalma geçişinin süresi (3–60 sn, varsayılan 15)
- **Dalga şekli** — yumuşak (sinüs, nefes alır gibi) veya doğrusal
- **Gerçek ekran parlaklığı** — açıkken telefonun donanım parlaklığı döngüyle değişir (renk tam kalır); kapalıyken yalnızca renk koyulaşıp açılır

Uygulama açıkken ekran kapanmaz. Uygulamadan çıkınca telefonun kendi parlaklık ayarı geri gelir. Ek izin gerekmez.

## Derleme

**Android Studio:** klasörü açın, Gradle senkronizasyonu bitince çalıştırın (minSdk 26, Android 8.0+).

**GitHub Actions:** repoya push edince `.github/workflows/build.yml` debug APK üretir; Actions sekmesinde *Artifacts* altından indirebilirsiniz.

**Komut satırı:** Gradle 8.9 ve JDK 17 kuruluysa `gradle assembleDebug`. (Gradle wrapper betikleri dahil değil; istenirse `gradle wrapper` ile üretilebilir.)

## Yapı

```
app/src/main/java/com/kircla/app/
  MainActivity.kt      # tam ekran ışık + parlaklık döngüsü + çift dokunma
  SettingsActivity.kt  # ayarlar ekranı
  Prefs.kt             # ayar anahtarları ve sabitler (min/max seviye, bekleme süresi)
app/src/main/res/xml/preferences.xml   # ayar menüsü
```

Bekleme süresi (4 sn) ve alt/üst seviyeler (%20/%100) `Prefs.kt` içindeki sabitlerden değiştirilebilir.
