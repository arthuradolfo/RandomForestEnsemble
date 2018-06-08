package bilbo.randomforestensemble

class BilboBenchmark(private val k: Int, private val datasetPercentage: DatasetPercentage) {
    fun run() {
        var fileName = String()
        var config = BilboConfig(0, 0)

        for (i in 1..4) {
            //choose file suffix based on data set percentage
            val fileSuffix = when (datasetPercentage) {
                DatasetPercentage.PCT100 -> "_100pct.data"
                DatasetPercentage.PCT75 -> "_75pct.data"
                DatasetPercentage.PCT50 -> "_50pct.data"
                DatasetPercentage.PCT25 -> "_25pct.data"
            }

            when (i) { //choose dataset and forest configuration
                1 -> {
                    fileName = "wine/wine$fileSuffix"
                    config = BilboConfig(m = 13, nTree = 40)
                }
                2 -> {
                    fileName = "haberman/haberman$fileSuffix"
                    config = BilboConfig(m = 3, nTree = 28)
                }
                3 -> {
                    fileName = "cmc/cmc$fileSuffix"
                    config = BilboConfig(m = 8, nTree = 21)
                }
                4 -> {
                    fileName = "wdbc/wdbc$fileSuffix"
                    config = BilboConfig(m = 29, nTree = 15)
                }
            }

            //run selected configuration
            val dr = DataReader(fileName)
            val folding = Folding(dr.trainingDataSet, k)

            val metricsList = mutableListOf<Metrics>()
            for (testFold in 0 until k) {

                val trainingSet = folding.fuseTrainingFolds(testFold)

                val bilbo = Bilbo(
                        nTree = config.nTree,
                        instances = trainingSet,
                        categoricalAttributesValues = dr.categoricalAttributesValues,
                        mAttributes = config.m

                )

                metricsList.add(calculateMetrics(bilbo.testTrees(folding.folds[testFold].dataSet)))
            }
            //take mean and std dev of metrics
            val meanMetrics = calculateMeanMetrics(metricsList.toList())

            println("$fileName { m = ${config.m}, nTree = ${config.nTree} } -> Acc = ${meanMetrics.accuracy}")
        }
    }

    private fun Folding.fuseTrainingFolds(testFold: Int): List<Instance> {
        //add instances from training folds to one big list of instances called trainingInstances
        val trainingFolds = this.folds.filterIndexed { index, _ -> index != testFold }
        val trainingInstances = mutableListOf<Instance>()
        trainingFolds.forEach { it.dataSet.forEach { trainingInstances.add(it) } }
        return trainingInstances
    }

    private fun calculateMetrics(votes: List<Pair<Int, Int>>): Metrics {
        val correctVotes = votes.filter { it.first == it.second }.size
        val accuracy = correctVotes / votes.size.toDouble()
        return Metrics(accuracy, 0.0)
    }

    private fun calculateMeanMetrics(metricsList: List<Metrics>): Metrics {
        var meanAccuracy = 0.0
        //var standardDeviationAccuracy = 0.0

        metricsList.forEach {
            meanAccuracy += it.accuracy
        }
        meanAccuracy /= metricsList.size

        /*metricsList.forEach {
            standardDeviationAccuracy += Math.pow((it.accuracy - meanAccuracy), 2.0) / (metricsList.size - 1)
        }

        standardDeviationAccuracy = Math.sqrt(standardDeviationAccuracy)*/

        return Metrics(
                meanAccuracy,
                0.0
        )
    }
}

enum class DatasetPercentage {
    PCT100, PCT75, PCT50, PCT25
}

fun main(args: Array<String>) {
    val benchmark = BilboBenchmark(k = 10, datasetPercentage = DatasetPercentage.PCT100)
    benchmark.run()
}