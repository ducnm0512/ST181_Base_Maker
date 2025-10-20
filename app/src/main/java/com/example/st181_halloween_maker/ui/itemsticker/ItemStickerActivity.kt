package com.example.st181_halloween_maker.ui.itemsticker

import android.R.attr.bitmap
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.dialog.LoadingDialog
import com.example.st181_halloween_maker.core.dialog.StickerDialog
import com.example.st181_halloween_maker.core.extensions.checkPermissions
import com.example.st181_halloween_maker.core.extensions.goToSettings
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.handleShare
import com.example.st181_halloween_maker.core.extensions.hideNavigation
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.requestPermission
import com.example.st181_halloween_maker.core.extensions.saveBitmapToExternalStorage
import com.example.st181_halloween_maker.core.extensions.select
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.extensions.startIntentAnim
import com.example.st181_halloween_maker.core.utils.KeyApp.HALLOWEEN_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.NOTIFICATION_PERMISSION_CODE
import com.example.st181_halloween_maker.core.utils.KeyApp.STORAGE_PERMISSION_CODE
import com.example.st181_halloween_maker.core.utils.SystemUtils.isNotificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.isStoragePermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.setLocale
import com.example.st181_halloween_maker.core.utils.SystemUtils.setNotificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.setStoragePermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.storagePermission
import com.example.st181_halloween_maker.databinding.ActivityItemStickerBinding
import com.example.st181_halloween_maker.ui.customize.CustomizeActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.forEachIndexed

class ItemStickerActivity : BaseActivity<ActivityItemStickerBinding>() {

    private lateinit var dialogSticker: StickerDialog

