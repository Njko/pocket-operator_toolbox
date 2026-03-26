# Pocket Operator Toolbox

Application desktop pour creer, editer et jouer des patterns pour les Pocket Operators de Teenage Engineering.

[![Build & Test](https://github.com/Njko/pocket-operator_toolbox/actions/workflows/build.yml/badge.svg)](https://github.com/Njko/pocket-operator_toolbox/actions/workflows/build.yml)

## Fonctionnalites

### Gestion de patterns
- Creation et edition de patterns 16 steps avec grille interactive
- Support de tous les Pocket Operators (PO-12 a PO-35)
- Patterns multi-mesures (1 a 16 bars) avec chainage
- Templates integres (Four-on-the-Floor, Rock, Breakbeat, Hip-Hop, Techno)
- Stockage en markdown lisible (compatible GitHub)

### Lecture MIDI en temps reel
- Play/Stop/Loop directement dans l'application
- Synthetiseur GM integre (aucun logiciel externe requis)
- Animation LED style PO-12 : les points actifs pulsent au rythme de la lecture
- Noms d'instruments qui s'illuminent quand leur step est joue
- Indicateur de mesure courante pour le playback multi-mesures

### Accessibilite
- 3 themes : Sombre, Clair, Contraste eleve (WCAG AAA)
- 3 modes daltonisme : Deuteranopie, Protanopie, Tritanopie
- Taille de police ajustable (80% a 200%)
- Reduction des animations
- Navigation clavier complete (Ctrl+N, Ctrl+E, Delete, F5, Space...)
- Focus visible sur tous les elements interactifs
- Texte accessible sur chaque step et instrument

### Export
- MIDI (.mid) pour DAWs
- JSON pour integration programmatique
- CSV (liste et grille) pour tableurs
- Markdown avec instructions de programmation PO-12

### Analyse
- Recherche de patterns similaires (voix, steps, rythme)
- Statistiques (densite, complexite, syncopation)
- Validation avec erreurs et avertissements

## Installation

### Executable natif (recommande)

Telecharger la derniere release depuis [GitHub Releases](https://github.com/Njko/pocket-operator_toolbox/releases) :

| Plateforme | Fichier |
|------------|---------|
| Windows x64 | `PO-Toolbox-windows-x64.zip` |
| Linux x64 | `PO-Toolbox-linux-x64.tar.gz` |
| macOS ARM64 | `PO-Toolbox-macos-arm64.tar.gz` |

Decompresser et lancer — aucune installation de Java requise.

**Windows :** decompresser le zip, puis lancer `PO-Toolbox\PO-Toolbox.exe`

**Linux :**
```bash
tar xzf PO-Toolbox-linux-x64.tar.gz
./PO-Toolbox/bin/PO-Toolbox
```

**macOS :**
```bash
tar xzf PO-Toolbox-macos-arm64.tar.gz
xattr -cr PO-Toolbox.app
open PO-Toolbox.app
```

### Depuis les sources

```bash
git clone https://github.com/Njko/pocket-operator_toolbox.git
cd pocket-operator_toolbox

# Builder et lancer (necessite Java 25)
./gradlew shadowJar
java --add-opens=java.base/java.lang=ALL-UNNAMED \
     --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
     --enable-native-access=ALL-UNNAMED \
     -jar build/libs/po-toolbox-1.0.0-win.jar

# Ou builder l'executable natif
./gradlew jpackage
./build/package/PO-Toolbox/PO-Toolbox.exe  # Windows
```

## Raccourcis clavier

| Action | Raccourci |
|--------|-----------|
| Nouveau pattern | Ctrl+N |
| Editer | Ctrl+E |
| Generer depuis template | Ctrl+G |
| Supprimer | Delete |
| Actualiser | F5 |
| Play / Stop | Bouton Play |
| Parametres accessibilite | Ctrl+Shift+A |

## Pocket Operators supportes

| Modele | Nom | Type |
|--------|-----|------|
| PO-12 | Rhythm | Drum machine |
| PO-14 | Sub | Synthe basse |
| PO-16 | Factory | Synthe lead |
| PO-20 | Arcade | Chiptune |
| PO-24 | Office | Noise/Percussion |
| PO-28 | Robot | Synthe 8-bit |
| PO-32 | Tonic | Drum synth |
| PO-33 | K.O! | Sampler |
| PO-35 | Speak | Synthe vocal |

Les guides officiels sont accessibles depuis le menu Aide > Guides Pocket Operator.

## Developpement

### Prerequis
- Java 25 (Amazon Corretto recommande)
- Gradle 9.4 (wrapper inclus)

### Build & Test
```bash
./gradlew test              # Lancer les tests
./gradlew jacocoTestReport  # Rapport de couverture
./gradlew shadowJar         # Fat JAR
./gradlew jpackage          # Executable natif
```

### Stack technique
- **Kotlin 2.3** + **Java 25**
- **JavaFX 24** + **TornadoFX** — Interface desktop
- **javax.sound.midi** — Lecture et export MIDI
- **JUnit 5** + **MockK** — Tests
- **JaCoCo** — Couverture de code (seuil 80%)
- **Shadow JAR** — Fat JAR avec dependances
- **jpackage** — Executables natifs (Windows/Linux/macOS)

### Methodologie
Toutes les fonctionnalites sont developpees en **TDD strict** (Red/Green/Refactor) avec revue de code par des agents specialises.

## Documentation

- [CLAUDE.md](CLAUDE.md) — Guide de developpement detaille
- [docs/TODO.md](docs/TODO.md) — Roadmap et taches en cours
- [Guides Pocket Operator](https://teenage.engineering/products/po) — Documentation officielle

## Auteur

**Nicolas Linard**

## Licence

[A definir]
