package app.simplecloud.migration.api.serializer

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object GenericEnumSerializer : TypeSerializer<Enum<*>> {

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(type: Type, node: ConfigurationNode): Enum<*> {
        val value = node.string ?: throw SerializationException("No value present in node")

        if (type !is Class<*> || !type.isEnum) {
            throw SerializationException("Type is not an enum class")
        }

        return try {
            java.lang.Enum.valueOf(type as Class<out Enum<*>>, value)
        } catch (e: IllegalArgumentException) {
            throw SerializationException("Invalid enum constant")
        }
    }

    override fun serialize(type: Type, obj: Enum<*>?, node: ConfigurationNode) {
        node.set(obj?.name)
    }

}