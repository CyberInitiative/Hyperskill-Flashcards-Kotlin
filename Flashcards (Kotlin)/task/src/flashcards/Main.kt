package flashcards

import java.io.File

object Logger {
    private val loggedMessages = mutableListOf<String>()

    fun printlnAndLogMessage(message: String) {
        println(message)
        loggedMessages.add(message)
    }

    fun readlnAndLog(): String {
        val message = readln()
        loggedMessages.add(message)
        return message
    }

    fun saveLogs() {
        printlnAndLogMessage("File name:")
        val fileName = readlnAndLog()

        val separator = File.separator
        val fullFileName = "src${separator}$fileName"
        val file = File(fileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText("")

        for (log in loggedMessages) {
            file.appendText("$log\n")
        }
        printlnAndLogMessage("The log has been saved.")
    }

}

data class Card(val cardName: String, var cardDefinition: String, var statistics: Int = 0) : Comparable<Card> {
    override fun compareTo(other: Card): Int = this.statistics.compareTo(other.statistics)


    override fun toString(): String {
        return "$cardName=$cardDefinition=$statistics"
    }
}

class CardManager {
    private val cards = mutableListOf<Card>()

    private fun checkCardByName(cardName: String): Boolean {
        for (card in cards) {
            if (cardName == card.cardName) {
                return true
            }
        }
        return false
    }

    private fun checkCardByDefinition(cardDefinition: String): Boolean {
        for (card in cards) {
            if (cardDefinition == card.cardDefinition) {
                return true
            }
        }
        return false
    }

    private fun getCardByName(cardName: String): Card? {
        for (card in cards) {
            if (cardName == card.cardName) {
                return card
            }
        }
        return null
    }

    private fun getCardByDefinition(cardDefinition: String): Card? {
        for (card in cards) {
            if (cardDefinition == card.cardDefinition) {
                return card
            }
        }
        return null
    }

    fun addCard() {
        Logger.printlnAndLogMessage("The card:")
        val cardName = Logger.readlnAndLog()
        if (checkCardByName(cardName)) {
            Logger.printlnAndLogMessage("The card \"$cardName\" already exists.")
            return
        }

        Logger.printlnAndLogMessage("The definition of the card:")
        val cardDefinition = Logger.readlnAndLog()
        if (checkCardByDefinition(cardDefinition)) {
            Logger.printlnAndLogMessage("The definition \"$cardDefinition\" already exists.")
            return
        }

        cards.add(Card(cardName, cardDefinition))
        Logger.printlnAndLogMessage("The pair (\"$cardName\":\"$cardDefinition\") has been added.")
    }

    fun removeCard() {
        Logger.printlnAndLogMessage("Which card?")
        val cardName = Logger.readlnAndLog()

        if (checkCardByName(cardName)) {
            cards.remove(getCardByName(cardName))
            Logger.printlnAndLogMessage("The card has been removed.")
        } else {
            Logger.printlnAndLogMessage("Can't remove \"$cardName\": there is no such card.")
        }
    }

    fun importCards(fileName: String = "") {
        Logger.printlnAndLogMessage("File name:")
        var locFileName: String
        if(fileName.isBlank()) {
            locFileName = Logger.readlnAndLog()
        } else {
            locFileName = fileName
        }
        val separator = File.separator
        val fullFileName = "src${separator}$locFileName"
        val file = File(locFileName)
        if (file.exists()) {
            val lines = file.readLines()
            for (line in lines) {
                val (cardName, cardDefinition, statistics) = line.split("=")
                if (checkCardByName(cardName)) {
                    getCardByName(cardName)!!.cardDefinition = cardDefinition
                } else {
                    cards.add(Card(cardName, cardDefinition, statistics.toInt()))
                }
            }
            Logger.printlnAndLogMessage("${lines.size} cards have been loaded")
        } else {
            Logger.printlnAndLogMessage("File not found.")
        }
    }

    fun exportCards(fileName: String = "") {
        Logger.printlnAndLogMessage("File name:")
        var locFileName: String
        if(fileName.isBlank()) {
            locFileName = Logger.readlnAndLog()
        } else {
            locFileName = fileName
        }
        val separator = File.separator
        val fullFileName = "src${separator}$locFileName"
        val file = File(locFileName)
        if (!file.exists()) {
            file.createNewFile()
        }
        file.writeText("")
        for (card in cards) {
            file.appendText("$card\n")
        }
        Logger.printlnAndLogMessage("${cards.size} cards have been saved.")
    }

    fun getHardestCard() {
        val hardestValue = cards.maxOrNull()
        if (hardestValue == null || hardestValue.statistics == 0) {
            Logger.printlnAndLogMessage("There are no cards with errors.")
            return
        } else {
            val result = cards.filter { it.statistics == hardestValue.statistics }
            if (result.size == 1) {
                val card = result.first()
                Logger.printlnAndLogMessage("The hardest card is \"${card.cardName}\". You have ${card.statistics} errors answering it")
            } else if (result.size > 1) {
                val cards = cards.joinToString(separator = ", " ) { "\"${it.cardName}\""}
                Logger.printlnAndLogMessage("The hardest cards are $cards. You have ${hardestValue.statistics} errors answering them.")
            }
        }
    }

    fun resetStatistics() {
        for (card in cards) {
            card.statistics = 0
        }
        Logger.printlnAndLogMessage("Card statistics have been reset.")
    }

    fun ask() {
        Logger.printlnAndLogMessage("How many times to ask?")
        val timesToAsk = Logger.readlnAndLog().toInt()
        var timeAsked = 0
        mainloop@ for (card in cards) {
            if (timeAsked == timesToAsk) {
                break
            }
            timeAsked++
            Logger.printlnAndLogMessage("Print the definition of \"${card.cardName}\":")
            val answer = Logger.readlnAndLog()
            if (answer == card.cardDefinition) {
                Logger.printlnAndLogMessage("Correct!")
            } else {
                for (anotherCard in cards) {
                    if (answer == anotherCard.cardDefinition) {
                        Logger.printlnAndLogMessage("Wrong. The right answer is \"${card.cardDefinition}\", but your definition is correct for \"${anotherCard.cardName}\".")
                        card.statistics++
                        continue@mainloop
                    }
                }
                Logger.printlnAndLogMessage("Wrong. The right answer is \"${card.cardDefinition}\".")
                card.statistics++
            }
        }
    }

}

fun main(args: Array<String>) {
    val cardManager = CardManager()

    var importFileName: String? = null
    var exportFileName: String? = null

    for (i in args.indices) {
        when (args[i]) {
            "-import" -> if (i + 1 < args.size) importFileName = args[i + 1]
            "-export" -> if (i + 1 < args.size) exportFileName = args[i + 1]
        }
    }

    if(importFileName != null){
        cardManager.importCards(importFileName)
    }

    var command: String = ""
    while (command != "exit") {
        Logger.printlnAndLogMessage("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        command = Logger.readlnAndLog()
        when (command) {
            "add" -> cardManager.addCard()
            "remove" -> cardManager.removeCard()
            "import" -> cardManager.importCards()
            "export" -> cardManager.exportCards()
            "ask" -> cardManager.ask()
            "log" -> Logger.saveLogs()
            "reset stats" -> cardManager.resetStatistics()
            "hardest card" -> cardManager.getHardestCard()
            "exit" -> {
                if(exportFileName != null) {
                    cardManager.exportCards(exportFileName)
                }
                Logger.printlnAndLogMessage("Bye bye!")
                break
            }
        }
    }
}