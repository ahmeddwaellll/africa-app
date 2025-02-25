package com.example.africanschools.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Register", style = MaterialTheme.typography.h5)
        TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
        TextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation())
        TextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, visualTransformation = PasswordVisualTransformation())
        Button(onClick = {
            if (password == confirmPassword) {
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener { onRegisterSuccess() }
                    .addOnFailureListener { /* Handle error */ }
            } else {
                // Handle password mismatch
            }
        }) {
            Text("Register")
        }
    }
}
