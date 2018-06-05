package bilbo.randomforestensemble

data class NodeSplit(val attribute: Int, val ranges: List<ClosedRange<Double>>)