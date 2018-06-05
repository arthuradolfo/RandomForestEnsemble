package bilbo.randomforestensemble

data class MapOfTargetAttributes(var targetAttributesKnown: MutableMap<String, Int>) {
    var lastAttributeValue = 0

    fun insertTargetAttribute(targetAttribute: String): Int? {
        return if (!checkIfTargetAttributeIsKnown(targetAttribute)) {
            targetAttributesKnown.put(targetAttribute, lastAttributeValue)
            lastAttributeValue++
            targetAttributesKnown[targetAttribute]
        } else {
            targetAttributesKnown[targetAttribute]
        }
    }

    private fun checkIfTargetAttributeIsKnown(targetAttribute: String): Boolean {
        return targetAttributesKnown[targetAttribute] != null
    }
}

fun main(args: Array<String>) {
}