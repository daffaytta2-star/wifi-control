package com.axiom.wificontrol

import android.content.Context

object AppSettings {
    private const val PREF = "app_settings"
    private const val KEY_PILL_WIDTH = "pill_width"
    private const val KEY_PILL_HEIGHT = "pill_height"
    private const val KEY_PILL_FONT = "pill_font"

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun getPillWidth(ctx: Context): Int = prefs(ctx).getInt(KEY_PILL_WIDTH, 90)
    fun setPillWidth(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_PILL_WIDTH, v).apply()

    fun getPillHeight(ctx: Context): Int = prefs(ctx).getInt(KEY_PILL_HEIGHT, 6)
    fun setPillHeight(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_PILL_HEIGHT, v).apply()

    fun getPillFont(ctx: Context): Int = prefs(ctx).getInt(KEY_PILL_FONT, 10)
    fun setPillFont(ctx: Context, v: Int) = prefs(ctx).edit().putInt(KEY_PILL_FONT, v).apply()
}
