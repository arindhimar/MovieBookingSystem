package com.example.dailyquotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dailyquotes.ui.theme.DailyQuotesTheme
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyQuotesTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    var quote by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var isFetching by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val fetchedQuote = fetchQuote()
        quote = fetchedQuote.first
        author = fetchedQuote.second
        isFetching = false
    }

    MainContext(quote, author, isFetching)
}

suspend fun fetchQuote(): Pair<String, String> = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url("https://api.api-ninjas.com/v1/quotes?category=happiness")
        .addHeader("accept", "application/json")
        .addHeader("X-Api-Key", "1nkiJyFpOk3fEyxbf7LrfA==Q0NKhCa3hiJGcdSQ") // Add your API key here
        .build()

    client.newCall(request).execute().use { response ->
        val responseStream: InputStream = response.body?.byteStream() ?: return@withContext Pair("No quote found", "")
        val mapper = ObjectMapper()
        val root: JsonNode = mapper.readTree(responseStream)
        val quoteText = root.path(0).path("quote").asText()
        val quoteAuthor = root.path(0).path("author").asText()
        Pair(quoteText, quoteAuthor)
    }
}

@Composable
fun MainContext(quote: String, author: String, isFetching: Boolean) {
    val fontSize = when {
        quote.length < 50 -> 24.sp
        quote.length < 100 -> 20.sp
        else -> 16.sp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                if (isFetching) {
                    // Show the gift wrapping animation
                    AnimatedVisibility(
                        visible = isFetching,
                        enter = fadeIn() + expandIn(),
                        exit = fadeOut() + shrinkOut()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.facebook),
                            contentDescription = "Gift Wrapping",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Adjust the height as needed
                        )
                    }
                } else {
                    // Show the quote content
                    AnimatedVisibility(
                        visible = !isFetching,
                        enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000)),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = quote,
                                fontWeight = FontWeight.Bold,
                                fontSize = fontSize
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "~$author",
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                    textAlign = TextAlign.Right
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            SocialMediaButtons()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SocialMediaButtons() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val iconModifier = Modifier.size(24.dp)

            TextButton(onClick = { /* Handle Facebook click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.facebook),
                    contentDescription = "Facebook",
                    contentScale = ContentScale.Fit,
                    modifier = iconModifier
                )
            }
            TextButton(onClick = { /* Handle Instagram click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.instagram),
                    contentDescription = "Instagram",
                    contentScale = ContentScale.Fit,
                    modifier = iconModifier
                )
            }
            TextButton(onClick = { /* Handle Twitter click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.twitter),
                    contentDescription = "Twitter",
                    contentScale = ContentScale.Fit,
                    modifier = iconModifier
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            val iconModifier = Modifier.size(24.dp)

            TextButton(onClick = { /* Handle Snapchat click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.snapchat),
                    contentDescription = "Snapchat",
                    contentScale = ContentScale.Fit,
                    modifier = iconModifier
                )
            }
            TextButton(onClick = { /* Handle WhatsApp click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.whatsapp),
                    contentDescription = "WhatsApp",
                    contentScale = ContentScale.Fit,
                    modifier = iconModifier
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainContextPreview() {
    DailyQuotesTheme {
        MainContext("Be happy for this moment. This moment is your life.", "Omar Khayyam", isFetching = false)
    }
}
