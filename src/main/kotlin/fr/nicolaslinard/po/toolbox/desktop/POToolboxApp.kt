package fr.nicolaslinard.po.toolbox.desktop

import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch

class POToolboxApp : App(MainView::class) {
    override fun start(stage: Stage) {
        val sz = ScaledSize()
        stage.minWidth = sz.mainMinWidth
        stage.minHeight = sz.mainMinHeight
        super.start(stage)
        ThemeManager.apply(stage.scene, SharedAccessibilityPreferences.instance)
    }

    override fun stop() {
        SharedPlaybackController.instance.dispose()
        super.stop()
    }
}

fun main(args: Array<String>) {
    launch<POToolboxApp>(args)
}
