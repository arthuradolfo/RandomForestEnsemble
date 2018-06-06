package bilbo.randomforestensemble

class BilboBenchmark {
    fun run() {
        var fileName = String()
        var config = BilboConfig(0, 0)

        for (i in 1..4) {
            when (i) { //choose file and configuration
                1 -> {
                    fileName = "wine.data"
                    config = BilboConfig(m = 13, nTree = 40)
                }
                2 -> {
                    fileName = "haberman.data"
                    config = BilboConfig(m = 3, nTree = 28)
                }
                3 -> {
                    fileName = "cmc.data"
                    config = BilboConfig(m = 8, nTree = 21)
                }
                4 -> {
                    fileName = "wdbc.data"
                    config = BilboConfig(m = 29, nTree = 15)
                }
            }


            //run selected configuration
            val dataReader = DataReader(fileName)
            val bilbo = Bilbo(
                    nTree = config.nTree,
                    instances = dataReader.trainingDataSet,
                    categoricalAttributesValues = dataReader.categoricalAttributesValues,
                    mAttributes = config.m

            )
            val results = bilbo.testTrees(dataReader.testDataSet)

            println(fileName)
            println(results)
        }
    }
}

fun main(args: Array<String>) {
    val benchmark = BilboBenchmark()
    benchmark.run()
}