import java.io.File
import java.util.*

class DataReader(file: String) {

    private val dataString: String = File(file).readText()
    val dataSet: MutableList<Instance> = mutableListOf()
    val trainingDataSet: MutableList<Instance> = mutableListOf()
    val testDataSet: MutableList<Instance> = mutableListOf()
    val categoricalAttributesValues: Map<Int, List<Double>>
    val categoricalAttributesMeanings: Map<Int, Map<String, Double>>


    companion object {

        /**
         * Constant for describing an ID column
         */
        public val IS_ID = 0

        /**
         * Constant for describing a numerical attribute column
         */
        public val IS_NUMERICAL = 1

        /**
         * Constant for describing a categorical attribute column
         */
        public val IS_CATEGORICAL = 2

        /**
         * Constant for describing target class column
         */
        public val IS_TARGET = 3

        /**
         * Pattern to parse in file identifying column as id
         */
        public val ID_PATTERN = "id"

        /**
         * Pattern to parse in file identifying column as numerical attribute
         */
        public val NUMERICAL_PATTERN = "numerical"

        /**
         * Pattern to parse in file identifying column as categorical attribute
         */
        public val CATEGORICAL_PATTERN = "categorical"

        /**
         * Pattern to parse in file identifying column as target class
         */
        public val TARGET_PATTERN = "target"
    }

    /**
     * Maps each column to a description (constants in companion object)
     */
    val columnDescriptor: MutableList<Int>

    init {
        val protoDataSet: MutableList<List<String>> = mutableListOf()

        columnDescriptor = buildColumnDescriptor().toMutableList()


        dataString.lines().forEachIndexed { index, line ->
            if (line != "" && index != 0) {
                val instanceAux = line.split(",", ";").toMutableList()

                //if the are ids, we discard it
                val idColumns = mutableListOf<Int>()
                instanceAux.indices.forEach { if (isIdPosition(it)) idColumns.add(it) }
                idColumns.forEach { instanceAux.removeAt(it) }

                protoDataSet.add(instanceAux)
            }
        }
        //after ids in instances are removed, we remove IS_ID entries in descriptor
        val idColumns = mutableListOf<Int>()
        columnDescriptor.indices.forEach { if (isIdPosition(it)) idColumns.add(it) }
        idColumns.forEach { columnDescriptor.removeAt(it) }
        println("\n\nCOLUMN DESCRIPTOR:") ; println(columnDescriptor)

        val standardizer = FeaturesStandardizer(getTargetPosition(), columnDescriptor)
        dataSet.addAll(standardizer.standardizeFeatures(protoDataSet))
        categoricalAttributesValues = standardizer.categoricalAttributesValues
        categoricalAttributesMeanings = standardizer.categoricalAttributesMeanings
        println("CATEGORICAL ATTRIBUTES VALUES:") ; println(categoricalAttributesValues)
        println("CATEGORICAL ATTRIBUTES MEANINGS:") ; println(categoricalAttributesMeanings)

        Collections.shuffle(dataSet)
        splitSetsToTrainAndTest()
    }

    /**
     * Uses patterns (constants in companion object) to build column descriptor
     */
    private fun buildColumnDescriptor(): List<Int> {
        val aColumnDescriptor = mutableListOf<Int>()
        dataString.lines()[0].split(",", ";").forEach { string ->
            when (string) {
                ID_PATTERN -> aColumnDescriptor.add(IS_ID)
                NUMERICAL_PATTERN -> aColumnDescriptor.add(IS_NUMERICAL)
                CATEGORICAL_PATTERN -> aColumnDescriptor.add(IS_CATEGORICAL)
                TARGET_PATTERN -> aColumnDescriptor.add(IS_TARGET)
            }
        }
        return aColumnDescriptor
    }

    private fun getTargetPosition(): Int {
        var position = 0
        columnDescriptor.forEachIndexed { index, description ->
            if (description == IS_TARGET) position = index
        }
        return position
    }

    private fun isIdPosition(position: Int): Boolean {
        return columnDescriptor[position] == IS_ID
    }

    private fun splitSetsToTrainAndTest() {
        val numberOfSets: Int = dataSet.count()
        val numberOfTrainingSets: Int = Math.floor(numberOfSets * 0.8).toInt()
        for (i in 1..numberOfSets) {
            if (i - 1 < numberOfTrainingSets) {
                trainingDataSet.add(dataSet[i - 1])
            } else {
                testDataSet.add(dataSet[i - 1])
            }
        }
    }
}

fun main(args: Array<String>) {
    val dataReader: DataReader = DataReader("./data/haberman.data")
    println(dataReader)
}