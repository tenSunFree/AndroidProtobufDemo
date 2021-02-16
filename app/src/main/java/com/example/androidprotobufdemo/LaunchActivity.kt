package com.example.androidprotobufdemo

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_launch.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        setContentView(R.layout.activity_launch)
        initLaunchModel()
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window: Window = window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun initLaunchModel() {
        val (formatDate: String, path, fileName) = initData()
        try {
            val model = LaunchModel.FirstStart.parseFrom(FileInputStream(File(path, fileName)))
            when (model.startType) {
                LaunchModel.FirstStart.StartType.SECOND_START -> {
                    saveLaunchModel(LaunchModel.FirstStart.StartType.THIRD_START,
                            model.firstStartDate, path, fileName)
                    Toast.makeText(this, "第二次啟動 一秒後跳轉, " +
                            "安裝時間:${model.firstStartDate}", Toast.LENGTH_LONG).show()
                    image_view.postDelayed({
                        startActivity(MainActivity.createMainActivity(this))
                        finish()
                    }, 1000)
                }
                LaunchModel.FirstStart.StartType.THIRD_START -> {
                    Toast.makeText(this, "安裝時間:${model.firstStartDate}",
                            Toast.LENGTH_LONG).show()
                    startActivity(MainActivity.createMainActivity(this))
                    finish()
                }
                else -> finish()
            }
        } catch (e1: IOException) {
            Toast.makeText(this, "第一次啟動, 安裝時間:$formatDate", Toast.LENGTH_LONG).show()
            saveLaunchModel(LaunchModel.FirstStart.StartType.SECOND_START,
                    formatDate, path, fileName)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun initData(): Triple<String, String?, String> {
        val timeMillis = System.currentTimeMillis()
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date(timeMillis)
        val formatDate: String = format.format(date)
        val path = getFilePath()
        val fileName = "LaunchModel"
        return Triple(formatDate, path, fileName)
    }

    private fun getFilePath(): String? {
        val fileDire: File = filesDir
        val filePath: String = fileDire.toString()
        return filePath.substring(0, filePath.lastIndexOf("/"))
    }

    private fun saveLaunchModel(type: LaunchModel.FirstStart.StartType, date: String,
                                path: String?, fileName: String) {
        LaunchModel.FirstStart.newBuilder()
                .setStartType(type)
                .setFirstStartDate(date)
                .build()
                .writeTo(FileOutputStream(File(path, fileName)))
    }
}