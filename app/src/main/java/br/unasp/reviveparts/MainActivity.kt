package br.unasp.reviveparts

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import br.unasp.reviveparts.ui.nav.AppNavHost
import br.unasp.reviveparts.ui.theme.RevivepartsTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RevivepartsTheme { AppNavHost() } }
    }
}
