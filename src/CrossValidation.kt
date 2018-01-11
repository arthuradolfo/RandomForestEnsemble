class CrossValidation (dataFile : String, k : Int, targetPosition : Int, hasId : Boolean) {

    private val dr = DataReader(dataFile, targetPosition, hasId)
    private val folding = Folding(dr.trainingDataSet, k)
    private val MAX_BEST_CONFIGS_LIST_SIZE = 10

    fun doCrossValidation() {

    }

    private fun calculateMetrics(folding: Folding, testFold: Int): MetricsA {
        return MetricsA(0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0)
    }

    private fun calculateMeanMetrics(metricsList: List<MetricsA>): MetricsA {
        var meanJ = 0.0
        var meanAccuracy = 0.0
        var meanPrecision = 0.0
        var meanRecall = 0.0
        var standardDeviationJ = 0.0
        var standardDeviationAccuracy = 0.0
        var standardDeviationPrecision = 0.0
        var standardDeviationRecall = 0.0

        metricsList.forEach {
            meanJ += it.j/metricsList.size
            meanAccuracy += it.accuracy/metricsList.size
            meanPrecision += it.precision/metricsList.size
            meanRecall += it.recall/metricsList.size
        }

        metricsList.forEach {
            standardDeviationJ += Math.pow((it.j-meanJ), 2.0)/(metricsList.size-1)
            standardDeviationAccuracy += Math.pow((it.accuracy-meanAccuracy), 2.0)/(metricsList.size-1)
            standardDeviationPrecision += Math.pow((it.precision-meanPrecision), 2.0)/(metricsList.size-1)
            standardDeviationRecall += Math.pow((it.recall-meanRecall), 2.0)/(metricsList.size-1)
        }

        standardDeviationJ = Math.sqrt(standardDeviationJ)
        standardDeviationAccuracy = Math.sqrt(standardDeviationAccuracy)
        standardDeviationPrecision = Math.sqrt(standardDeviationPrecision)
        standardDeviationRecall = Math.sqrt(standardDeviationRecall)

        return MetricsA(
                meanJ,
                meanAccuracy,
                meanPrecision,
                meanRecall,
                standardDeviationJ,
                standardDeviationAccuracy,
                standardDeviationPrecision,
                standardDeviationRecall
        )
    }

    private fun saveIfGoodConfig() {
    }
}

fun main(args : Array<String>) {
    val cv = CrossValidation("./data/cmc.data", 10, 9, false)
    cv.doCrossValidation()
    println("ok")
}