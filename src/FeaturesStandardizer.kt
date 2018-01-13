class FeaturesStandardizer(private val targetPosition: Int, private val columnDescriptor: List<Int>) {

    val categoricalAttributesValues = mutableMapOf<Int, List<Double>>()
    val categoricalAttributesMeanings = mutableMapOf<Int, Map<String,Double>>()
    private val targetAttributesKnown: MapOfTargetAttributes = MapOfTargetAttributes(targetAttributesKnown = mutableMapOf())

    fun standardizeFeatures(dataSet: MutableList<List<String>>): Collection<Instance> {

        val auxDataSet: MutableList<Instance> = toMutableListOfInstances(dataSet)

        //standardize target attribute
        auxDataSet.forEachIndexed { index, instance ->
            auxDataSet[index].targetAttributeInt = targetAttributesKnown.insertTargetAttribute(instance.targetAttribute)!!
            auxDataSet[index].targetAttribute = instance.targetAttribute
        }

        return auxDataSet
    }

    private fun toMutableListOfInstances(dataSet: MutableList<List<String>>): MutableList<Instance> {

        val instancesConverted: MutableList<Instance> = mutableListOf()

        //collect possible values for each categorical attribute and replace categorical strings with real values
        columnDescriptor.indices.forEach { column ->
            if (columnDescriptor[column] == DataReader.IS_CATEGORICAL) {

                val values = mutableListOf<Double>()
                val stringValues = mutableMapOf<String, Double>()
                dataSet.forEachIndexed { instanceIndex, instance ->

                    //make a mutable copy to use as a replacement in case we need to make alterations
                    val mutableInstance = instance.toMutableList()
                    var value: Double
                    try {
                        value = instance[column].toDouble()
                    } catch (exception: NumberFormatException) {
                        //if we have a mapping for string attribute to double, we swap the string for it
                        if (stringValues.containsKey(instance[column])) {
                            value = stringValues[instance[column]]!!
                            mutableInstance[column] = stringValues[instance[column]].toString()
                        }
                        //if we DON'T have a mapping for string attribute to double, create one
                        else {
                            value = if (values.isNotEmpty()) values.max()!! + 1 else 0.0
                            stringValues.put(instance[column], value)
                            mutableInstance[column] = value.toString()
                        }
                    }
                    if (!values.contains(value)) values.add(value)

                    //save possible alteration to instance
                    dataSet[instanceIndex] = mutableInstance
                }
                categoricalAttributesValues.put(column, values.toList())
                categoricalAttributesMeanings.put(column, stringValues)
            }
        }

        dataSet.forEach { data ->
            instancesConverted.add(convertAttributesToDoubles(data))
        }

        return instancesConverted
    }

    private fun convertAttributesToDoubles(stringAttributes: List<String>): Instance {

        val instanceConverted = Instance(
                attributes = mutableListOf(),
                targetAttribute = "",
                targetAttributeInt = 0
        )

        stringAttributes.forEachIndexed { index, stringAttribute ->
            if (index == targetPosition) instanceConverted.targetAttribute = stringAttribute
            else instanceConverted.attributes.add(stringAttribute.toDouble())
        }

        return instanceConverted
    }
}