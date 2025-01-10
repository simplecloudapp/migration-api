package app.simplecloud.migration.api.config

import app.simplecloud.migration.api.serializer.GenericEnumSerializer
import org.apache.logging.log4j.LogManager
import org.spongepowered.configurate.CommentedConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.objectMapperFactory
import org.spongepowered.configurate.yaml.NodeStyle
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import java.io.File

/**
 * @author Niklas Nieberler
 */

class YamlConfigurationMigrator<E>(
    private val file: File,
    private val versionPattern: String = "version"
) {

    private val logger = LogManager.getLogger(YamlConfigurationMigrator::class.java)

    private val configurationLoader = createConfigurationLoader()
    private val defaultOptions = ConfigurationOptions.defaults()

    fun migrate(clazz: Class<E>, vararg migrators: ConfigurationNodeMigrator<E>) {
        if (!this.file.exists() && !this.file.endsWith(".yml"))
            return
        val configurationNode = this.configurationLoader.load(this.defaultOptions)
        val configurationMigrator = findPossibleConfigurationMigrator(configurationNode, *migrators) ?: return
        this.logger.info("Migrate $file to config version ${configurationMigrator.getVersion()}")

        val entity = configurationMigrator.migrate(configurationNode, configurationNode.get(clazz))
        save(clazz, entity)
    }

    private fun findPossibleConfigurationMigrator(
        configurationNode: CommentedConfigurationNode,
        vararg migrators: ConfigurationNodeMigrator<E>
    ): ConfigurationNodeMigrator<E>? {
        val migrator = migrators.maxByOrNull { it.getVersion() } ?: return null
        if (configurationNode.node(this.versionPattern).getString("1").toInt() >= migrator.getVersion())
            return null
        return migrator
    }

    private fun save(clazz: Class<E>, entity: E) {
        this.file.delete()
        val configurationNode = this.configurationLoader.createNode(this.defaultOptions)
        configurationNode.set(clazz, entity)
        this.configurationLoader.save(configurationNode)
    }

    private fun createConfigurationLoader(): YamlConfigurationLoader {
        return YamlConfigurationLoader.builder()
            .path(this.file.toPath())
            .nodeStyle(NodeStyle.BLOCK)
            .defaultOptions { options ->
                options.serializers { builder ->
                    builder.registerAnnotatedObjects(objectMapperFactory())
                    builder.register(Enum::class.java, GenericEnumSerializer)
                }
            }.build()
    }

}

inline fun <reified E> YamlConfigurationMigrator<E>.migrate(vararg migrators: ConfigurationNodeMigrator<E>) {
    this.migrate(E::class.java, *migrators)
}