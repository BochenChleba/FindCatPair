package bochenchleba.com.findcatpair.game

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.compose.animation.with
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import bochenchleba.com.findcatpair.model.Card
import bochenchleba.com.findcatpair.model.CatImage
import bochenchleba.com.findcatpair.repository.CatImageRepository
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.Job

class GameViewModel(
    private val app: Application,
    private val catImageRepository: CatImageRepository,
) : AndroidViewModel(app) {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var firstClickedCard: Card? = null
    private var secondClickedCard: Card? = null
    private var timerJob: Job? = null

    init {
        loadCards()
    }

    fun loadCards() {
        _uiState.update { currentState ->
            currentState.copy(isGameFinished = false, isError = false, gameTime = 0L)
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val catImages = catImageRepository.getCatImages(NUMBER_OF_PAIRS)
                preloadImages(catImages)
                val cards = createCards(catImages)
                _uiState.value = _uiState.value.copy(cards = cards, isLoading = false)
                startTimer()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error loading images", isLoading = false, isError = true)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { currentState ->
                    currentState.copy(gameTime = currentState.gameTime + 1)
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
    }

    private suspend fun preloadImages(catImages: List<CatImage>) {
        withContext(Dispatchers.IO) {
            catImages.forEach { catImage ->
                val requestBuilder: RequestBuilder<Drawable> = Glide.with(app).load(catImage.url)
                requestBuilder.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        _uiState.update { currentState ->
                            currentState.copy(isError = true, error = "Error loading images")
                        }
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }
                }).submit().get()
            }
        }
    }

    private fun createCards(catImages: List<CatImage>): List<Card> {
        val cards = mutableListOf<Card>()
        catImages.forEachIndexed { index, catImage ->
            cards.add(Card(index * 2, catImage, isRevealed = false, isCovered = true))
            cards.add(Card(index * 2 + 1, catImage, isRevealed = false, isCovered = true))
        }
        return cards.shuffled()
    }

    fun onCardClick(card: Card) {
        if (card.isCovered) {
            if (firstClickedCard == null) {
                firstClickedCard = card
                updateCardState(card, isCovered = false)
            } else if (secondClickedCard == null && firstClickedCard?.id != card.id) {
                secondClickedCard = card
                updateCardState(card, isCovered = false)
                checkMatch()
            }
        }
    }

    private fun checkMatch() {
        viewModelScope.launch {
            if (firstClickedCard?.catImage?.id == secondClickedCard?.catImage?.id) {
                markCardsAsMatched()
            } else {
                _uiState.update { currentState ->
                    val updatedCards = currentState.cards.map {
                        if (it.id == firstClickedCard?.id || it.id == secondClickedCard?.id) {
                            it.copy(isShaking = true)
                        } else {
                            it
                        }
                    }
                    currentState.copy(cards = updatedCards)
                }
                delay(1000)
                resetCards()
            }
        }
    }

    private fun markCardsAsMatched() {
        _uiState.update { currentState ->
            val updatedCards = currentState.cards.map {
                if (it.id == firstClickedCard?.id || it.id == secondClickedCard?.id) {
                    it.copy(isMatched = true)
                } else {
                    it
                }
            }
            currentState.copy(cards = updatedCards)
        }
        firstClickedCard = null
        secondClickedCard = null
        checkGameFinished()
    }

    private fun resetCards() {
        _uiState.update { currentState ->
            val updatedCards = currentState.cards.map {
                if (it.id == firstClickedCard?.id || it.id == secondClickedCard?.id) {
                    it.copy(isCovered = true, isRevealed = false)
                } else {
                    it
                }
            }
            currentState.copy(cards = updatedCards)
        }
        firstClickedCard = null
        secondClickedCard = null
    }

    private fun updateCardState(card: Card, isCovered: Boolean) {
        _uiState.update { currentState ->
            val updatedCards = currentState.cards.map {
                if (it.id == card.id) {
                    it.copy(isCovered = isCovered, isRevealed = !isCovered)
                } else {
                    it
                }
            }
            currentState.copy(cards = updatedCards)
        }
    }

    private fun checkGameFinished() {
        val allMatched = _uiState.value.cards.all { it.isMatched }
        _uiState.update { currentState ->
            currentState.copy(isGameFinished = allMatched)
        }
        if (allMatched) {
            stopTimer()
        }
    }

    companion object {
        const val NUMBER_OF_PAIRS = 6
    }
}