package com.kotlinaai.sticker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isUnspecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.zIndex
import com.kotlinaai.sticker.ext.angleTo
import com.kotlinaai.sticker.ext.rotate

/**
 *
 * @Author:         chenp
 * @CreateDate:     2024/9/20 11:34
 * @UpdateUser:     chenp
 * @UpdateDate:     2024/9/20 11:34
 * @Version:        1.0
 * @Description:
 */

internal class StickerState {
    var enable by mutableStateOf(false)
    var translation by mutableStateOf(Offset.Zero)
    var scale by mutableFloatStateOf(1f)
    var rotation by mutableFloatStateOf(0f)
    var zIndex by mutableFloatStateOf(0f)
}

@Composable
internal fun rememberStickerState(): StickerState =
    remember {
        StickerState()
    }

@Composable
internal fun StickerItem(
    scaleAndRotate: @Composable () -> Unit,
    delete: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    stickerState: StickerState = rememberStickerState(),
    scaleRange: ClosedFloatingPointRange<Float> = 0.5f..3f,
    background: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    var imageCenter = remember {
        Offset.Unspecified
    }
    var transformBtnCoordinates: LayoutCoordinates? = remember {
        null
    }
    var transformBtnAnchor = remember {
        Offset.Unspecified
    }
    var deleteBtnAnchor = remember {
        Offset.Unspecified
    }
    var deleteBtnPos = remember {
        Offset.Unspecified
    }
    var buttonVisible by remember {
        mutableStateOf(true)
    }

    Box(
        modifier = Modifier
            .zIndex(stickerState.zIndex)
            .graphicsLayer {
                translationX = stickerState.translation.x
                translationY = stickerState.translation.y
            }
            .then(modifier)
    ) {

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .onPlaced { coordinates ->
                    imageCenter = coordinates.boundsInRoot().center
                }
                .graphicsLayer {

                    scaleX = stickerState.scale
                    scaleY = stickerState.scale
                    rotationZ = stickerState.rotation
                }
        ) {
                Box(
                    modifier = Modifier
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                while (true) {
                                    val event = awaitPointerEvent()

                                    buttonVisible = !event.changes.fastAny { it.pressed }
                                }
                            }
                        }
                        .pointerInput(stickerState) {
                            detectDragGestures { _, dragAmount ->
                                stickerState.translation += (dragAmount * stickerState.scale).rotate(stickerState.rotation)
                            }
                        }
                ) {

                    if (stickerState.enable) {

                        background()
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .pointerInput(stickerState) {
                                awaitEachGesture {
                                    awaitFirstDown(false)
                                    stickerState.enable = true
                                }
                            }
                    ) { content() }

                    //缩放按钮锚点
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(1.dp)
                            .onPlaced {
                                transformBtnAnchor = it.boundsInRoot().center
                            }
                    )
                    //删除按钮锚点
                    Spacer(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(1.dp)
                            .onPlaced {
                                deleteBtnAnchor = it.boundsInRoot().center
                            }
                    )
                }
        }
        if (stickerState.enable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        alpha = if (buttonVisible) 1f else 0f

                        if (buttonVisible && !deleteBtnPos.isUnspecified && !deleteBtnAnchor.isUnspecified) {
                            val offset = deleteBtnAnchor - deleteBtnPos

                            translationX += offset.x
                            translationY += offset.y
                        }
                    }
                    .onPlaced {
                        deleteBtnPos = it.boundsInRoot().center
                    }
            ) {
                delete()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        alpha = if (buttonVisible) 1f else 0f

                        if (buttonVisible && transformBtnCoordinates != null && !transformBtnAnchor.isUnspecified) {
                            val offset =
                                transformBtnAnchor - transformBtnCoordinates!!.boundsInRoot().center

                            translationX += offset.x
                            translationY += offset.y
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .onPlaced {
                            transformBtnCoordinates = it
                        }
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                while (true) {
                                    val event = awaitPointerEvent()

                                    buttonVisible = !event.changes.fastAny { it.pressed }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                transformBtnCoordinates?.let {
                                    val prevVector =
                                        it.localToRoot(change.previousPosition) - imageCenter
                                    val curVector = it.localToRoot(change.position) - imageCenter

                                    stickerState.scale =
                                        (stickerState.scale * curVector.getDistance() / prevVector.getDistance()).coerceIn(
                                            scaleRange
                                        )
                                    stickerState.rotation += prevVector.angleTo(curVector)
                                }
                            }
                        }
                ) {
                    scaleAndRotate()
                }
            }
        }
    }
}

@Preview
@Composable
private fun StickerPreview() {

    var stickers by remember {
        mutableStateOf(
            buildList {
                add(StickerState())
                add(StickerState())
                add(StickerState())
                add(StickerState())
                add(StickerState())
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    stickers.forEach {
                        it.enable = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {

        stickers.forEach {

            key(it) {
                LaunchedEffect(it) {
                    snapshotFlow { it.enable }
                        .collect { enable ->
                            if (enable) {
                                val newStickers = stickers - it

                                newStickers.forEach { item -> item.enable = false }

                                stickers = newStickers + it
                            }
                        }
                }

                StickerItem(
                    stickerState = it,
                    scaleAndRotate = {
                        Spacer(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = Color.Blue,
                                    shape = CircleShape
                                )
                        )
                    },
                    delete = {
                        Spacer(
                            modifier = Modifier
                                .size(20.dp)
                                .background(
                                    color = Color.Green,
                                    shape = CircleShape
                                )
                                .clickable {
                                    stickers = stickers - it
                                }
                        )
                    },
                    background = {
                        Spacer(
                            modifier = Modifier
                                .size(100.dp)
                                .background(
                                    color = Color.Black.copy(alpha = 0.3f)
                                )
                        )
                    }
                ) {
                    Spacer(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                color = Color.Magenta
                            )
                    )
                }
            }

        }
    }
}