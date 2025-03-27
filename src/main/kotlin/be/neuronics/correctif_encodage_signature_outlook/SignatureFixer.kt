/*
 * Copyright 2024 Sylvain Bernard - NEURONICS SA
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package be.neuronics.correctif_encodage_signature_outlook

import java.io.*
import java.nio.charset.Charset


class SignatureRepair(private val encoding: Charset = Charset.forName("windows-1252")) {

    @Throws(IOException::class)
    fun fixBrokenAccentsToOutput(inputFile: File, outputFile: File) {
        val content = inputFile.readText(encoding)
        val fixedContent = fixBrokenCharacters(content)
        outputFile.writeText(fixedContent, encoding)
        println("✅ Correction terminée : ${outputFile.absolutePath}")
    }

    fun fixBrokenCharacters(text: String): String {
        val replacements = linkedMapOf(
            // === Double encodage très fréquent (absolument prioritaire) ===
            "ÃƒÂ " to "à",
            "ÃƒÂ¡" to "á",
            "ÃƒÂ¢" to "â",
            "ÃƒÂ£" to "ã",
            "ÃƒÂ¤" to "ä",
            "ÃƒÂ©" to "é",
            "ÃƒÂ¨" to "è",
            "ÃƒÂª" to "ê",
            "ÃƒÂ«" to "ë",
            "ÃƒÂ®" to "î",
            "ÃƒÂ¯" to "ï",
            "ÃƒÂ¬" to "ì",
            "ÃƒÂ­" to "í",
            "ÃƒÂ³" to "ó",
            "ÃƒÂ´" to "ô",
            "ÃƒÂ¶" to "ö",
            "ÃƒÂ¹" to "ù",
            "ÃƒÂ¼" to "ü",
            "ÃƒÂ»" to "û",
            "ÃƒÂ§" to "ç",
            "ÃƒÂ€" to "À",
            "ÃƒÂ‰" to "É",
            "ÃƒÂŽ" to "Î",
            "Ãƒâ€°" to "É",
            "Ãƒâ€¹" to "Ë",
            "Ãƒâ€œ" to "Ó",
            "ÃƒÅ“" to "Œ",
            "ÃƒÅ’" to "Œ",
            "Ãƒâ€š" to "Â",
            "Ãâ€š" to "Â",
            "ÃƒÅ" to "Å",
            "Ãƒâ€ž" to "Ä",
            "Ãâ€ž" to "Ä",
            "ÃƒÂŸ" to "ß",

            // === Cas supplémentaires fréquents (prioritaires aussi) ===
            "À©" to "é",
            "À¨" to "è",
            "Àª" to "ê",
            "À®" to "î",
            "À " to "à",
            "À§" to "ç",
            "À¹" to "ù",
            "À»" to "û",
            "À´" to "ô",
            "À¢" to "â",
            "À€" to "À",
            "À‰" to "É",
            "ÀŽ" to "Î",
            "À  " to "à ",

            // === Encodage simple (UTF-8 vers Windows-1252) ===
            "Ã€" to "À", "Ã" to "Á", "Ã‚" to "Â", "Ãƒ" to "Ã", "Ã„" to "Ä",
            "Ã…" to "Å", "Ã†" to "Æ", "Ã‡" to "Ç", "Ãˆ" to "È", "Ã‰" to "É",
            "ÃŠ" to "Ê", "Ã‹" to "Ë", "ÃŒ" to "Ì", "Ã" to "Í", "ÃŽ" to "Î",
            "Ã" to "Ï", "Ã‘" to "Ñ", "Ã’" to "Ò", "Ã“" to "Ó", "Ã”" to "Ô",
            "Ã•" to "Õ", "Ã–" to "Ö", "Ã˜" to "Ø", "Ã™" to "Ù", "Ãš" to "Ú",
            "Ã›" to "Û", "Ãœ" to "Ü", "Ã" to "Ý", "Ãž" to "Þ", "ÃŸ" to "ß",
            "Ã " to "à", "Ã¡" to "á", "Ã¢" to "â", "Ã£" to "ã", "Ã¤" to "ä",
            "Ã¥" to "å", "Ã¦" to "æ", "Ã§" to "ç", "Ã¨" to "è", "Ã©" to "é",
            "Ãª" to "ê", "Ã«" to "ë", "Ã¬" to "ì", "Ã­" to "í", "Ã®" to "î",
            "Ã¯" to "ï", "Ã°" to "ð", "Ã±" to "ñ", "Ã²" to "ò", "Ã³" to "ó",
            "Ã´" to "ô", "Ãµ" to "õ", "Ã¶" to "ö", "Ã¸" to "ø", "Ã¹" to "ù",
            "Ãº" to "ú", "Ã»" to "û", "Ã¼" to "ü", "Ã½" to "ý", "Ã¾" to "þ", "Ã¿" to "ÿ",

            // === Symboles divers ===
            "â€˜" to "‘", "â€™" to "’", "â€œ" to "“", "â€" to "”",
            "â€“" to "–", "â€”" to "—", "â€¦" to "…", "â€¢" to "•",
            "â‚¬" to "€", "â„¢" to "™", "â„—" to "℗", "â„œ" to "ℜ",
            "Â©" to "©", "Â®" to "®", "Â°" to "°", "Â±" to "±",
            "Â·" to "·", "Âµ" to "µ", "Â«" to "«", "Â»" to "»",
            "Â¡" to "¡", "Â¢" to "¢", "Â£" to "£", "Â¤" to "¤",
            "Â¥" to "¥", "Â¦" to "¦", "Â§" to "§", "Â¨" to "¨",
            "Â¬" to "¬", "Â²" to "²", "Â³" to "³", "Â´" to "´",
            "Â¶" to "¶", "Â¸" to "¸", "Â¹" to "¹", "Âº" to "º",
            "Â¼" to "¼", "Â½" to "½", "Â¾" to "¾",

            // === Résidus généraux ===
            "Ã‚" to "",
            "Â" to ""
        )

        var correctedText = text
        var previous: String
        do {
            previous = correctedText
            replacements.forEach { (bad, good) ->
                correctedText = correctedText.replace(bad, good)
            }
        } while (correctedText != previous)

        return correctedText
    }


}
