# BTRooster Lite :alarm_clock:
## De open roosterapp met :zap: 

[![Licentie: GPL v3](https://img.shields.io/badge/License-GPL%20v3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Build Status](https://travis-ci.org/RutgerBroekhoff/btrooster-lite.svg?branch=master)](https://travis-ci.org/RutgerBroekhoff/btrooster-lite)
[![GitHub (pre-)release](https://img.shields.io/github/release/RutgerBroekhoff/btrooster-lite/all.svg)](https://github.com/RutgerBroekhoff/btrooster-lite/releases/latest)

<a href='https://play.google.com/store/apps/details?id=nl.viasalix.btroosterlite&pcampaignid=MKT-Other-global-all-co-prtnr-py-PartBadge-Mar2515-1'>
  <img alt='Ontdek het op Google Play' src='https://play.google.com/intl/en_us/badges/images/generic/nl_badge_web_generic.png' height='100'/>
</a>

BTRooster Lite is een roosterapp gemaakt voor het Calvijn College

### Bouwen :hammer: (debug APK)
Wil je BTRooster Lite zelf bouwen?

Zorg ervoor dat je de Android SDK op je computer hebt staan en je ook weet waar.
De app zal anders niet gebouwd kunnen worden.

Clone eerst de repository via Android Studio (IntelliJ) of via Git:

```bash
git clone https://github.com/RutgerBroekhoff/btrooster-lite
```

#### Optie 1
__De Android SDK zit al in Android Studio, deze hoef je dus niet apart te downloaden__

Open het project in Android Studio / IntelliJ en bouw het.

#### Optie 2
__Waarschijnlijk moet je het pad naar je Android SDK als environment variable instellen of in local.properties (sdk.dir=\<pad naar SDK>)__

Voer een van de volgende commando's uit:

Windows:
```
gradlew.bat assembleDebug
```

Linux:
```bash
./gradlew assembleDebug
```

Mac:
```sh
./gradlew assembleDebug
```

__Google Play en het logo van Google Play zijn handelsmerken van Google LLC.__
