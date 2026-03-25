package fr.nicolaslinard.po.toolbox.desktop

import javafx.stage.Stage
import tornadofx.App
import tornadofx.launch

class POToolboxApp : App(MainView::class) {
    override fun start(stage: Stage) {
        stage.minWidth = 900.0
        stage.minHeight = 600.0
        super.start(stage)
        val css = javaClass.getResource("/css/app.css")?.toExternalForm()
        if (css != null) stage.scene.stylesheets.add(css)
    }

    override fun stop() {
        SharedPlaybackController.instance.dispose()
        super.stop()
    }
}

fun main(args: Array<String>) {
    launch<POToolboxApp>(args)
}
