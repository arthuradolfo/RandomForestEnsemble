class Bilbo(var nTree: Int, var mAttributes: Int, var instances: MutableList<Instance>, var categoricalAttributesValues: Map<Int, List<Double>>) {
    val bootstrapSets: MutableList<Bootstrap> = mutableListOf()
    val decisionTrees: MutableList<DecisionTree> = mutableListOf()

    init {
        generateBootstraps()
        println(bootstrapSets[2].bootstrapSet.size)
        generateDecisionTrees()
    }

    private fun generateDecisionTrees() {
        val planter = Planter(instances, categoricalAttributesValues, mAttributes)
        for (number_of_sets in 1..nTree) {
            decisionTrees.add(planter.plantTree())
        }
    }

    private fun generateBootstraps() {
        for (number_of_sets in 1..nTree) {
            bootstrapSets.add(Bootstrap(instances))
        }
    }

    public fun testTrees() {
        val answers = mutableListOf<Int>()
        (0..instances.size).forEach {
            i -> decisionTrees.forEachIndexed {
                index, decisionTree -> answers.add(decisionTree.decide(bootstrapSets[index].instances[i]))
            }
            println(answers.voteDecision())
        }
    }

    private fun MutableList<Int>.voteDecision() : Int {
        val classes = mutableMapOf<Int, Int>()
        this.forEach { answer ->
            if (classes.containsKey(answer)) {
                val update = classes.get(key = answer)!! + 1
                classes.replace(answer, update)
            } else classes.put(answer, 1)
        }
        return classes.values.max()!!
    }
}

fun main(args: Array<String>) {
    val dataReader = DataReader("./data/haberman.data")
    val bilbo = Bilbo(10, 3, dataReader.trainingDataSet, dataReader.categoricalAttributesValues)
    println(bilbo.bootstrapSets[0].bootstrapSet)
    println(bilbo.bootstrapSets[1].bootstrapSet)
    println(bilbo.bootstrapSets[2].bootstrapSet)
    println(bilbo.bootstrapSets[2].bootstrapSet.size)
    println(bilbo.decisionTrees)
    println(bilbo.testTrees()
    )
    println("ok")
}