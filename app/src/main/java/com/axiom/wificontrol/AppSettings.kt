package com.axiom.wificontrol

import android.content.Context

object AppSettings {

    private const val PREF = "app_settings"

    // Default: pill blur 30dp, alpha 6%
    private const val KEY_PILL_BLUR = "pill_blur"
    private const val KEY_PILL_ALPHA = "pill_alpha"
    private const val KEY_CARD_BLUR = "card_blur"

    fun getPillBlur(ctx: Context): Int =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_PILL_BLUR, 30)

    fun setPillBlur(ctx: Context, v: Int) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_PILL_BLUR, v).apply()

    fun getPillAlpha(ctx: Context): Int =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_PILL_ALPHA, 16)  // 16 = 6% dari 255

    fun setPillAlpha(ctx: Context, v: Int) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_PILL_ALPHA, v).apply()

    fun getCardBlur(ctx: Context): Int =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_CARD_BLUR, 24)

    fun setCardBlur(ctx: Context, v: Int) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putInt(KEY_CARD_BLUR, v).apply()
}
