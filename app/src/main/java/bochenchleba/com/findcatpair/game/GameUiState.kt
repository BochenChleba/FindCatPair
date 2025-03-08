package bochenchleba.com.findcatpair.game

import bochenchleba.com.findcatpair.model.Card

data class GameUiState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isGameFinished: Boolean = false,
    val gameTime: Long = 0L,
    val isError: Boolean = false
)