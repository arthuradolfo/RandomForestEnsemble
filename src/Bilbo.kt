class Bilbo(var ntree: Int, var mattributes: Int, var instances: MutableList<Instance>, var categoricalAttributesValues: Map<Int, List<Double>>) {
    val bootstrap_sets: MutableList<Bootstrap> = mutableListOf()
    val decision_trees: MutableList<DecisionTree> = mutableListOf()

    init {
        generateBootstraps()
        println(bootstrap_sets[2].bootstrapSet.size)
        generateDecisionTrees()
    }

    private fun generateDecisionTrees() {
        var planter = Planter(instances, categoricalAttributesValues, mattributes)
        for (number_of_sets in 1..ntree) {
            decision_trees.add(planter.plantTree())
        }
    }

    private fun generateBootstraps() {
        for (number_of_sets in 1..ntree) {
            bootstrap_sets.add(Bootstrap(instances))
        }
    }

    public fun testTrees() {
        val answers = mutableListOf<Int>()
        (0..instances.size).forEach {
            i -> decision_trees.forEachIndexed {
                index, decisionTree -> answers.add(decisionTree.decide(bootstrap_sets[index].instances[i]))
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
    println(bilbo.bootstrap_sets[0].bootstrapSet)
    println(bilbo.bootstrap_sets[1].bootstrapSet)
    println(bilbo.bootstrap_sets[2].bootstrapSet)
    println(bilbo.bootstrap_sets[2].bootstrapSet.size)
    println(bilbo.decision_trees)
    println(bilbo.testTrees()
    )
    println("ok")
}