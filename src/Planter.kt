import java.util.*
import kotlin.math.log2

class Planter(private val instances: List<Instance>, private val mAttribs: Int, private val categoricalAttributesValues: Map<Int, List<Int>>) {

    fun plantTree(): DecisionTree = recursivelyBuildTree(instances,
            instances.possibleAttributes().randomSubset(mAttribs).toMutableList()
    )

    private fun recursivelyBuildTree(instances: List<Instance>, possibleAttributes: MutableList<Int>): DecisionTree {
        when {
            instances.allAreSameClass() -> return DecisionLeaf(instances.first().targetAttributeInt)
            possibleAttributes.isEmpty() -> return DecisionLeaf(instances.majorityClass())
        }

        val nodeSplit = nodeSplitID3(instances, possibleAttributes)
        possibleAttributes.remove(nodeSplit.attribute)

        val decisionTree = TestNode(nodeSplit.attribute, mutableListOf())

        (0 until nodeSplit.ranges.size).forEach { branchIndex ->
            val instancesSubset = instances.filter { it.targetAttributeInt in nodeSplit.ranges[branchIndex] }
            if (instancesSubset.isEmpty())
                decisionTree.branches.add(branchIndex, DecisionLeaf(instances.majorityClass()))
            else
                decisionTree.branches.add(branchIndex, recursivelyBuildTree(instancesSubset, possibleAttributes))
        }

        return decisionTree
    }

    private fun List<Instance>.possibleAttributes(): List<Int> = (0 until this.first().attributes.size).toList()

    private fun List<Instance>.majorityClass(): Int {
        val classes = mutableMapOf<Int, Int>()
        this.forEach { instance ->
            if (classes.containsKey(instance.targetAttributeInt)) {
                val update = instance.targetAttributeInt + 1
                classes.replace(instance.targetAttributeInt, update)
            }
            else classes.put(instance.targetAttributeInt, 1)
        }
        return classes.values.max()!!
    }

    private fun List<Instance>.allAreSameClass(): Boolean
            = this.all { it.targetAttributeInt == this.first().targetAttributeInt }

    private fun List<Int>.randomSubset(size: Int): List<Int> {
        val listCopy = this.toMutableList()

        if (this.size < size) throw Exception("Provided size for subset is bigger then original set's size")

        //We don't need to shuffle the whole list
        for (i in this.lastIndex downTo this.size - size) {
            Collections.swap(listCopy, i, Random().nextInt(i + 1))
        }
        return this.subList(this.size - size, this.size)
    }

    private fun nodeSplitID3(instances: List<Instance>, possibleAttributes: List<Int>): NodeSplit {
        var maxGain = 0.0
        var maxGainNodeSplit = NodeSplit(0, emptyList())

        possibleAttributes.forEach { attribute ->
            //check if attribute is categorical or continuous
            if (categoricalAttributesValues.containsKey(attribute)) {

                val gain = infoGain(attribute, instances)

                if (gain > maxGain) {
                    //update comparison gain
                    maxGain = gain
                    //generate ranges from possible values of attribute
                    val ranges = mutableListOf<ClosedRange<Double>>()
                    categoricalAttributesValues[attribute]?.forEach { ranges.add(it.toDouble() .. it.toDouble()) }
                    maxGainNodeSplit = NodeSplit(attribute, ranges)
                }

            } else {
                //if attribute is continuous, we must find the best cut point
                possibleCutPoints(instances, attribute).forEach { cutPoint ->

                    val gain = infoGain(attribute, cutPoint, instances)

                    if (gain > maxGain) {
                        //update comparison gain
                        maxGain = gain
                        //generate ranges from cut point
                        maxGainNodeSplit = NodeSplit(attribute, listOf(0.0 .. cutPoint, cutPoint + Double.MIN_VALUE .. Double.MAX_VALUE))
                    }
                }
            }
        }
        return maxGainNodeSplit
    }

    private fun infoGain(attribute: Int, instances: List<Instance>): Double
            = info(instances) - infoCategoricalAttribute(attribute, instances)

    private fun infoGain(attribute: Int, cutPoint: Double, instances: List<Instance>): Double
            = info(instances) - infoContinuousAttribute(attribute, cutPoint, instances)

    private fun info(instances: List<Instance>): Double {
        val classesProbabilities = mutableMapOf<Int, Double>()
        instances.forEach { instance ->
            if (!classesProbabilities.containsKey(instance.targetAttributeInt)) {
                val clazz = instance.targetAttributeInt
                val classAppearances = instances.filter { it.targetAttributeInt == clazz }.size
                val classProbability = classAppearances / instances.size
                classesProbabilities.put(instance.targetAttributeInt, classProbability.toDouble())
            }
        }
        var sum = 0.0
        classesProbabilities.values.forEach { sum += it * log2(it) }
        return -sum
    }

    private fun infoContinuousAttribute(attribute: Int, cutPoint: Double, instances: List<Instance>): Double {
        //use ranges (based on the cut point) to represent the possible values for the attribute
        val ranges = listOf<ClosedRange<Double>>(0.0 .. cutPoint, cutPoint + Double.MIN_VALUE .. Double.MAX_VALUE)

        var sum = 0.0
        ranges.forEach { range ->
            val instancesWithTheValue = instances.filter { it.attributes[attribute] in range }
            sum += instancesWithTheValue.size * info(instancesWithTheValue)
        }
        return sum / instances.size
    }

    private fun infoCategoricalAttribute(attribute: Int, instances: List<Instance>): Double {
        var sum = 0.0
        categoricalAttributesValues[attribute]?.forEach { categoricalValue ->
            val instancesWithTheValue = instances.filter { it.attributes[attribute] == categoricalValue.toDouble() }
            sum += instancesWithTheValue.size * info(instancesWithTheValue)
        }
        return sum / instances.size
    }

    /**
     * Returns a list of cut point for a split with a continuous-value attribute
     */
    private fun possibleCutPoints(instances: List<Instance>, attribute: Int): List<Double> {
        val cutPoints = mutableListOf<Double>()

        //order instances by attribute value
        val orderedInstances: List<Instance> = instances.sortedBy { it.attributes[attribute] }
        orderedInstances.indices.forEach {
            //if there is a class change
            if (orderedInstances[it].targetAttributeInt != orderedInstances[it + 1].targetAttributeInt)
                //make a cut point between the attribute values
                cutPoints.add((orderedInstances[it].attributes[attribute]
                                + orderedInstances[it + 1].attributes[attribute]) / 2)
        }

        return cutPoints.toList()
    }
}

fun main(args: Array<String>) {
    val reader = DataReader("./data/dadosBenchmark_validacaoAlgoritmoAD.csv", 4, true)
    reader.dataSet.forEach { println(it) }
}

