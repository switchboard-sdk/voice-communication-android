package com.synervoz.duckingandsoundeffectsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.lifecycle.lifecycleScope
import com.synervoz.switchboard.sdk.SwitchboardSDK
import com.synervoz.switchboardagora.AgoraExtension
import com.synervoz.duckingandsoundeffectsapp.databinding.ActivityMainBinding
import com.synervoz.duckingandsoundeffectsapp.ui.room.RoomFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        SwitchboardSDK.initialize(this, Config.clientID, Config.clientSecret)
        AgoraExtension.initialize(Config.agoraAppID)

        if (!requestPermission()) return
        if (savedInstanceState == null) {
            lifecycleScope.launch {
                supportFragmentManager.commit {
                    replace<RoomFragment>(R.id.container)
                    setReorderingAllowed(true)
                }
            }
        }
    }

    private fun requestPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, permissions, 0)
                return false
            }
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != 0 || grantResults.isEmpty() || grantResults.size != permissions.size) return
        var hasAllPermissions = true

        for (grantResult in grantResults)
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                hasAllPermissions = false
                Toast.makeText(
                    applicationContext,
                    "Please allow all permissions for the app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        if (hasAllPermissions) {
            lifecycleScope.launch {
                supportFragmentManager.commit {
                    replace<RoomFragment>(R.id.container)
                    setReorderingAllowed(true)
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        supportFragmentManager.popBackStack()
    }
}
