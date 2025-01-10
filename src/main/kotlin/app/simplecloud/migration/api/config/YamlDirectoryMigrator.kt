package app.simplecloud.migration.api.config

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * @author Niklas Nieberler
 */

class YamlDirectoryMigrator<E>(
    private val directory: Path,
    private val versionPattern: String = "version"
) {

    private val directoryFile = directory.toFile()

    fun migrate(clazz: Class<E>, vararg migrators: ConfigurationNodeMigrator<E>) {
        if (!this.directoryFile.exists())
            return
        Files.walk(this.directory)
            .toList()
            .filter { !it.toFile().isDirectory && it.toString().endsWith(".yml") }
            .mapNotNull { migrateFile(clazz, it.toFile(), *migrators) }
    }

    private fun migrateFile(clazz: Class<E>, file: File, vararg migrators: ConfigurationNodeMigrator<E>) {
        YamlConfigurationMigrator<E>(file, this.versionPattern).migrate(clazz, *migrators)
    }

}

inline fun <reified E> YamlDirectoryMigrator<E>.migrate(vararg migrators: ConfigurationNodeMigrator<E>) {
    this.migrate(E::class.java, *migrators)
}