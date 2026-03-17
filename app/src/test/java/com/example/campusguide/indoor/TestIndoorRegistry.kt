package com.example.campusguide.indoor

internal object TestIndoorRegistry {
    @Suppress("UNCHECKED_CAST")
    private fun mutableRegistry(): MutableMap<Pair<String, Int>, IndoorFloorGraph> {
        val field = IndoorGraphRegistry::class.java.getDeclaredField("registry")
        field.isAccessible = true
        return field.get(IndoorGraphRegistry) as MutableMap<Pair<String, Int>, IndoorFloorGraph>
    }

    fun seed(vararg graphs: IndoorFloorGraph) {
        val registry = mutableRegistry()
        registry.clear()
        graphs.forEach { graph ->
            registry[graph.buildingCode.uppercase() to graph.floor] = graph
        }
    }

    fun clear() {
        mutableRegistry().clear()
    }
}
