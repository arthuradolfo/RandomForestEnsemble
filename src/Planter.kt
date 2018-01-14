import java.util.*
import kotlin.math.log2

class Planter(
        private val instances: List<Instance>,
        private val categoricalAttributesValues: Map<Int, List<Double>>,
        private val mAttributes: Int = instances.first().attributes.size
) {

    fun plantTree(): DecisionTree {
        //println("\n%%%%% Building Tree ... %%%%%\n")
        val tree = recursivelyBuildTree(instances, instances.possibleAttributes().randomSubset(mAttributes).toMutableList())
        tree.commonAttributeRange = 0.0..0.0
        //println("\n%%%%%%%% Built Tree %%%%%%%%%\n")
        return tree
    }

    private fun recursivelyBuildTree(instances: List<Instance>, possibleAttributes: MutableList<Int>): DecisionTree {
        //println("recursivelyBuildTree with instances $instances")

        when {
            instances.allAreSameClass() -> {
                //println("All instances are of same class! $instances")
                return DecisionLeaf(instances.first().targetAttributeInt)
            }
            possibleAttributes.isEmpty() -> {
                //println("No more possible attributes!")
                return DecisionLeaf(instances.majorityClass())
            }
        }

        val nodeSplit = nodeSplitID3(instances, possibleAttributes)
        possibleAttributes.remove(nodeSplit.attribute)

        val decisionTree = TestNode(nodeSplit.attribute, mutableListOf())

        (0 until nodeSplit.ranges.size).forEach { branchIndex ->
            val instancesSubset = instances.filter { it.attributes[nodeSplit.attribute] in nodeSplit.ranges[branchIndex] }
            if (instancesSubset.isEmpty()) {
                val leaf = DecisionLeaf(instances.majorityClass())
                leaf.commonAttributeRange = nodeSplit.ranges[branchIndex]
                decisionTree.branches.add(branchIndex, leaf)
            }
            else {
                //println("\n\n!!!!!Recursive call!!!!!")
                val recursiveBranch = recursivelyBuildTree(instancesSubset, possibleAttributes)
                recursiveBranch.commonAttributeRange = nodeSplit.ranges[branchIndex]
                decisionTree.branches.add(branchIndex, recursiveBranch)
            }
        }
        if (decisionTree.branches.isEmpty()) {
            println("ERRO !")
            println(nodeSplit)
            println(instances)
            println(possibleAttributes)
            println(categoricalAttributesValues)
            println("\n")
        }

        return decisionTree
    }

    private fun List<Instance>.possibleAttributes(): List<Int> = (0 until this.first().attributes.size).toList()

    private fun List<Instance>.majorityClass(): Int {
        val classes = mutableMapOf<Int, Int>()
        this.forEach { instance ->
            if (classes.containsKey(instance.targetAttributeInt) && classes[instance.targetAttributeInt] != null) {
                val update = classes[instance.targetAttributeInt]!! + 1
                classes.replace(instance.targetAttributeInt, update)
            } else classes.put(instance.targetAttributeInt, 1)
        }
        return classes.entries.sortedBy { it.value }.last().key
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
        var maxGain = Double.NEGATIVE_INFINITY
        var maxGainNodeSplit = NodeSplit(0, emptyList())

        //println("\n\n ---- POSSIBLE ATTRIBUTES $possibleAttributes ----")
        possibleAttributes.forEach { attribute ->
            //check if attribute is categorical or continuous
            if (categoricalAttributesValues.containsKey(attribute)) {

                //println("\n\n ---- CATEGORICAL ATTRIBUTE $attribute ----")
                val gain = infoGain(attribute, instances)
                //println("Gain = $gain")

                if (gain >= maxGain) {
                    //update comparison gain
                    maxGain = gain
                    //generate ranges from possible values of attribute
                    val ranges = mutableListOf<ClosedRange<Double>>()
                    categoricalAttributesValues[attribute]?.forEach { ranges.add(it..it) }
                    maxGainNodeSplit = NodeSplit(attribute, ranges)
                }

            } else {
                //if attribute is continuous, we must find the best cut point
                possibleCutPoints(instances, attribute).forEach { cutPoint ->

                    val gain = infoGain(attribute, cutPoint, instances)

                    if (gain >= maxGain) {
                        //update comparison gain
                        maxGain = gain
                        //generate ranges from cut point
                        maxGainNodeSplit = NodeSplit(attribute, listOf(0.0..cutPoint, cutPoint + Double.MIN_VALUE..Double.MAX_VALUE))
                    }
                }
            }
        }
        //println("Max gain is = $maxGain for $maxGainNodeSplit")
        return maxGainNodeSplit
    }

    private fun infoGain(attribute: Int, instances: List<Instance>): Double
            = info(instances) - infoCategoricalAttribute(attribute, instances)

    private fun infoGain(attribute: Int, cutPoint: Double, instances: List<Instance>): Double
            = info(instances) - infoContinuousAttribute(attribute, cutPoint, instances)

    private fun info(instances: List<Instance>): Double {
        //println("\n*** INFO CALL ***")
        val classesProbabilities = mutableMapOf<Int, Double>()
        instances.forEach { instance ->
            if (!classesProbabilities.containsKey(instance.targetAttributeInt)) {
                val clazz = instance.targetAttributeInt
                val classAppearances = instances.filter { it.targetAttributeInt == clazz }.size
                val classProbability = classAppearances / instances.size.toDouble()
                classesProbabilities.put(instance.targetAttributeInt, classProbability)
                //println("Class $clazz probability is $classAppearances / ${ instances.size} = $classProbability")
            }
        }
        var sum = 0.0
        classesProbabilities.values.forEach { sum += it * log2(it) }

        //println("Info is ${-sum}")

        return -sum
    }

    private fun infoContinuousAttribute(attribute: Int, cutPoint: Double, instances: List<Instance>): Double {
        //println("\n*** INFO CONTINUOUS ATTRIBUTE CALL ***")
        //use ranges (based on the cut point) to represent the possible values for the attribute
        val ranges = listOf<ClosedRange<Double>>(0.0..cutPoint, cutPoint + Double.MIN_VALUE..Double.MAX_VALUE)

        var sum = 0.0
        ranges.forEach { range ->
            val instancesWithTheValue = instances.filter { it.attributes[attribute] in range }
            sum += instancesWithTheValue.size * info(instancesWithTheValue)
        }

        //println("Info of continuous attribute is ${sum / instances.size}")

        return sum / instances.size
    }

    private fun infoCategoricalAttribute(attribute: Int, instances: List<Instance>): Double {
        //println("\n*** INFO CATEGORICAL ATTRIBUTE CALL ***")
        var sum = 0.0
        categoricalAttributesValues[attribute]?.forEach { categoricalValue ->
            val instancesWithTheValue = instances.filter { it.attributes[attribute] == categoricalValue }
            sum += instancesWithTheValue.size * info(instancesWithTheValue)
        }
        //println("\n*** INFO CATEGORICAL ATTRIBUTE END ***")
        //println("Info of categorical attribute $attribute is ${sum / instances.size}")

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
            if (it != orderedInstances.lastIndex && orderedInstances[it].targetAttributeInt != orderedInstances[it + 1].targetAttributeInt)
            //make a cut point between the attribute values
                cutPoints.add((orderedInstances[it].attributes[attribute]
                        + orderedInstances[it + 1].attributes[attribute]) / 2)
        }

        return cutPoints.toList()
    }
}

fun main(args: Array<String>) {
    val reader = DataReader("./data/haberman.data")
    //val reader = DataReader("./data/dadosBenchmark_validacaoAlgoritmoAD.csv")
    //reader.dataSet.forEach { println(it) }

    val planter = Planter(reader.dataSet, reader.categoricalAttributesValues)
    val tree = planter.plantTree()
    println(tree)
    reader.dataSet.forEach {
        println("Decision on $it is ${tree.decide(it)}")
    }
}

