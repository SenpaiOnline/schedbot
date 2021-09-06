package online.senpai.schedbot

class Trie<K, V> {
    private val root = TrieNode<K, V>(null, null, null)

    fun put(iterable: Iterable<K>, payload: V) {
        var current: TrieNode<K, V> = root

        iterable.forEach { element: K ->
            current = current.children.getOrPut(element) { TrieNode(element, current, payload) }
        }
    }

    fun put(vararg elements: K, payload: V): Unit = put(elements.toList(), payload)

    fun get(iterable: Iterable<K>): V? {
        var current: TrieNode<K, V> = root

        iterable.forEach { element: K ->
            val child: TrieNode<K, V> = current.children[element] ?: return null
            current = child
        }
        return current.payload
    }

    fun get(vararg elements: K): V? = get(elements.toList())

    fun remove(iterable: Iterable<K>) {
        var current: TrieNode<K, V> = root

        iterable.forEach { element: K ->
            val child: TrieNode<K, V> = current.children[element] ?: return
            current = child
        }
        if (!current.isLeaf) return
        current.payload = null

        val parent: TrieNode<K, V>? = current.parent
        while (!current.hasChildren && !current.isLeaf) {
            parent?.let { existingParent: TrieNode<K, V> ->
                existingParent.children.remove(current.key)
                current = existingParent
            }
        }
    }

    fun remove(vararg elements: K): Unit = remove(elements.toList())
}
