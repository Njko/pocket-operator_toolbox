# PO-Toolbox — Roadmap & TODO

## 1. Support de l'ensemble des Pocket Operators de Teenage Engineering

**Priorité :** Haute
**Complexité :** Élevée

L'application ne prend actuellement en charge que le **PO-12 Rhythm** (drum machine). L'objectif est d'étendre le support à l'ensemble de la gamme Pocket Operator afin de couvrir tous les workflows de création musicale.

### Modèles à supporter

**Série PO-10 (Original, 2015) :**
| Modèle | Nom | Type | Sons | Spécificités |
|--------|-----|------|------|--------------|
| PO-12 | Rhythm | Drum machine | 16 sons de percussion | ✅ Déjà supporté |
| PO-14 | Sub | Synthé basse | 16 sons de basse | Séquenceur mélodique (notes), pas de grille percussion |
| PO-16 | Factory | Synthé lead | 16 sons lead | Séquenceur mélodique avec accords |

**Série PO-20 (8-bit, 2016) :**
| Modèle | Nom | Type | Sons | Spécificités |
|--------|-----|------|------|--------------|
| PO-20 | Arcade | Chiptune | 15 sons 8-bit | Synthèse chiptune, arpégiateur |
| PO-24 | Office | Noise/Percussion | 16 sons industriels | Bruits de bureau, percussion noise |
| PO-28 | Robot | Synthé lead 8-bit | 15 sons robot | Micro intégré, vocoder-like |

**Série PO-30 (Metal, 2017–2018) :**
| Modèle | Nom | Type | Sons | Spécificités |
|--------|-----|------|------|--------------|
| PO-32 | Tonic | Drum machine avancée | 16 sons custom via Microtonic | Import de sons personnalisés via transfert audio |
| PO-33 | K.O! | Sampler | 40 slots (8 mélodiques + 8 drums) | Enregistrement/sampling, time-stretch |
| PO-35 | Speak | Synthé vocal | 15 sons | Micro intégré, synthèse vocale |

**Éditions spéciales :**
| Modèle | Nom | Type | Spécificités |
|--------|-----|------|--------------|
| PO-128 | Mega Man | Chiptune | Sons Mega Man (Capcom collab) |
| PO-133 | Street Fighter | Sampler | Basé sur le PO-33, sons Street Fighter |
| PO-137 | Rick and Morty | Synthé vocal | Basé sur le PO-35, voix Rick & Morty |

### Impact sur l'architecture

- **Modèle de données** : Généraliser `PO12Pattern` → `POPattern` avec un type de PO et un mapping de sons spécifique à chaque modèle. Les modèles `PO12DrumVoice` doivent être abstraits pour supporter les sons mélodiques (notes MIDI) en plus des percussions.
- **MIDI Export** : Le `MidiNoteMapper` doit être étendu pour mapper les sons de chaque PO vers les programmes MIDI correspondants (drums sur canal 10, synthés sur canal 1-9).
- **Playback** : Le `MidiPlaybackService` doit supporter la lecture de patterns mélodiques (pas seulement des drums).
- **UI** : L'écran de création doit s'adapter au type de PO sélectionné (grille percussion vs séquenceur mélodique).
- **Templates** : Ajouter des templates spécifiques à chaque modèle dans `BuiltInTemplates`.

### Ressources