    private lateinit var bitmap: Bitmap
    private lateinit var dialogLoading: LoadingDialog
    private val stickerList = ArrayList<String>()
    private val stickerAdapter by lazy { ItemStickerAdapter(this) }
    override fun setViewBinding(): ActivityItemStickerBinding {
        return ActivityItemStickerBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        dialogLoading = LoadingDialog(this@ItemStickerActivity)
        dialogSticker = StickerDialog(this@ItemStickerActivity)
        initData()
        initRcv()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
            btnDowLoad.onSingleClick {
                handleRightBot()
            }
            btnShareTelegram.onSingleClick {
                handleShareTelegram()
            }
            btnShareWhatsapp.onSingleClick {
                handleShareWhatsapp()
            }
            handleRcv()
        }
    }

    override fun initText() {
        binding.apply {
            txtShareWhatsapp.select()
            txtDownLoad.select()
            txtShareTelegram.select()
        }
    }

    private fun initData() {
        val folder = intent.getStringExtra(HALLOWEEN_KEY) ?: return
        stickerList.clear()
        stickerList.addAll(getStickerFromAssets(folder))
    }

    private fun getStickerFromAssets(folder: String): List<String> {
        val stickers = ArrayList<String>()
        try {
            val files = assets.list(folder) ?: return emptyList()
            for (file in files) {
                stickers.add("$folder/$file")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stickers
    }

    private fun initRcv() {
        binding.apply {
            rcv.adapter = stickerAdapter
            rcv.itemAnimator = null
            stickerAdapter.submitList(stickerList)
            Log.d("Sticker", "${stickerList.size}")
        }
    }

    private fun handleRightBot() {
        checkPermission()
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkPermissions(storagePermission)) {
                handleDownload()

            } else {
                val deniedCount = isStoragePermission(this)
                Log.d("PermissionCheck", "Số lần bị từ chối quyền storage: $deniedCount")
                if (isStoragePermission(this) < 2 && !checkPermissions(storagePermission)) {
                    Log.d("PermissionCheck", "Yêu cầu lại quyền storage")
                    requestPermission(storagePermission, STORAGE_PERMISSION_CODE)
                } else {
                    Log.d("PermissionCheck", "Đã từ chối vĩnh viễn -> chuyển tới Cài đặt")
                    goToSettings()
                }
            }
        } else {
            handleDownload()

        }
    }
    private fun handleRightBot1() {
        checkPermission1()
    }

    private fun checkPermission1() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (checkPermissions(storagePermission)) {
                handleDownload1()

            } else {
                val deniedCount = isStoragePermission(this)
                Log.d("PermissionCheck", "Số lần bị từ chối quyền storage: $deniedCount")
                if (isStoragePermission(this) < 2 && !checkPermissions(storagePermission)) {
                    Log.d("PermissionCheck", "Yêu cầu lại quyền storage")
                    requestPermission(storagePermission, STORAGE_PERMISSION_CODE)
                } else {
                    Log.d("PermissionCheck", "Đã từ chối vĩnh viễn -> chuyển tới Cài đặt")
                    goToSettings()
                }
            }
        } else {
            handleDownload1()

        }
    }

    @SuppressLint("NotifyDataSetChanged") private fun handleDownload() {
        setLocale(this@ItemStickerActivity)
        dialogLoading.show()

        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            dialogLoading.dismiss()
            Log.e("DownloadError", "Error: ${throwable.message}")
            showToast(getString(R.string.error))
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            try {
                var successCount = 0

                for (path in stickerList) {
                    try {
                        // Mở file trong assets
                        val inputStream = assets.open(path)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()

                        // Lưu từng ảnh ra gallery
                        saveBitmapToExternalStorage(bitmap)
                        successCount++
                    } catch (e: Exception) {
                        Log.e("DownloadError", "Cannot save $path: ${e.message}")
                    }
                }

                launch(Dispatchers.Main) {
                    dialogLoading.dismiss()
                    if (successCount > 0) showToast(getString(R.string.download_success))
                    else showToast(getString(R.string.error))
                }

            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    dialogLoading.dismiss()
                    showToast(getString(R.string.error))
                }
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, R.string.granted_storage, Toast.LENGTH_SHORT).show()
            } else {
                setStoragePermission(this, (isStoragePermission(this) + 1))
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.granted_notification, Toast.LENGTH_SHORT).show()
            } else {
                setNotificationPermission(this, (isNotificationPermission(this) + 1))
            }
        }
    }


    private fun shareImagesToApp(packageNames: List<String>, appName: String) {
        dialogLoading.show()

        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            dialogLoading.dismiss()
            Log.e("ShareError", "Error: ${throwable.message}")
            showToast(getString(R.string.error))
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            try {
                val imageUris = ArrayList<Uri>()

                // Chuyển từng ảnh trong assets sang file tạm cache
                for (path in stickerList) {
                    try {
                        assets.open(path).use { input ->
                            val bitmap = BitmapFactory.decodeStream(input)
                            val file = File(cacheDir, "sticker_${System.currentTimeMillis()}.png")
                            FileOutputStream(file).use { out ->
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                            }

                            val uri = FileProvider.getUriForFile(this@ItemStickerActivity, "${applicationContext.packageName}.fileprovider", file)
                            imageUris.add(uri)
                        }
                    } catch (e: Exception) {
                        Log.e("ShareError", "Error processing $path: ${e.message}")
                    }
                }

                withContext(Dispatchers.Main) {
                    dialogLoading.dismiss()

                    if (imageUris.isEmpty()) {
                        showToast(getString(R.string.error))
                        return@withContext
                    }

                    // Tạo intent chia sẻ
                    var targetIntent: Intent? = null
                    for (pkg in packageNames) {
                        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                            type = "image/png"
                            putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            setPackage(pkg)
                        }
                        if (intent.resolveActivity(packageManager) != null) {
                            targetIntent = intent
                            break
                        }
                    }

                    if (targetIntent != null) {
                        try {
                            startActivity(targetIntent)
                        } catch (e: Exception) {
                            showToast("Không thể mở $appName.")
                        }
                    } else {
                        showToast("$appName chưa được cài đặt!")
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    dialogLoading.dismiss()
                    showToast(getString(R.string.error))
                }
            }
        }
    }

    private fun handleShareTelegram() {

        val telegramPackages = listOf("org.telegram.messenger", "org.thunderdog.challegram")
        shareImagesToApp(telegramPackages, "Telegram")
    }

    private fun handleShareWhatsapp() {
        val whatsappPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
        shareImagesToApp(whatsappPackages, "WhatsApp")
    }

    private fun handleRcv() {
        binding.apply {
            stickerAdapter.onItemClick = { path, position ->

                dialogSticker.show()
                dialogSticker.binding.imvImage.setImageDrawable(null)

                val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
                    throwable.printStackTrace()
                    Toast.makeText(this@ItemStickerActivity, "Lỗi load ảnh!", Toast.LENGTH_SHORT).show()
                }

                CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {

                    bitmap = Glide.with(this@ItemStickerActivity)
                        .asBitmap()
                        .load("file:///android_asset/$path")
                        .submit()
                        .get()


                    withContext(Dispatchers.Main) {
                        dialogSticker.binding.imvImage.setImageBitmap(bitmap)
                    }
                }

                // Nút download
                dialogSticker.onDownloadClick = {
                    handleRightBot1()
                }

                // Nút share
                dialogSticker.onShareClick = {
                   handleShare(this@ItemStickerActivity,bitmap)
                }

                // Nút đóng
                dialogSticker.onDismissClick = {
                    dialogSticker.dismiss()
                }
            }
        }
    }

    private fun handleDownload1() {
        setLocale(this@ItemStickerActivity)
        dialogLoading.show()
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            dialogLoading.dismiss()
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            val job1 = async {
                saveBitmapToExternalStorage(bitmap)
                return@async true
            }
            launch(Dispatchers.Main) {
                if (job1.await()) {
                    dialogLoading.dismiss()
                    showToast(getString(R.string.download_success))
                    hideNavigation()
                }
            }
        }
    }

    private fun downloadSticker(){

    }
    private fun shareSticker(){

    }



}