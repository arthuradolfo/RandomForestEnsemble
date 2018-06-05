package bilbo.randomforestensemble

class Bilbo(
        var nTree: Int,
        var instances: List<Instance>,
        var categoricalAttributesValues: Map<Int, List<Double>>,
        var mAttributes: Int = instances.first().attributes.size
) {
    val bootstrapSets: MutableList<Bootstrap> = mutableListOf()
    val decisionTrees: MutableList<DecisionTree> = mutableListOf()

    init {
        generateBootstraps()
        generateDecisionTrees()
    }

    private fun generateDecisionTrees() {
        for (i in 0 until nTree) {
            val planter = Planter(bootstrapSets[i].trainingSet, categoricalAttributesValues, mAttributes)
            decisionTrees.add(planter.plantTree())
        }
    }

    private fun generateBootstraps() {
        for (number_of_sets in 1..nTree) {
            bootstrapSets.add(Bootstrap(instances))
        }
    }

    public fun testTrees(testInstances: List<Instance>): List<Pair<Int, Int>>{
        val votes = mutableListOf<Pair<Int, Int>>()
        testInstances.forEach {
            //save a list of expected and predicted classes
            val answersForATree = mutableListOf<Int>()
            decisionTrees.forEach { decisionTree ->
                answersForATree.add(decisionTree.decide(it))
            }
            votes.add(Pair(it.targetAttributeInt, answersForATree.voteDecision()))
        }
        //println(votes)
        return votes
    }

    private fun MutableList<Int>.voteDecision(): Int {
        val classes = mutableMapOf<Int, Int>()
        this.forEach { answer ->
            if (classes.containsKey(answer)) {
                val update = classes.get(key = answer)!! + 1
                classes.replace(answer, update)
            } else classes.put(answer, 1)
        }
        return classes.entries.sortedBy { it.value }.last().key
    }
}

fun main(args: Array<String>) {
    val dataReader = DataReader("./data/dadosBenchmark_validacaoAlgoritmoAD.csv")
    val bilbo = Bilbo(10, dataReader.trainingDataSet, dataReader.categoricalAttributesValues)

    println(bilbo.decisionTrees)
    bilbo.testTrees(dataReader.testDataSet)
    println("ok")
}