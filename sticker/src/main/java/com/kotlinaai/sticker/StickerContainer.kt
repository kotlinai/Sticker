package com.kotlinaai.sticker

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 *
 * @Author:         chenp
 * @CreateDate:     2024/9/20 11:35
 * @UpdateUser:     chenp
 * @UpdateDate:     2024/9/20 11:35
 * @Version:        1.0
 * @Description:
 */

class StickerContainerState(
    updateStickerCount: () -> Int
) {

    var stickerCountState = mutableStateOf(updateStickerCount)
    val stickerCount: Int get() = stickerCountState.value.invoke()

    private val stickerStateMap = mutableStateMapOf<Any, StickerState>()

    @Composable
    internal fun getStickerState(key: Any) = remember(key) {
        derivedStateOf {
            stickerStateMap.getOrPut(key) {
                val maxZIndex = stickerStateMap.values.maxOfOrNull { it.zIndex } ?: 0f

                //selectedIndex = stickerStateMap.size
                clearEditState()
                StickerState(true, maxZIndex + 1f)
            }
        }
    }

    fun setSelectItem(key: Any) {
        val maxZIndex = stickerStateMap.values.maxOfOrNull { it.zIndex } ?: 0f

        stickerStateMap.forEach { (stateKey, state) ->
            state.enable = stateKey == key

            if (state.enable) {
                state.zIndex = maxZIndex + 1
            }
        }
    }

    internal fun clearInvalidState(keys: List<Any>) {

        stickerStateMap
            .filterKeys {
                !keys.contains(it)
            }
            .forEach {
                stickerStateMap.remove(it)
            }
    }

    fun clearEditState() {
        //selectedIndex = -1
        stickerStateMap.values.forEach {
            it.enable = false
        }
    }
}

@Composable
fun rememberStickerContainerState(
    stickerCount: () -> Int
) = remember {
    StickerContainerState(stickerCount)
}.apply {
    stickerCountState.value = stickerCount
}

/**
 * 1.点击显示编辑,取消其他编辑
 * 2.点击显示为最上层
 * 3.点击外部取消编辑
 * @param scaleAndRotateButton [@androidx.compose.runtime.Composable] Function0<Unit>
 * @param deleteButton [@androidx.compose.runtime.Composable] Function0<Unit>
 * @param background [@androidx.compose.runtime.Composable] Function0<Unit>
 * @param modifier Modifier
 * @param stickerContainerState StickerContainerState
 * @param stickerContent [@androidx.compose.runtime.Composable] Function0<Unit>
 */
@Composable
fun StickerContainer(
    stickerContainerState: StickerContainerState,
    scaleAndRotateButton: @Composable () -> Unit,
    deleteButton: @Composable (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    background: @Composable () -> Unit = {},
    key: ((index: Int) -> Any)? = null,
    scaleRange: ClosedFloatingPointRange<Float> = 0.5f..3f,
    stickerContent: @Composable (index: Int) -> Unit
) {
    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures {
                    stickerContainerState.clearEditState()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val lastKey by rememberUpdatedState(key ?: {index-> index})
        val lastContent by rememberUpdatedState(stickerContent)
        val lastDeleteButton by rememberUpdatedState(deleteButton)
        val keys by remember(stickerContainerState, lastKey, lastContent) {
            derivedStateOf {
                buildList {
                    for (index in 0 until stickerContainerState.stickerCount) {
                        add(lastKey(index))
                    }
                }
            }
        }

        SideEffect {
            Log.d("Sticker", "重组")
            stickerContainerState.clearInvalidState(keys)
        }

        keys.forEachIndexed { index, itemKey ->
            val stickerState by stickerContainerState.getStickerState(key = itemKey)

            StickerItem(
                stickerState = stickerState,
                scaleAndRotate = scaleAndRotateButton,
                scaleRange = scaleRange,
                delete = {
                    lastDeleteButton(index)
                },
                background = background,
                content = {
                    Box(
                        modifier = Modifier
                            .pointerInput(stickerState, itemKey) {
                                awaitEachGesture {
                                    awaitFirstDown(false)

                                    stickerContainerState.setSelectItem(itemKey)
                                }
                            }
                    ) {
                        lastContent(index)
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun StickerContainerPreview() {

    var stickers by remember {
        mutableStateOf<List<Long>>(emptyList())
    }
    val containerState = rememberStickerContainerState{stickers.size}

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        StickerContainer(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
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
                            color = Color.Green,
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
                        .size(60.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.3f)
                        )
                )
            },
            key = { stickers[it] }
        ) { index ->
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color.Magenta
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stickers[index].toString())
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth(),
            onClick = {
                stickers += System.currentTimeMillis()
            }
        ) {
            Text("添加")
        }
    }
}