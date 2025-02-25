package com.example.africanschools.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.africanschools.ui.theme.AfricanSchoolsTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            AfricanSchoolsTheme {
                val navController = rememberNavController()
Scaffold(
    bottomBar = {
        BottomNavigation {
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                selected = navController.currentDestination?.route == "home",
                onClick = { navController.navigate("home") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Chat, contentDescription = "AI Assistant") },
                label = { Text("AI Assistant") },
                selected = navController.currentDestination?.route == "ai_assistant",
                onClick = { navController.navigate("ai_assistant") }
            )
        }
    }
) { paddingValues ->
    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = Modifier.padding(paddingValues)
    ) {
        composable("login") { 
            LoginScreen(
                googleSignInClient = googleSignInClient,
                onLoginSuccess = { 
                    // Set default trial account
                    if (googleSignInClient.signInIntent == null) {
                        // Use trial account
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                defaultEmail = "medowael91@gmail.com",
                defaultPassword = "messi12345"
            ) 
        }
        composable("home") { HomeScreen() }
        composable("ai_assistant") { AIAssistantScreen() }
    }
}

            }
        }
    }
}
