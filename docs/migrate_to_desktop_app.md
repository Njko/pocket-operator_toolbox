# Migration CLI → Application Desktop (TornadoFX)

## Contexte

Le mode ligne de commande (CLI) montre ses limites pour une utilisation quotidienne :
- Navigation dans les grilles de steps peu ergonomique en terminal
- Pas de retour visuel immédiat sur les patterns
- Dépendance à JLine3 pour les flèches clavier (problèmes cross-platform)

L'objectif est de créer une application desktop JavaFX/TornadoFX qui réutilise **tout le code métier existant** et remplace uniquement la couche UI CLI.

---

## Ce qui est réutilisé tel quel (ne pas toucher)

| Package | Contenu |
|---------|---------|
| `models/` | `PO12Pattern`, `PO12DrumVoice`, `PatternMetadata`, `PatternTemplate`, `PatternEditHistory` |
| `io/` | `MarkdownParser`, `MarkdownWriter`, `MidiExporter`, `JsonExporter`, `CsvExporter` |
| `analysis/` | `PatternSimilarity`, `PatternStatistics` |
| `validation/` | `PatternValidator` |
| `utils/` | `VoiceCopyUtility` |
| `ocr/` | `OcrEngine`, `NotationParser`, `InstrumentMapper` |

---

## Ce qui est remplacé / ignoré

| Remplacé | Par |
|----------|-----|
| `commands/*.kt` (Clikt) | Controllers TornadoFX |
| `ui/GridEditor.kt` (terminal) | Grille cliquable JavaFX |
| `ui/JLine3KeyboardReader.kt` | Événements clavier JavaFX natifs |
| `ui/MultiVoiceRenderer.kt` (Mordant) | Vue JavaFX dédiée |
| Dépendances Clikt, Mordant, JLine3 | TornadoFX + JavaFX |

---

## Stack technique

- **Kotlin** (identique, même version)
- **JavaFX 21** — dépendances Maven directes (classifier `linux-aarch64` pour Raspberry Pi)
- **TornadoFX 1.7.20** — DSL Kotlin pour JavaFX
- **Gradle** — même fichier `build.gradle.kts`, ajout du task `runDesktop`

### Prérequis plateforme

- Java 21+ (testé Java 25 Corretto aarch64)
- Affichage graphique disponible (X11 ou Wayland)
- **Ne pas utiliser le plugin `org.openjfx.javafxplugin`** : incompatible Java 25. Utiliser les dépendances Maven directes.

---

## Architecture desktop cible

```
src/main/kotlin/.../desktop/
├── POToolboxApp.kt       ← Point d'entrée TornadoFX (App + main())
├── MainView.kt           ← Fenêtre principale (BorderPane + SplitPane)
├── PatternListView.kt    ← Panneau gauche : tableau des patterns
├── PatternDetailView.kt  ← Panneau droit : grille + métadonnées
└── PatternController.kt  ← Logique : charge patterns via MarkdownParser
```

### Layout principal

```
┌─────────────────────────────────────────────────────┐
│  Menu : Fichier                                      │
├──────────────────┬──────────────────────────────────┤
│  Liste patterns  │  Détail du pattern sélectionné    │
│                  │                                    │
│  Tableau :       │  Titre, BPM, genre, difficulté    │
│  - Nom           │                                    │
│  - #             │  Grille 16 steps par voix :        │
│  - BPM           │  Kick     1 · · · 5 · · · 9 · · ·│
│  - Difficulté    │  Snare    · · · · 5 · · · · · · · │
│  - Genre         │  ...                               │
│  - Voix          │                                    │
│                  │  Instructions PO-12                │
└──────────────────┴──────────────────────────────────┘
```

---

## Phases de migration

### Desktop Phase 1 — Lecture seule ✅ En cours

**Objectif :** Ouvrir une fenêtre, lister les patterns, afficher le détail.

Fichiers créés :
- `desktop/POToolboxApp.kt`
- `desktop/MainView.kt`
- `desktop/PatternListView.kt`
- `desktop/PatternDetailView.kt`
- `desktop/PatternController.kt`

Lancer avec :
```bash
./gradlew runDesktop
```

Statut actuel : build en cours de résolution (problème dépendances JavaFX aarch64).

---

### Desktop Phase 2 — Création de pattern

**Objectif :** Créer un nouveau pattern depuis l'interface.

- Formulaire de métadonnées (nom, BPM, genre, difficulté)
- Grille 16 steps cliquable pour chaque voix
- Sélection des voix à programmer
- Sauvegarde via `MarkdownWriter` existant
- Chargement depuis un template (`BuiltInTemplates`)

---

### Desktop Phase 3 — Édition de pattern

**Objectif :** Modifier un pattern existant.

- Grille éditable en place (cliquer sur un step pour le toggle)
- Undo/Redo via `PatternEditHistory` existant (Ctrl+Z / Ctrl+Y)
- Ajout / suppression de voix
- Sauvegarde inline (overwrite ou Save As)

---

### Desktop Phase 4 — Fonctionnalités avancées

**Objectif :** Exposer les fonctionnalités avancées existantes via l'UI.

- Export MIDI / JSON / CSV depuis un bouton
- Validation avec affichage des erreurs et warnings
- Statistiques du pattern (densité, polyrhythmie, etc.)
- Recherche de patterns similaires
- Gestion du chaînage de patterns

---

### Desktop Phase 5 — Polish

**Objectif :** UX finale.

- Thème sombre
- Raccourcis clavier globaux
- Drag & drop pour réordonner les voix
- Prévisualisation audio (si intégration future)
- Packaging en application native (jpackage)

---

## Commandes utiles

```bash
# Compiler uniquement
./gradlew compileKotlin

# Lancer l'application desktop
./gradlew runDesktop

# Lancer le CLI (inchangé)
./gradlew run --args="list"

# Tests (inchangés)
./gradlew test
```

---

## Points d'attention

1. **Java 25 + TornadoFX** : TornadoFX 1.7.20 utilise la réflexion Kotlin intensivement. Des `--add-opens` JVM sont nécessaires (configurés dans le task `runDesktop`).

2. **Affichage sur Raspberry Pi** : JavaFX fonctionne en X11/Wayland. S'assurer qu'un serveur d'affichage est actif (`DISPLAY` ou `WAYLAND_DISPLAY` défini).

3. **Pas de tests TornadoFX** : Les tests UI JavaFX sont complexes à automatiser. On privilégie les tests unitaires sur les controllers et la logique métier (déjà couverts à 95%+).

4. **Le CLI reste fonctionnel** : Le point d'entrée `MainKt` n'est pas touché. Le desktop est un point d'entrée supplémentaire.
