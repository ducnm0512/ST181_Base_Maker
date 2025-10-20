package com.example.st181_halloween_maker.ui.view

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.dialog.ConfirmDialog
import com.example.st181_halloween_maker.core.dialog.LoadingDialog
import com.example.st181_halloween_maker.core.extensions.checkPermissions
import com.example.st181_halloween_maker.core.extensions.dLog
import com.example.st181_halloween_maker.core.extensions.goToSettings
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.handleShare
import com.example.st181_halloween_maker.core.extensions.hideNavigation
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.requestPermission
import com.example.st181_halloween_maker.core.extensions.saveBitmapToExternalStorage
import com.example.st181_halloween_maker.core.extensions.select
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.utils.KeyApp.NOTIFICATION_PERMISSION_CODE
import com.example.st181_halloween_maker.core.utils.KeyApp.PATH_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.STORAGE_PERMISSION_CODE
import com.example.st181_halloween_maker.core.utils.SystemUtils.isNotificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.isStoragePermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.setLocale
import com.example.st181_halloween_maker.core.utils.SystemUtils.setNotificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.setStoragePermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.storagePermission
import com.example.st181_halloween_maker.databinding.ActivityViewBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private var path: String = ""
    private lateinit var bitmap: Bitmap
    private lateinit var dialogLoading: LoadingDialog
    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initData()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
            btnDelete.onSingleClick {
                handleDelete()
            }
            btnDowLoad.onSingleClick {
                handleRightBot()
            }
            btnShare.onSingleClick {
                handleShare(this@ViewActivity, bitmap)
            }
        }
    }

    override fun initText() {
        binding.apply {
            txtShare.select()
            txtDownLoad.select()
        }
    }
    private fun initData(){
        binding.apply {
            val getPath = intent.getStringExtra(PATH_KEY)
            dialogLoading = LoadingDialog(this@ViewActivity)
            getPath.let {
                path = it!!
                setLocale(this@ViewActivity)
                dialogLoading.show()
                val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
                    dialogLoading.dismiss()
                }

                CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
                    val job1 = async {
                        bitmap = Glide.with(this@ViewActivity).load(path).submit().get().toBitmap()
                        return@async true
                    }

                    launch(Dispatchers.Main) {
                        if (job1.await()) {
                            imvImage.setImageBitmap(bitmap)
                            dialogLoading.dismiss()
                            hideNavigation(true)
                        }
                    }
                }
            }

        }
    }
    private fun handleRightBot() {
        checkPermission()
    }
    private fun checkPermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (this.checkPermissions(storagePermission)) {
                handleDownload()
            } else {
                if (isStoragePermission(this) < 2 && !this.checkPermissions(storagePermission)) {
                    this.requestPermission(storagePermission, STORAGE_PERMISSION_CODE)
                } else {
                    goToSettings()
                }
            }
        } else {
            handleDownload()
        }
    }
    private fun handleDownload() {
        setLocale(this@ViewActivity)
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
                    hideNavigation(true)
                }
            }
        }
    }
    private fun handleDelete() {
        val dialogDelete = ConfirmDialog(this@ViewActivity, R.string.delete, R.string.do_you_want_to_delete)
        setLocale(this@ViewActivity)
        dialogDelete.show()

        dialogDelete.binding.btnNo.onSingleClick {
            dialogDelete.dismiss()
            hideNavigation(true)
        }

        dialogDelete.binding.btnYes.onSingleClick {
            val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
                dialogDelete.dismiss()
            }
            CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
                val job1 = async {
                    val file = File(path)
                    if (file.exists() && file.delete()) {
                        dLog("Delete Successfully")
                    }
                    return@async true
                }
                launch(Dispatchers.Main) {
                    if (job1.await()) {
                        dialogDelete.dismiss()
                        hideNavigation(true)
                        finish()
                    }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
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

}