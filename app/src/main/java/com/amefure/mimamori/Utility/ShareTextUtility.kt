package com.amefure.mimamori.Utility

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.amefure.mimamori.R

class ShareTextUtility {

    companion object {
        /** 共有する */
        public fun shareUserId(id: String, context: Context) {
            val msg = context.getString(R.string.onboarding3_mimamori_share_text, id)
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, msg)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            }
        }

        /** クリップボードにIDをコピーする */
        public fun copyIdToClipboard(context: Context, id: String) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Mimamori ID", id)
            clipboard.setPrimaryClip(clip)
        }
    }
}