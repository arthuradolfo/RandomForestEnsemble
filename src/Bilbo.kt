class Bilbo(var ntree : Int, var mattributes : Int, var instances: MutableList<Instance>) {
    val bootstrap_sets : MutableList<Bootstrap> = mutableListOf()

    init {
        generateBootstraps()
    }

    private fun generateBootstraps() {
        for(number_of_sets in 1..ntree) {
            bootstrap_sets.add(Bootstrap(instances))
        }
    }
}

fun main(args : Array<String>) {
    val dataReader = DataReader("./data/haberman.data", 3, true)
    val bilbo = Bilbo(10, 5, dataReader.trainingDataSet)
    println(bilbo.bootstrap_sets[0].bootstrap_set)
    println(bilbo.bootstrap_sets[1].bootstrap_set)
    println(bilbo.bootstrap_sets[2].bootstrap_set)
    println("ok")
}