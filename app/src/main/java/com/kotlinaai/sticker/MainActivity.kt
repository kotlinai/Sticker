package com.kotlinaai.sticker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.kotlinaai.sticker.ui.theme.StickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StickerTheme {
                var stickers by remember {
                    mutableStateOf<List<Int>>(emptyList())
                }
                var index = remember {
                    0
                }
                val containerState = rememberStickerContainerState { stickers.size }


                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    StickerContainer(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(50.dp)
                            .clipToBounds()
                            .paint(
                                painterResource(R.drawable.test),
                                contentScale = ContentScale.Crop
                            ),
                        stickerContainerState = containerState,
                        scaleAndRotateButton = {
                            Spacer(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = Color.Blue,
                                        shape = CircleShape
                                    )
                            )
                        },
                        deleteButton = {
                            Spacer(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = Color.Red,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        stickers = stickers - stickers[it]
                                    }
                            )
                        },
                        background = {
                            Spacer(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        color = Color.Black.copy(alpha = 0.3f)
                                    )
                            )
                        },
                        key = { stickers[it] }
                    ) { index ->
                        Box(
                            modifier = Modifier
                                .drawWithCache {
                                    val roundedPolygon = RoundedPolygon(
                                        numVertices = stickers[index] + 3,
                                        radius = size.minDimension / 2,
                                        centerX = size.width / 2,
                                        centerY = size.height / 2
                                    )
                                    val roundedPolygonPath = roundedPolygon
                                        .toPath()
                                        .asComposePath()
                                    onDrawBehind {
                                        drawPath(roundedPolygonPath, color = Color.Magenta)
                                    }
                                }
                                .size(50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = stickers[index].toString())
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        onClick = {
                            stickers += (++index)
                            Log.d("Sticker", "添加$index")
                        }
                    ) {
                        Text("添加")
                    }
                }
            }

        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StickerTheme {
        Greeting("Android")
    }
}