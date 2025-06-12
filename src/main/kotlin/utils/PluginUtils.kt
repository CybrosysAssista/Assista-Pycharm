package utils

object PluginUtils {

    fun parseFieldAttributes(data: String): Map<String, String> {
        val fieldAttributes = mutableMapOf<String, String>()
        val fieldData = data.split("|")

        for (attribute in fieldData) {
            val parts = attribute.split("=", limit = 2)
            if (parts.size == 2) {
                fieldAttributes[parts[0]] = parts[1]
            }
        }

        return fieldAttributes
    }
}