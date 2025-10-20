package com.example.st181_halloween_maker.ui.success

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import com.example.st181_halloween_maker.core.dialog.LoadingDialog
import com.example.st181_halloween_maker.core.extensions.checkPermissions
import com.example.st181_halloween_maker.core.extensions.goToSettings
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.handleComeBackHome
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
import com.example.st181_halloween_maker.databinding.ActivitySuccessBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    private var path: String = ""
    private lateinit var bitmap: Bitmap
    private lateinit var dialogLoading: LoadingDialog
    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initData()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
            btnHome.onSingleClick {
                handleComeBackHome(this@SuccessActivity)
            }
            btnShare.onSingleClick(1500) {
                handleShare(this@SuccessActivity, bitmap)
            }
            btnDowLoad.onSingleClick {
                handleRightBot()
            }
//            btnMyAlbum.onSingleClick {
//                val intent = Intent(this@SuccessActivity, MyCreationActivity::class.java)
//                intent.putExtra(FROM_SAVE, FROM_SAVE)
//                startActivity(intent)
//                finish()
//            }
        }
    }

    override fun initText() {
        binding.apply {
            txtShare.select()
            txtDownLoad.select()
        }
    }
    private fun initData() {
        binding.apply {
            val getPath = intent.getStringExtra(PATH_KEY)
            dialogLoading = LoadingDialog(this@SuccessActivity)
            getPath.let {
                path = it!!
                setLocale(this@SuccessActivity)
                dialogLoading.show()
                val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
                    dialogLoading.dismiss()
                }

                CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
                    val job1 = async {
                        bitmap = Glide.with(this@SuccessActivity).load(path).submit().get().toBitmap()
                        return@async true
                    }

                    launch(Dispatchers.Main) {
                        if (job1.await()) {
                            imvImage.setImageBitmap(bitmap)
                            dialogLoading.dismiss()
                            hideNavigation()
                        }
                    }
                }
            }

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

    private fun handleDownload() {
        setLocale(this@SuccessActivity)
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

}