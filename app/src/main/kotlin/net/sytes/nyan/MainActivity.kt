package net.sytes.nyan

import android.content.BroadcastReceiver
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

import android.os.Bundle
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.view.ContextThemeWrapper
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import net.sytes.nyan.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding
    private lateinit var button: AppCompatButton

    private val connectionErrorReceiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            button.text = when(intent.action){
                "StartStream" -> getString(R.string.streamButtonStream)
                "StopStream" -> getString(R.string.streamButton)
                "ConnectionError" -> getString(R.string.streamButtonCannotConnect)
                else -> ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        button = binding.streamButton

        if(SendService.enabled())
            button.text = getString(R.string.streamButtonStream)
        else
            button.text = getString(R.string.streamButton)

        button.setOnClickListener { _ -> stream() }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            connectionErrorReceiver,
            IntentFilter().apply {
                addAction("StartStream")
                addAction("ConnectionError")
                addAction("StopStream")
            }
        )
    }

    private fun checkPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true

        button.text = getString(R.string.streamButtonError)
        button.background =
            AppCompatButton(ContextThemeWrapper(this, R.style.AndroidTest_button_error), null, 0).background

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECORD_AUDIO), 0)

        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                button.text = getString(R.string.streamButton)

            button.background =
                AppCompatButton(ContextThemeWrapper(this, R.style.AndroidTest_button), null, 0).background
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionErrorReceiver)
    }

    private fun stream() {
        if (checkPermission()) {
            if (!SendService.enabled()) {
                startService(Intent(this, SendService::class.java))
            } else {
                stopService(Intent(this, SendService::class.java))
            }
        }
    }
}
