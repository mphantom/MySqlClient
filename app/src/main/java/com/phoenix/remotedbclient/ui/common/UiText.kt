package com.phoenix.remotedbclient.ui.common

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes

/**
 * 职责：承载 ViewModel 需要展示给用户的文案，避免在业务层拼接固定英文。
 * 位置：ui 公共层，供各 ViewModel 的一次性事件与对应 Screen 的 Snackbar 共用。
 *
 * 用法：
 * - [Res] 对应 strings.xml 中的普通字符串；
 * - [Plural] 对应 plurals 中的数量文案；
 * - 动态值（数据库报错、连接名等）通过 args 传入占位符，不在业务层拼字符串。
 */
sealed interface UiText {

    /** 普通字符串资源，[args] 依次填充 %1$s、%2$d 等占位符。 */
    data class Res(
        @StringRes val id: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    /** 复数资源，[count] 决定单复数形式，[args] 用于填充占位符（通常包含 count）。 */
    data class Plural(
        @PluralsRes val id: Int,
        val count: Int,
        val args: List<Any> = emptyList()
    ) : UiText

    /** 在非 Composable 环境（如 Snackbar 回调）中解析为最终文案。 */
    fun resolve(context: Context): String = when (this) {
        is Res -> context.getString(id, *args.toTypedArray())
        is Plural -> context.resources.getQuantityString(id, count, *args.toTypedArray())
    }
}
