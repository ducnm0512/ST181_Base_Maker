package com.example.st181_halloween_maker.core.dialog

import android.app.Activity
import android.content.Context
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseDialog
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.databinding.DialogItemTickerBinding

class StickerDialog(val context: Activity): BaseDialog<DialogItemTickerBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_item_ticker
    override val isCancel: Boolean = false
    override val isBack: Boolean = false
    var onShareClick: (() -> Unit)? = null
    var onDownloadClick: (() -> Unit)? = null
    var onDismissClick: (() -> Unit)? = null

    override fun initView() {
        binding.apply {
//            txtCheck.select()
        }
    }

    override fun initAction() {
        binding.btnExit.onSingleClick {
            onDismissClick?.invoke()
        }
        binding.main.onSingleClick {
            onDismissClick?.invoke()
        }
        binding.btnShare.onSingleClick {
            onShareClick?.invoke()
        }
        binding.btnDownLoad.onSingleClick {
            onDownloadClick?.invoke()
        }
    }

    override fun onDismissListener() {

    }
}