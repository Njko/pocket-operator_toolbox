package fr.nicolaslinard.po.toolbox

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import fr.nicolaslinard.po.toolbox.commands.CreateCommand

class POToolbox : CliktCommand(name = "po-toolbox") {
    override fun help(context: com.github.ajalt.clikt.core.Context) =
        "Pocket Operator pattern management tool"

    override fun run() = Unit
}

fun main(args: Array<String>) = POToolbox()
    .subcommands(CreateCommand())
    .main(args)
