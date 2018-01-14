import java.util.*

class Bootstrap(var instances: List<Instance>) {

    val trainingSet: MutableList<Instance> = mutableListOf()
    val testSet: MutableList<Instance> = mutableListOf()

    init {
        doBootstrapping()
    }

    private fun doBootstrapping() {
        val sizeOfSet = instances.size
        while (trainingSet.size < sizeOfSet) {
            val randomIndex = Random().nextInt((sizeOfSet))
            trainingSet.add(instances[randomIndex])
        }
        testSet.addAll(instances.filter { !trainingSet.contains(it) })
    }
}

fun main(args: Array<String>) {
    val dataReader = DataReader("./data/haberman.data")
    val bs = Bootstrap(dataReader.trainingDataSet)
    println(bs.trainingSet)
    println("ok")
}