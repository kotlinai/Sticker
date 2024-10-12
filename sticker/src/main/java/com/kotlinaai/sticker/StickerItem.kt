package com.kotlinaai.sticker

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.zIndex
import com.kotlinaai.sticker.ext.angleTo
import com.kotlinaai.sticker.ext.rotate
import kotlinx.coroutines.flow.filter

/**
 *
 * @Author:         chenp
 * @CreateDate:     2024/9/20 11:34
 * @UpdateUser:     chenp
 * @UpdateDate:     2024/9/20 11:34
 * @Version:        1.0
 * @Description:
 */

internal class StickerState(initialEnable: Boolean = false, zOrder: Float = 0f) {
    var enable by mutableStateOf(initialEnable)
    var translation by mutableStateOf(Offset.Zero)
    var scale by mutableFloatStateOf(1f)
    var rotation by mutableFloatStateOf(0f)
    var zIndex by mutableFloatStateOf(zOrder)
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
    var imageCoordinates: LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    var transformBtnCoordinates: LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    var transformBtnAnchorCoordinates: LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    var deleteAnchorCoordinate: LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    var deleteBtnCoordinates: LayoutCoordinates? by remember {
        mutableStateOf(null)
    }
    var buttonVisible by remember {
        mutableStateOf(true)
    }
    var deleteBtnTranslate by remember {
        mutableStateOf(Offset.Zero)
    }
    var transformBtnTranslate by remember {
        mutableStateOf(Offset.Zero)
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(buttonVisible, transformBtnAnchorCoordinates, transformBtnCoordinates)
        }.filter { (_, anchor, button) ->
            anchor != null && button?.isAttached == true
        }.collect { (_, anchor, button) ->
            val target = anchor!!.boundsInWindow().center
            val cur = button!!.boundsInWindow().center

            transformBtnTranslate += target - cur
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            Triple(buttonVisible, deleteAnchorCoordinate, deleteBtnCoordinates)
        }.filter { (_, anchor, button) ->
            anchor != null && button?.isAttached == true
        }.collect { (_, anchor, button) ->
            val target = anchor!!.boundsInWindow().center
            val cur = button!!.boundsInWindow().center

            deleteBtnTranslate += target - cur
        }
    }

    Box(
        modifier = Modifier
            .zIndex(stickerState.zIndex)
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    translationX = stickerState.translation.x
                    translationY = stickerState.translation.y
                }
        ) {

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .onPlaced { coordinates ->
                        imageCoordinates = coordinates
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
                        .pointerInput(Unit) {
                            detectTapGestures {  }
                        }
                ) {

                    if (stickerState.enable) {

                        background()
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) { content() }

                    //缩放按钮锚点
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(1.dp)
                            .onPlaced {
                                transformBtnAnchorCoordinates = it
                            }
                    )
                    //删除按钮锚点
                    Spacer(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(1.dp)
                            .onPlaced {
                                //val center = it.boundsInWindow().center
                                deleteAnchorCoordinate = it
                            }
                    )
                }
            }
        }

        if (stickerState.enable) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .graphicsLayer {
                        alpha = if (buttonVisible) 1f else 0f
                        //Log.d("Sticker", "deleteBtnTranslate=$deleteBtnTranslate")
                        translationX = deleteBtnTranslate.x
                        translationY = deleteBtnTranslate.y
                    }
            ) {
                Box(
                    modifier = Modifier
                        .onPlaced {
                            deleteBtnCoordinates = it
                        }
                ) {
                    delete()
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .graphicsLayer {
                        alpha = if (buttonVisible) 1f else 0f

                        translationX = transformBtnTranslate.x
                        translationY = transformBtnTranslate.y
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
                        .pointerInput(transformBtnCoordinates, imageCoordinates) {
                            detectDragGestures { change, _ ->

                                transformBtnCoordinates?.let { transCoor ->
                                    imageCoordinates?.boundsInWindow()?.center?.let { imageCenter ->

                                        val imageCenterInLocal = transCoor.windowToLocal(imageCenter)
                                        val prevVector = change.previousPosition - imageCenterInLocal
                                        val curVector = change.position - imageCenterInLocal

                                        stickerState.scale =
                                            (stickerState.scale * curVector.getDistance() / prevVector.getDistance()).coerceIn(
                                                scaleRange
                                            )
                                        stickerState.rotation += prevVector.angleTo(curVector)
                                    }
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
                add(StickerState(true))
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
                                //stickers = stickers - it
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