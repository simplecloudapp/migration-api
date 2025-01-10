package app.simplecloud.migration.api.config

import org.spongepowered.configurate.CommentedConfigurationNode

/**
 * @author Niklas Nieberler
 */

interface ConfigurationNodeMigrator<E> {

    fun getVersion(): Int

    fun migrate(configurationNode: CommentedConfigurationNode, oldConfig: E?): E

}