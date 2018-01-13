import java.util.*

class Bootstrap(var instances: MutableList<Instance>) {

    val bootstrapSet: MutableList<Instance> = mutableListOf()

    init {
        doBootstrapping()
    }

    private fun doBootstrapping() {
        val sizeOfSet = instances.size
        println("bootstrap $sizeOfSet")
        while (bootstrapSet.size < sizeOfSet) {
            val randomIndex = Random().nextInt((sizeOfSet))
            bootstrapSet.add(instances[randomIndex])
        }
    }
}

fun main(args: Array<String>) {
    val dataReader = DataReader("./data/haberman.data")
    val bs = Bootstrap(dataReader.trainingDataSet)
    println(bs.bootstrapSet)
    println("ok")
}