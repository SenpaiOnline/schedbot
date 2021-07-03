package online.senpai.schedbot

import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull

class TrieTests : ExpectSpec({
    isolationMode = IsolationMode.InstancePerTest

    val trie: Trie<String, String> = Trie<String, String>().apply {
        put("the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog", payload = "brown")
        put("the", "quick", "gray", "fox", "jumps", "over", "the", "lazy", "dog", payload = "gray")
        put("the", "quick", "fennec", "fox", "jumps", "over", "the", "lazy", "dog", payload = "fennec")
        put("the", "quick", "swift", "fox", "jumps", "over", "the", "lazy", "dog", payload = "swift")
    }

    expect("a trie to return correct nodes by their addresses") {
        trie.run {
            get("the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog")
                ?.shouldBeEqualComparingTo("brown")
            get("the", "quick", "fennec", "fox", "jumps", "over", "the", "lazy", "dog")
                ?.shouldBeEqualComparingTo("fennec")
        }
    }
    expect("a trie to return nulls") {
        trie.run {
            get("the", "quick", "arctic", "fox", "jumps", "over", "the", "lazy", "dog").shouldBeNull()
            get("the", "quick", "cape", "fox", "jumps", "over", "the", "lazy", "dog").shouldBeNull()
        }
    }
    expect("a trie to return nulls to removed addresses but leave the rest unchanged") {
        trie.run {
            get("the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog").shouldNotBeNull()
            remove("the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog")
            get("the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog").shouldBeNull()

            get("the", "quick", "fennec", "fox", "jumps", "over", "the", "lazy", "dog").shouldNotBeNull()
            remove("the", "quick", "fennec", "fox", "jumps", "over", "the", "lazy", "dog")
            get("the", "quick", "fennec", "fox", "jumps", "over", "the", "lazy", "dog").shouldBeNull()

            get("the", "quick", "gray", "fox", "jumps", "over", "the", "lazy", "dog")
                ?.shouldBeEqualComparingTo("gray")
            get("the", "quick", "swift", "fox", "jumps", "over", "the", "lazy", "dog")
                ?.shouldBeEqualComparingTo("swift")
        }
    }
})
