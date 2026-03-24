package fr.nicolaslinard.po.toolbox.desktop

import tornadofx.View
import tornadofx.action
import tornadofx.borderpane
import tornadofx.item
import tornadofx.menu
import tornadofx.menubar
import tornadofx.splitpane

class MainView : View("PO-12 Toolbox") {

    private val controller: PatternController by inject()

    override val root = borderpane {
        prefWidth = 1100.0
        prefHeight = 700.0

        top = menubar {
            menu("Fichier") {
                item("Actualiser") { action { controller.loadPatterns() } }
            }
        }

        center = splitpane {
            add(PatternListView::class)
            add(PatternDetailView::class)
            setDividerPositions(0.32)  // SplitPane native JavaFX method
        }
    }

    override fun onDock() {
        controller.loadPatterns()
    }
}
