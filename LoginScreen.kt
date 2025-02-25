package com.example.africanschools.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(
    googleSignInClient: GoogleSignInClient,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    defaultEmail: String = "medowael91@gmail.com",
    defaultPassword: String = "messi12345"
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                onLoginSuccess()
            } catch (e: ApiException) {
                // Handle error
            }
        }
    }

    var email by remember { mutableStateOf(defaultEmail) }
    var password by remember { mutableStateOf(defaultPassword) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login", style = MaterialTheme.typography.h5)
        
        if (error != null) {
            Text(
                text = error!!, 
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        Button(
            onClick = {
                loading = true
                error = null
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        loading = false
                        onLoginSuccess()
                    }
                    .addOnFailureListener { e ->
                        loading = false
                        error = e.message ?: "Login failed"
                    }
            },
            enabled = !loading && email.isNotEmpty() && password.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(if (loading) "Logging in..." else "Login")
        }

        TextButton(
            onClick = onRegisterClick,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Register")
        }
    }
}
