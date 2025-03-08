package bochenchleba.com.findcatpair.model

data class Card(
    val id: Int,
    val catImage: CatImage,
    var isCovered: Boolean = true,
    var isMatched: Boolean = false,
    var isRevealed: Boolean = false,
    var isShaking: Boolean = false
)