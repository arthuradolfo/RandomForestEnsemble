package bilbo.randomforestensemble

fun main(args: Array<String>) {
    val cv = CrossValidation("./data/wine.data", 10)
    val accuracies = cv.doCrossValidation_GetAllAccuracies()
    val bigListOfAccuracies = mutableListOf<Double>()
    val bigNTreeSequence = mutableListOf<Double>()

    for (m in 1..cv.dr.columnDescriptor.filter { it != DataReader.IS_TARGET && it != DataReader.IS_ID }.size) {
        val accuraciesSequence = mutableListOf<Double>()
        val nTreeSequence = mutableListOf<Double>()

        accuracies.filter { it.first.m == m }.forEach {
            accuraciesSequence.add(it.second)
            nTreeSequence.add(it.first.nTree.toDouble())
        }

        println(nTreeSequence.toDoubleArray())
        println(accuraciesSequence.toDoubleArray())
        val plot = Plot(nTreeSequence.toDoubleArray(), accuraciesSequence.toDoubleArray(), "m = $m")
        plot.show()

        bigListOfAccuracies.addAll(accuraciesSequence)
        bigNTreeSequence.addAll(nTreeSequence)
    }
    val x = mutableListOf<Double>()
    bigListOfAccuracies.indices.forEach {
        x.add(it.toDouble())
    }
    val plot = Plot(x.toDoubleArray(), bigListOfAccuracies.toDoubleArray(), "All configs")
    plot.show()

}