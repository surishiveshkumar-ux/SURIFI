package com.drs.surifi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.drs.surifi.localsend.LocalSendEngine
import com.drs.surifi.netmerge.MultiPathBondingEngine
import com.drs.surifi.ui.SurifiPortalApp

class MainActivity : ComponentActivity() {

    private lateinit var bondingEngine: MultiPathBondingEngine
    private lateinit var localSendEngine: LocalSendEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        bondingEngine = MultiPathBondingEngine(applicationContext)
        localSendEngine = LocalSendEngine(applicationContext)
        setContent {
            SurifiPortalApp(bondingEngine = bondingEngine, localSendEngine = localSendEngine)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            bondingEngine.stopBonding()
            localSendEngine.stopLocalSendRadar()
        }
    }
}
