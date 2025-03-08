@file:OptIn(ExperimentalGlideComposeApi::class)

package bochenchleba.com.findcatpair.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bochenchleba.com.findcatpair.model.Card
import bochenchleba.com.findcatpair.ui.theme.FindCatPairTheme
import org.koin.androidx.compose.koinViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import kotlin.ranges.coerceIn

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindCatPairTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen()
                }
            }
        }
    }
}

@Composable
fun GameScreen(gameViewModel: GameViewModel = koinViewModel()) {
    val uiState by gameViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.isError) {
                LaunchedEffect(uiState.error) {
                    snackbarHostState.showSnackbar(
                        message = uiState.error ?: "Unknown error",
                    )
                }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { gameViewModel.loadCards() }) {
                        Text(text = "Retry")
                    }
                }
            } else if (uiState.isGameFinished) {
                var isWinTextVisible by remember { mutableStateOf(false) }
                val winTextScale by animateFloatAsState(
                    targetValue = if (isWinTextVisible) 1.5f else 0.5f,
                    animationSpec = tween(durationMillis = 400),
                    label = "winTextScale"
                )
                val winTextAlpha by animateFloatAsState(
                    targetValue = if (isWinTextVisible) 1f else 0f,
                    animationSpec = tween(durationMillis = 400),
                    label = "winTextAlpha"
                )
                LaunchedEffect(uiState.isGameFinished) {
                    delay(500)
                    isWinTextVisible = true
                }
                Column(
                    modifier = Modifier.align(Alignment.Center)
                        .graphicsLayer {
                            scaleX = winTextScale
                            scaleY = winTextScale
                        }
                        .alpha(winTextAlpha),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You Win!",
                        fontSize = 30.sp,
                        modifier = Modifier

                    )
                    Text(text = "Time: ${uiState.gameTime} seconds")
                    Button(onClick = { gameViewModel.loadCards() }) {
                        Text(text = "Restart")
                    }
                }
            } else {
                val cardSize = calculateCardSize(GameViewModel.NUMBER_OF_PAIRS * 2)
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(cardSize),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    itemsIndexed(uiState.cards) { _, card ->
                        CardItem(
                            card = card,
                            cardSize = cardSize,
                            onCardClick = { gameViewModel.onCardClick(card) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CardItem(
    card: Card,
    cardSize: Dp,
    onCardClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (card.isRevealed) 180f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "rotation"
    )
    var isScaled by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isScaled) 1.2f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "scale"
    )
    val shakeAnimation by animateFloatAsState(
        targetValue = if (card.isShaking) 1f else 0f,
        animationSpec = keyframes {
            durationMillis = 500
            0f at 0
            (-20f) at 50
            20f at 100
            (-20f) at 150
            20f at 200
            (-20f) at 250
            20f at 300
            (-20f) at 350
            20f at 400
            0f at 500
        }, label = "shakeAnimation"
    )

    LaunchedEffect(card.isMatched) {
        if (card.isMatched) {
            delay(100)
            isScaled = true
            delay(500)
            isScaled = false
        }
    }

    Box(
        modifier = Modifier
            .size(cardSize)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 8 * density
                scaleX = scale
                scaleY = scale
                translationX = shakeAnimation
            }
            .background(if (card.isCovered) Color.Gray else Color.Transparent)
            .clickable {
                onCardClick()
            }
    ) {
        if (rotation > 90f) {
            GlideImage(
                model = card.catImage.url,
                contentDescription = "Cat Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = 180f
                    }
            )
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindCatPairTheme {
        GameScreen()
    }
}

@Composable
fun calculateCardSize(numberOfCards: Int): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Calculate the approximate number of rows and columns
    val aspectRatio = screenWidth / screenHeight
    val approximateColumns = sqrt(numberOfCards.toFloat() * aspectRatio)
    val columns = approximateColumns.toInt().coerceIn(2, numberOfCards)
    val rows = (numberOfCards + columns - 1) / columns

    // Calculate the available width and height for cards
    val availableWidth = screenWidth - 8.dp * (columns + 1)
    val availableHeight = screenHeight - 8.dp * (rows + 1)

    // Calculate the card width and height
    val cardWidth = availableWidth / columns
    val cardHeight = availableHeight / rows

    // Return the smaller dimension to ensure all cards fit
    return minOf(cardWidth, cardHeight)
}