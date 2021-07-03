package online.senpai.schedbot

class TrieNode<K, V>(var key: K?, var parent: TrieNode<K, V>?, var payload: V?) {
    val children: MutableMap<K, TrieNode<K, V>> = mutableMapOf()
    val hasChildren: Boolean
        get() = children.isNotEmpty()
    val isLeaf: Boolean
        get() = payload != null

    override fun toString(): String {
        return "TrieNode{key=$key, parent=$parent, payload=$payload, children=$children, isLeaf=$isLeaf}"
    }
}
