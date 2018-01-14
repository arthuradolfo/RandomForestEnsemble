class CrossValidation(dataFile: String, val k: Int) {

    val dr = DataReader(dataFile)
    private val folding = Folding(dr.trainingDataSet, k)
    private var bestConfigs = mutableListOf<Pair<BilboConfig, Metrics>>()
    private val MAX_BEST_CONFIGS_LIST_SIZE = 10

    fun doCrossValidation_FindBestConfigs() {
        for (m in 1..dr.columnDescriptor.filter { it != DataReader.IS_TARGET && it != DataReader.IS_ID }.size) {
            for (nTree in 1..20) {
                println("\n\n//////////////////// CONFIG ////////////////////")
                println("m = $m | nTree = $nTree")

                val metricsList = mutableListOf<Metrics>()
                for (testFold in 0 until k) {

                    val trainingSet = fuseTrainingFolds(testFold)

                    val bilbo = Bilbo(nTree, trainingSet, dr.categoricalAttributesValues, m)

                    println(bilbo.decisionTrees.first())
                    metricsList.add(calculateMetrics(bilbo.testTrees(folding.folds[testFold].dataSet)))
                }
                //take mean and std dev of metrics
                val meanMetrics = calculateMeanMetrics(metricsList.toList())

                println("Mean accuracy = ${meanMetrics.accuracy}")
                println("Stadard Deviation accuracy = ${meanMetrics.standardDeviationAccuracy}")

                saveIfGoodConfig(BilboConfig(m, nTree), meanMetrics)
            }
        }
        println("\n\n!!!!!!!!!! BEST CONFIGS (${bestConfigs.size}) !!!!!!!!!!")
        println(bestConfigs.joinToString(separator = "\n"))
    }

    fun doCrossValidation_GetAllAccuracies(): List<Pair<BilboConfig, Double>> {
        val forestsAccuracies = mutableListOf<Pair<BilboConfig, Double>>()

        for (m in 1..dr.columnDescriptor.filter { it != DataReader.IS_TARGET && it != DataReader.IS_ID }.size) {
            for (nTree in 1..200) {

                val bilbo = Bilbo(
                        nTree = nTree,
                        instances = dr.trainingDataSet,
                        categoricalAttributesValues = dr.categoricalAttributesValues,
                        mAttributes = m
                )
                println("Bilbo has ${bilbo.decisionTrees.size} trees")

                val accuracy = calculateMetrics(bilbo.testTrees(dr.testDataSet)).accuracy
                forestsAccuracies.add(
                        Pair(BilboConfig(m = m, nTree = nTree), accuracy)
                )
                println("Forest Accuracy = ${accuracy}")
            }
        }
        return forestsAccuracies.toList()
    }

    private fun fuseTrainingFolds(testFold: Int): List<Instance> {
        //add instances from training folds to one big list of instances called trainingInstances
        val trainingFolds = folding.folds.filterIndexed { index, _ -> index != testFold }
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
        var standardDeviationAccuracy = 0.0

        metricsList.forEach {
            meanAccuracy += it.accuracy / metricsList.size
        }

        metricsList.forEach {
            standardDeviationAccuracy += Math.pow((it.accuracy - meanAccuracy), 2.0) / (metricsList.size - 1)
        }

        standardDeviationAccuracy = Math.sqrt(standardDeviationAccuracy)

        return Metrics(
                meanAccuracy,
                standardDeviationAccuracy
        )
    }

    private fun saveIfGoodConfig(config: BilboConfig, metrics: Metrics) {
        if (bestConfigs.size < MAX_BEST_CONFIGS_LIST_SIZE) { //list of best configs has space
            bestConfigs.add(Pair(config, metrics))

        } else { //list is full

            //find worst config of the best configs
            var worstConfigIndex = 0
            bestConfigs.forEachIndexed { index, pair ->
                if (pair.second.accuracy < bestConfigs[worstConfigIndex].second.accuracy)
                    worstConfigIndex = index
            }

            //evaluate if should replace worst config for new one
            if (metrics.accuracy > bestConfigs[worstConfigIndex].second.accuracy)
                bestConfigs[worstConfigIndex] = Pair(config, metrics)
        }
    }
}

fun main(args: Array<String>) {
    val cv = CrossValidation("./data/wdbc.data", 10)
    cv.doCrossValidation_FindBestConfigs()
    println("ok")
}