- Documentation officielle : https://teenage.engineering/guides
- Chaque PO a un guide dédié (ex: https://teenage.engineering/guides/po-12)

---

## 2. Appliquer les options d'accessibilité à l'écran d'édition de grille

**Priorité :** Haute
**Complexité :** Moyenne

### Problème

Le dialogue d'édition de pattern (`NewPatternDialog`) n'hérite pas des préférences d'accessibilité (thème, taille de police, daltonisme, réduction d'animations) définies dans le menu Accessibilité de l'écran principal. Cela crée une incohérence visuelle et une rupture d'expérience pour les utilisateurs qui dépendent de ces réglages.

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewPatternDialog.kt` — Dialog de création/édition
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/GeneratePatternDialog.kt` — Dialog de génération depuis template
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/ThemeManager.kt` — Doit appliquer les styles aux scènes des dialogs

### Tâches

- [ ] Appeler `ThemeManager.apply(dialogPane.scene, prefs)` après la construction de chaque dialog
- [ ] S'assurer que les inline styles dans `NewPatternDialog` et `GeneratePatternDialog` utilisent des classes CSS plutôt que des styles hardcodés
- [ ] Convertir les tailles de police hardcodées en `em` dans les dialogs
- [ ] Tester avec chaque combinaison theme × daltonisme × taille de police

---

## 3. Corriger le redimensionnement de l'interface au-delà de 150% de taille de police

**Priorité :** Haute
**Complexité :** Moyenne

### Problème

Lorsque l'utilisateur augmente la taille de police au-delà de 150% via les paramètres d'accessibilité, certains éléments de l'interface se chevauchent, sont tronqués ou débordent de leur conteneur. La grille de steps (16 colonnes) est particulièrement impactée.

### Zones problématiques identifiées

- **Grille de steps** (`PatternDetailView`) : Les 16 colonnes à largeur fixe (`prefWidth = 22.0`) ne s'adaptent pas quand le texte grossit. Les ● et les numéros de step débordent.
- **Colonnes du tableau** (`PatternListView`) : Les largeurs fixes (`prefWidth = 160.0`, `35.0`, etc.) ne laissent pas assez de place au texte agrandi.
- **Toolbar** (`MainView`) : Les boutons avec du texte fixe peuvent se chevaucher.
- **Dialog d'édition** (`NewPatternDialog`) : La grille de 16 ToggleButtons à 32×32px devient trop petite pour le texte.

### Tâches

- [ ] Remplacer les `prefWidth` fixes par des valeurs dynamiques calculées en fonction de `fontSizeMultiplier`
- [ ] Utiliser `Region.USE_COMPUTED_SIZE` là où c'est possible
- [ ] Ajouter un `ScrollPane` horizontal sur la grille de steps si elle dépasse la largeur disponible
- [ ] Tester avec des multiplicateurs de 1.5x, 1.75x et 2.0x
- [ ] S'assurer que le `minWidth` de la fenêtre s'adapte

---

## 4. Accès rapide à la documentation de chaque Pocket Operator

**Priorité :** Moyenne
**Complexité :** Faible

### Objectif

Permettre à l'utilisateur de consulter rapidement la documentation officielle de chaque Pocket Operator directement depuis l'application, notamment pour comprendre les fonctions spécifiques à chaque modèle (effets, parameter locks, swing, etc.).

### Approche proposée

- **Menu "Aide"** dans la barre de menus avec un sous-menu par modèle de PO
- Chaque entrée ouvre le navigateur par défaut sur le guide officiel correspondant :
  - PO-12 Rhythm → https://teenage.engineering/guides/po-12
  - PO-14 Sub → https://teenage.engineering/guides/po-14
  - PO-16 Factory → https://teenage.engineering/guides/po-16
  - etc.
- **Raccourci clavier** : F1 pour ouvrir la doc du modèle actif
- **Lien contextuel** : Dans l'écran d'édition, un bouton "?" qui ouvre la doc du PO correspondant au pattern en cours d'édition

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/MainView.kt` — Ajout du menu Aide
- Utilisation de `java.awt.Desktop.browse(URI)` pour ouvrir les URLs

---

## 5. Publication en tant qu'exécutable natif (Windows & Linux)

**Priorité :** Moyenne
**Complexité :** Élevée

### Problème

L'application est actuellement distribuée comme un fat JAR (`po-toolbox-1.0.0-win.jar`) qui nécessite Java 25 installé et une commande `java -jar` avec des flags `--add-opens`. Ce n'est pas une expérience utilisateur acceptable pour une application desktop.

### Objectif

Produire des exécutables natifs :
- **Windows** : `.exe` avec installeur `.msi` (ou `.exe` portable)
- **Linux** : `.deb`, `.rpm`, ou AppImage

### Approche recommandée

