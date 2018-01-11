import java.util.*

class Bootstrap(var instances: MutableList<Instance>) {

    val bootstrap_set    :   MutableList<Instance> = mutableListOf()

    init {
        doBootstraping()
    }

    private fun doBootstraping() {
        val size_of_set = instances.size
        println(size_of_set)
        while (bootstrap_set.size < size_of_set) {
            val random_index = Random().nextInt((size_of_set))
            println(random_index)
            bootstrap_set.add(instances[random_index])
        }
    }
}

fun main(args : Array<String>) {
    val dataReader = DataReader("./data/haberman.data", 3, true)
    val bs = Bootstrap(dataReader.trainingDataSet)
    println(bs.bootstrap_set)
    println("ok")
}