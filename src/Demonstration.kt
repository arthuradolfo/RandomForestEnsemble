fun main(args: Array<String>) {
    val reader = DataReader("./data/dadosBenchmark_validacaoAlgoritmoAD.csv")
    println("\n~~~~~~ DATA ~~~~~~")
    reader.dataSet.forEach { println(it) }
    println("~~~~~~~~~~~~~~~~~~")

    val planter = Planter(reader.dataSet, reader.categoricalAttributesValues)
    println("\n~~~~~~ TREE ~~~~~~")
    val tree = planter.plantTree()
    println(tree)
    println("~~~~~~~~~~~~~~~~~~")

    println("\n~~~~~~ PREDICTIONS ~~~~~~")
    reader.dataSet.forEach {
        println("Decision on $it is ${tree.decide(it)}")
    }
    println("~~~~~~~~~~~~~~~~~~~~~~~~~~~")
}
