package com.dz.phantomkit

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * DZ PhantomKit — a branded front-end/dashboard for a rootless NetHunter setup.
 *
 * This app does NOT reimplement any penetration-testing tools itself. It only:
 *  1) helps the user install the official, open-source Kali NetHunter Rootless
 *     environment inside Termux, and
 *  2) launches that environment via Termux's own documented automation API
 *     (the same one used by Termux:Tasker / Termux:Widget).
 *
 * See README.md in this project for build instructions, licensing notes, and
 * the trademark considerations around the underlying Kali/NetHunter project.
 */
class MainActivity : AppCompatActivity() {

    private val termuxPackage = "com.termux"
    private val termuxRunCommandService = "com.termux.app.RunCommandService"
    private val termuxBin = "/data/data/com.termux/files/usr/bin/bash"
    private val termuxHome = "/data/data/com.termux/files/home"

    // Official Kali NetHunter rootless installer, run inside Termux.
    // Source: https://www.kali.org/docs/nethunter/nethunter-rootless/
    private val installCommands = """
        termux-setup-storage
        pkg install wget -y
        wget -O install-nethunter-termux https://offs.ec/2MceZWr
        chmod +x install-nethunter-termux
        ./install-nethunter-termux
    """.trimIndent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val statusText = findViewById<TextView>(R.id.statusText)
        statusText.text = if (isTermuxInstalled()) {
            getString(R.string.status_ready)
        } else {
            getString(R.string.status_termux_missing)
        }

        findViewById<Button>(R.id.btnCopyInstall).setOnClickListener { copyInstallCommands() }
        findViewById<Button>(R.id.btnOpenTermux).setOnClickListener { openTermux() }
        findViewById<Button>(R.id.btnLaunchCli).setOnClickListener { runInTermux("nethunter") }
        findViewById<Button>(R.id.btnLaunchKex).setOnClickListener { runInTermux("nethunter kex") }
    }

    private fun isTermuxInstalled(): Boolean = try {
        packageManager.getPackageInfo(termuxPackage, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun copyInstallCommands() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("nethunter-install", installCommands))
        Toast.makeText(this, R.string.install_copied, Toast.LENGTH_LONG).show()
    }

    private fun openTermux() {
        val launchIntent = packageManager.getLaunchIntentForPackage(termuxPackage)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, R.string.status_termux_missing, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Sends a command to Termux via its official RunCommandService. This requires
     * the user to set allow-external-apps=true in ~/.termux/termux.properties
     * inside Termux first (documented in README.md) — without that, Termux will
     * reject the intent.
     */
    private fun runInTermux(command: String) {
        if (!isTermuxInstalled()) {
            Toast.makeText(this, R.string.status_termux_missing, Toast.LENGTH_LONG).show()
            return
        }
        try {
            val intent = Intent()
            intent.setClassName(termuxPackage, termuxRunCommandService)
            intent.action = "com.termux.RUN_COMMAND"
            intent.putExtra("com.termux.RUN_COMMAND_PATH", termuxBin)
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", arrayOf("-c", command))
            intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", termuxHome)
            intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false)
            intent.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION", "0")
            startService(intent)
        } catch (e: Exception) {
            Toast.makeText(this, e.message ?: "Could not reach Termux", Toast.LENGTH_LONG).show()
        }
    }
}
