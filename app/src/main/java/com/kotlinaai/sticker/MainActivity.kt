package com.kotlinaai.sticker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                    mutableStateOf(buildList {
                        add(1)
                        add(2)
                        add(3)
                        add(4)
                        add(5)
                    })
                }

                StickerContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .paint(
                            painterResource(com.kotlinaai.sticker.R.drawable.test),
                            contentScale = ContentScale.Crop
                        ),
                    stickerContainerState = rememberStickerContainerState{stickers.size},
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
                                .size(70.dp)
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
                                    numVertices = index + 3,
                                    radius = size.minDimension / 2,
                                    centerX = size.width / 2,
                                    centerY = size.height / 2
                                )
                                val roundedPolygonPath = roundedPolygon.toPath().asComposePath()
                                onDrawBehind {
                                    drawPath(roundedPolygonPath, color = Color.Magenta)
                                }
                            }
                            .size(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stickers[index].toString())
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