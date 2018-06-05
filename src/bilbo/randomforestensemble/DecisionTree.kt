package bilbo.randomforestensemble

sealed class DecisionTree {

    var commonAttributeRange: ClosedRange<Double> = 0.0..0.0 + Double.MIN_VALUE

    abstract fun decide(instance: Instance): Int
}

data class DecisionLeaf(val decision: Int) : DecisionTree() {
    override fun decide(instance: Instance): Int = decision

    override fun toString(): String {
        return "bilbo.randomforestensemble.DecisionLeaf(decision=$decision, commonAttributeRange=$commonAttributeRange)"
    }
}

data class TestNode(val testAttribute: Int, var branches: MutableList<DecisionTree>) : DecisionTree() {
    override fun decide(instance: Instance) = branch(instance, branches, testAttribute).decide(instance)

    private fun branch(instance: Instance, branches: MutableList<DecisionTree>, attribute: Int): DecisionTree {
        val matches = mutableListOf<Int>()
        (0..branches.lastIndex)
                .filter { instance.attributes[attribute] in branches[it].commonAttributeRange }
                .forEach { matches.add(it) }

        when (matches.size) {
            0 -> throw RuntimeException("No possible branches")
            else -> return branches[matches.first()]
        }
    }

    override fun toString(): String {
        return "bilbo.randomforestensemble.TestNode(testAttribute=$testAttribute, commonAttributeRange=$commonAttributeRange, branches=$branches)"
    }
}


