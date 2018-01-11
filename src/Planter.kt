class Planter(var instances: List<Instance>, var mAttribs: Int, var branchesPerNode: Int) {

    fun plantTree(): DecisionTree = recursivelyBuildTree(
            instances,
            instances.possibleAttributes().randomSubset(mAttribs).toMutableList()
    )

    private fun recursivelyBuildTree(instances: List<Instance>, possibleAttributes: MutableList<Int>): DecisionTree {
        when {
            instances.allAreSameClass() -> return DecisionLeaf(instances.first().targetAttributeInt)
            possibleAttributes.isEmpty() -> return DecisionLeaf(instances.majorityClass())
        }

        val attribute = selectAttributeID3(possibleAttributes)
        possibleAttributes.remove(attribute)

        val decisionTree = TestNode(attribute, mutableListOf())

        val ranges = branchesRanges(instances, attribute)
        (0 until branchesPerNode).forEach { branchIndex ->
            val instancesSubset = instances.filter { it.targetAttributeInt in ranges[branchIndex] }
            decisionTree.branches.add(branchIndex, recursivelyBuildTree(instancesSubset, possibleAttributes))
        }

        return decisionTree

        /*
        val branches = makeBranches()
        val tree = TestNode(attribute, branches)
        */
    }

    private fun branchesRanges(instances: List<Instance>, attribute: Int): List<ClosedRange<Double>>
            = TODO("Returns a list of ranges of values for an attribute, of size branchesPerNode")

    private fun List<Instance>.possibleAttributes(): List<Int>
            = TODO("Returns a list of all attributes (just need to check one element/instance)" +
            "They should be numbered up from 0. Essentially attributes.size")

    private fun List<Instance>.majorityClass(): Int
            = TODO("Return the targetAttributeInt that shows up the most times")

    private fun List<Instance>.allAreSameClass(): Boolean
            = TODO("Verify if, for every element, it.targetAttribute is the same")

    private fun List<Int>.randomSubset(size: Int): List<Int>
            = TODO("Take a random subset of provided size")

    private fun selectAttributeID3(possibleAttributes: List<Int>): Int
            = TODO("Use entropy and information gain")
}


