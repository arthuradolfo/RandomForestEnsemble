data class MapOfTargetAttributes(var targetAttributesKnown: MutableMap<String, Int>) {
    var lastAttributeValue = 0

    fun insertTargetAttribute(targetAttribute: String): Int? {
        if (!checkIfTargetAttributeIsKnown(targetAttribute)) {
            targetAttributesKnown.put(targetAttribute, lastAttributeValue)
            lastAttributeValue++
            return targetAttributesKnown[targetAttribute]
        } else {
            return targetAttributesKnown[targetAttribute]
        }
    }

    private fun checkIfTargetAttributeIsKnown(targetAttribute: String): Boolean {
        return targetAttributesKnown[targetAttribute] != null
    }
}

fun main(args: Array<String>) {
}