**Option A — `jpackage` (JDK 25 intégré) :**
```bash
jpackage \
  --input build/libs \
  --main-jar po-toolbox-1.0.0-win.jar \
  --main-class fr.nicolaslinard.po.toolbox.desktop.POToolboxAppKt \
  --name "PO-Toolbox" \
  --type msi \
  --icon src/main/resources/icon.ico \
  --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED" \
  --java-options "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" \
  --java-options "--enable-native-access=ALL-UNNAMED"
```

**Option B — GraalVM Native Image :**
Compilation AOT en binaire natif. Plus complexe (réflexion JavaFX à configurer) mais produit un binaire sans dépendance JVM.

### Tâches

- [ ] Configurer `jpackage` dans le `build.gradle.kts`
- [ ] Créer les icônes d'application (`.ico` pour Windows, `.png` pour Linux)
- [ ] Adapter le workflow GitHub Actions pour produire les exécutables natifs par plateforme
- [ ] Tester l'installation et le lancement sur Windows et Linux propres (sans JDK)
- [ ] Signer les exécutables si nécessaire (codesigning Windows, Gatekeeper macOS)

### Fichiers impactés

- `build.gradle.kts` — Tâche `jpackage`
- `.github/workflows/` — Pipeline CI/CD pour build multi-plateforme

---

## 6. Création de patterns multi-mesures et chainage pour composer un morceau

**Priorité :** Haute
**Complexité :** Élevée

### Objectif

Permettre de créer plusieurs mesures (slots) d'un coup, de les jouer en boucle ensemble, et de les chaîner pour composer un morceau complet. Actuellement, un pattern = 1 mesure de 16 steps. L'objectif est de supporter les workflows multi-mesures comme sur le vrai PO-12 (chainage de patterns 1-16).

### Fonctionnalités

#### 6a. Création multi-slots

- L'utilisateur peut choisir le nombre de mesures lors de la création (1 à 16)
- Chaque mesure est un slot avec sa propre grille de 16 steps
- Navigation entre les mesures via des onglets ou des boutons Précédent/Suivant
- Les voix sont partagées entre toutes les mesures (même kit de sons)
- Sauvegarde en un seul fichier markdown avec toutes les mesures

#### 6b. Playback multi-mesures

- Le `MidiPlaybackService` joue toutes les mesures séquentiellement
- En mode loop, la séquence complète boucle (mesure 1 → 2 → ... → N → 1)
- L'indicateur de step dans `PatternDetailView` affiche quelle mesure est en cours
- Le `MidiExporter.createSequence` supporte déjà les patterns chaînés (via `exportPatternsToMidi`)

#### 6c. Chainage de patterns pour composer un morceau

- Nouveau concept : **Song** = séquence ordonnée de patterns avec répétitions
- Ex: "Intro(×2) → Couplet(×4) → Refrain(×2) → Couplet(×4) → Outro(×1)"
- Interface de composition : liste ordonnée de patterns avec compteur de répétitions
- Playback du morceau complet avec indicateur de progression
- Export MIDI du morceau entier

### Impact sur l'architecture

- **Modèle de données** :
  - `PO12Pattern` supporte déjà un `number` (1-16) — étendre pour gérer une liste de mesures
  - Nouveau modèle `Song` : liste de `(pattern: PO12Pattern, repeatCount: Int)`
  - Le `PatternChain` existant peut servir de base
- **MidiPlaybackService** : Adapter `play()` pour accepter une liste de patterns et gérer le `loopEndPoint` sur l'ensemble
- **MidiExporter** : `exportPatternsToMidi` existe déjà — l'adapter pour le format Song
- **UI** :
  - `NewPatternDialog` : Ajouter la navigation multi-mesures
  - `PatternDetailView` : Afficher la mesure courante pendant le playback
  - Nouvel écran `SongEditor` pour la composition

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/PO12Pattern.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/PatternChain.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiPlaybackService.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MidiExporter.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownWriter.kt` / `MarkdownParser.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewPatternDialog.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternDetailView.kt`
- Nouveau : `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/SongEditorView.kt`
- Nouveau : `src/main/kotlin/fr/nicolaslinard/po/toolbox/models/Song.kt`
