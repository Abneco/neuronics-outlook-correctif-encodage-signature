/*
 * Copyright 2024 Sylvain Bernard - NEURONICS SA
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */

package be.neuronics.correctif_encodage_signature_outlook

import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

class Main {

    companion object {
        private val logger = LoggerFactory.getLogger(Main::class.java)

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("🚀 Démarrage de l'application correctif Outlook")
            afficherConditionsUtilisation()

            val defaultPath = "${System.getProperty("user.home")}\\AppData\\Roaming\\Microsoft\\Signatures"
            print("Chemin du dossier des signatures (Par défaut: $defaultPath) : ")
            val inputPath = readlnOrNull()?.takeIf { it.isNotBlank() } ?: defaultPath

            val signaturesDir = File(inputPath)

            if (!signaturesDir.exists() || !signaturesDir.isDirectory) {
                logger.error("❌ Le dossier indiqué n'existe pas : ${signaturesDir.absolutePath}")
                return
            }

            val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
            val timestamp = LocalDateTime.now().format(dateTimeFormatter)

            val backupDir = File("${signaturesDir.absolutePath}_BackupIT_$timestamp")
            backupAndCreateDir(signaturesDir, backupDir)


            print("Entrez la mailbox à corriger (ex: hello@mydomain.be) : ")
            val mailbox = readlnOrNull()?.takeIf { it.isNotBlank() }
            if (mailbox == null) {
                logger.error("❌ Aucune mailbox fournie. Arrêt du traitement.")
                return
            }

            val signatureRepair = SignatureRepair()

            signaturesDir.walkTopDown()
                .filter { it.isFile && it.extension.equals("htm", ignoreCase = true) && it.name.contains("($mailbox)") }
                .forEach { file ->
                    signatureRepair.fixBrokenAccentsToOutput(file, file)
                }


            logger.info("✅ Tous les fichiers pour la mailbox $mailbox ont été corrigés.")
        }

        private fun backupAndCreateDir(sourceDir: File, backupDir: File) {
            if (!backupDir.exists()) {
                backupDir.mkdirs()
                sourceDir.copyRecursively(backupDir, overwrite = true)
                logger.info("📂 Backup créé : ${backupDir.absolutePath}")
            } else {
                logger.info("⚠️ Backup déjà existant : ${backupDir.absolutePath}")
            }
        }

        private fun afficherConditionsUtilisation() {
            println("""
                |⚠️ CONDITIONS D'UTILISATION - LICENCE APACHE 2.0 ⚠️
                |
                |Copyright [2025] [NEURONICS SA - Sylvain Bernard]
                |
                |Ce logiciel est distribué sous licence Apache License, Version 2.0.
                |Vous pouvez consulter la licence complète à l'adresse suivante :
                |http://www.apache.org/licenses/LICENSE-2.0
                |
                |Le logiciel est fourni « TEL QUEL », sans garantie d'aucune sorte,
                |expresse ou implicite, y compris mais sans s'y limiter les garanties
                |de qualité marchande ou d'adéquation à un usage particulier.
                |
                |Avant toute modification, une sauvegarde automatique sera réalisée
                |dans un dossier clairement identifié, sous le format suivant :
                |<Chemin_Signatures>_BackupIT_<horodatage>
                |
                |En utilisant ce programme, vous acceptez explicitement les termes
                |de la licence Apache 2.0 et dégagez l'auteur de toute responsabilité.
                |
                |Souhaitez-vous continuer ? (y/n)
            """.trimMargin())

            val confirmation = readlnOrNull()
            if (confirmation?.lowercase() != "y") {
                println("Opération annulée par l'utilisateur.")
                exitProcess(0)
            }
        }
    }
}
