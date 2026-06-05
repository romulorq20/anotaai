package br.com.rrrqueiroz.notas.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import br.com.rrrqueiroz.notas.presentation.navigation.NavHost
import br.com.rrrqueiroz.notas.presentation.theme.AnotaAITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnotaAITheme {
                NavHost(rememberNavController())
            }
        }
    }
}
