# PO-Toolbox — Roadmap & TODO

> Les items 1-6 de la roadmap initiale sont tous terminés et livrés.
> Cette nouvelle roadmap couvre les améliorations identifiées lors des tests.

---

## 1. Refonte complète de l'écran d'édition de pattern

**Priorité :** Haute
**Complexité :** Élevée

### Problème

L'écran d'édition de pattern (`NewPatternDialog`) est un dialogue modal basique qui ne ressemble pas à l'écran principal. L'expérience utilisateur est incohérente entre la consultation (riche, stylée, avec animations) et l'édition (dialog JavaFX brut).

### Objectif

Reconstruire l'écran d'édition pour qu'il partage le même langage visuel que l'écran principal (`PatternDetailView`) :
- Même grille de steps avec les mêmes styles CSS (●/· colorés, beat separators)
- Même disposition : voice labels à gauche, grille de 16 steps à droite
- Les steps sont cliquables pour toggler on/off (au lieu de ToggleButtons séparés)
- Preview en temps réel du pattern pendant l'édition
- Support du thème actif (dark/light/high-contrast) et du scaling d'accessibilité
- Navigation multi-mesures intégrée dans le même écran

### Tâches

- [ ] Extraire le rendu de grille de `PatternDetailView` dans un composant réutilisable (`StepGridView`)
- [ ] Créer un mode "éditable" pour `StepGridView` (clic pour toggler un step)
- [ ] Refondre `NewPatternDialog` pour utiliser `StepGridView` en mode éditable
- [ ] Appliquer les mêmes styles CSS que l'écran principal (step-active, step-inactive, voice-label, beat-separator)
- [ ] Conserver le formulaire de metadata (nom, BPM, difficulté, modèle PO) dans un panneau latéral ou un header
- [ ] Intégrer la navigation multi-mesures (boutons < Mesure N >) dans la même vue
- [ ] S'assurer que le play/preview fonctionne pendant l'édition
- [ ] Tester avec tous les thèmes et niveaux de scaling

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/NewPatternDialog.kt` — Refonte complète
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternDetailView.kt` — Extraction du rendu grille
- Nouveau : `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/StepGridView.kt`

---

## 2. Playback multi-mesures pour les patterns existants

**Priorité :** Haute
**Complexité :** Moyenne

### Problème

Le playback (`MidiPlaybackService.playChain`) supporte déjà les chains multi-mesures, mais il n'est utilisé que pour les patterns créés dans la session courante (via `controller.activeDialogResult`). Les patterns multi-mesures sauvegardés sur disque ne sont pas rechargés comme des chains — ils sont lus comme des patterns isolés par `MarkdownParser`.

### Objectif

Quand un fichier contient plusieurs mesures (ou quand plusieurs fichiers sont chaînés), le bouton Play doit jouer l'ensemble des mesures en séquence, avec looping sur la totalité.

### Tâches

- [ ] Étendre `MarkdownWriter` pour sauvegarder les chains multi-mesures (plusieurs sections `## Pattern N` dans un seul fichier)
- [ ] Étendre `MarkdownParser` pour lire les fichiers multi-mesures et retourner une `PatternChain`
- [ ] Mettre à jour `PatternRepository` / `FilePatternRepository` pour gérer les chains
- [ ] Mettre à jour `PatternController` pour charger et stocker les chains associées aux fichiers
- [ ] Mettre à jour le bouton Play dans `MainView` pour jouer automatiquement la chain si le pattern sélectionné en fait partie
- [ ] Afficher "Mesure X / Y" dans `PatternDetailView` pendant le playback multi-mesures

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownWriter.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/io/MarkdownParser.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternRepository.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternController.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/MainView.kt`

---

## 3. Menu Paramètres unifié (incluant l'accessibilité)

**Priorité :** Moyenne
**Complexité :** Faible

### Problème

Le menu "Accessibilité" est un menu de premier niveau dans la barre de menus. Ce n'est pas une convention standard — les paramètres d'accessibilité devraient faire partie d'un menu "Paramètres" plus large, capable d'accueillir d'autres réglages futurs (langue, répertoire par défaut, raccourcis clavier, etc.).

### Objectif

- Renommer le menu "Accessibilité" en "Paramètres"
- Le dialogue de paramètres contient un panneau latéral avec des catégories :
  - **Apparence** : thème, daltonisme, taille de police
  - **Animations** : réduire les animations
  - **Général** : répertoire de patterns par défaut, langue (futur)
- Raccourci clavier : Ctrl+, (convention standard) ou garder Ctrl+Shift+A

### Tâches

- [ ] Renommer le menu "Accessibilité" → "Paramètres" dans `MainView.kt`
- [ ] Refondre `AccessibilityDialog` en `SettingsDialog` avec navigation par catégories (ListView à gauche, contenu à droite)
- [ ] Déplacer les préférences d'accessibilité dans la catégorie "Apparence"
- [ ] Ajouter une catégorie "Général" (vide pour l'instant, prête pour les futurs paramètres)
- [ ] Mettre à jour le raccourci clavier (Ctrl+, ou conserver Ctrl+Shift+A)

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/MainView.kt` — Renommer menu
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/AccessibilityDialog.kt` → Renommer en `SettingsDialog.kt`
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/AccessibilityPreferences.kt` — Éventuellement renommer en `AppPreferences.kt`

---

## 4. Visualisation des morceaux multi-mesures dans l'écran principal

**Priorité :** Haute
**Complexité :** Moyenne

### Problème

L'écran principal (`PatternDetailView`) affiche uniquement la première mesure d'un pattern, même si le fichier contient plusieurs mesures. L'utilisateur n'a aucun moyen visuel de savoir qu'un pattern est multi-mesures, ni de naviguer entre les mesures dans la vue de consultation.

### Objectif

Améliorer la visualisation dans `PatternDetailView` pour :
- Afficher clairement le nombre de mesures (ex: "3 mesures" sous le titre)
- Afficher toutes les mesures empilées verticalement (ou avec pagination)
- Mettre en surbrillance la mesure en cours de lecture pendant le playback
- Dans `PatternListView`, ajouter une colonne "Mesures" pour voir d'un coup d'œil quels patterns sont multi-mesures

### Tâches

- [ ] Ajouter un indicateur "N mesures" dans les metadata affichées par `PatternDetailView`
- [ ] Afficher toutes les mesures empilées avec un séparateur visuel entre chaque (ex: "--- Mesure 2 ---")
- [ ] Pendant le playback multi-mesures, mettre en surbrillance la mesure active (fond coloré ou bordure)
- [ ] Ajouter une colonne "Mesures" dans `PatternListView` (tableau de la liste)
- [ ] Permettre de cliquer sur une mesure pour la sélectionner/détailler

### Fichiers impactés

- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternDetailView.kt` — Affichage multi-mesures
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternListView.kt` — Colonne "Mesures"
- `src/main/kotlin/fr/nicolaslinard/po/toolbox/desktop/PatternController.kt` — Gestion de la mesure sélectionnée
- `src/main/resources/css/app.css` — Styles pour mesure active/inactive
