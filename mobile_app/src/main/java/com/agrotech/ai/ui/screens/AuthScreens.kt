package com.agrotech.ai.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.agrotech.ai.ui.components.AgroButton
import com.agrotech.ai.ui.components.AgroTextField
import com.agrotech.ai.ui.navigation.Screen
import com.agrotech.ai.viewmodel.AgroViewModel
import kotlinx.coroutines.launch
import com.agrotech.ai.ui.theme.LocalAppStrings

@Composable
fun LoginScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var mobileNumber by remember { mutableStateOf("") }
    
    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState).imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(strings.appName, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(strings.welcome, style = MaterialTheme.typography.titleMedium)
                
                Spacer(modifier = Modifier.height(48.dp))
                
                AgroTextField(
                    value = mobileNumber, 
                    onValueChange = { mobileNumber = it }, 
                    label = "Mobile Number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    AgroButton(text = strings.login, onClick = { 
                        if (mobileNumber.isNotEmpty()) {
                            android.util.Log.d("AUTH", "Login attempt: $mobileNumber")
                            viewModel.login(mobileNumber) { error ->
                                if (error == null) {
                                    android.util.Log.d("AUTH", "Login SUCCESS")
                                    navController.navigate(Screen.LanguageSelector.route)
                                } else {
                                    android.util.Log.e("AUTH", "Login FAILED: $error")
                                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Please enter mobile number", android.widget.Toast.LENGTH_SHORT).show()
                            scope.launch { snackbarHostState.showSnackbar("Please enter mobile number") }
                        }
                    })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.dontHaveAccount)
                    TextButton(onClick = { navController.navigate(Screen.Signup.route) }) {
                        Text(strings.signup)
                    }
                }
            }
        }
    }
}

@Composable
fun SignupScreen(navController: NavController, viewModel: AgroViewModel) {
    val strings = LocalAppStrings.current
    var name by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(scrollState).imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(strings.joinAgro, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(strings.empoweringFarmers, style = MaterialTheme.typography.bodySmall)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                AgroTextField(value = name, onValueChange = { name = it }, label = strings.fullName)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                AgroTextField(
                    value = mobileNumber, 
                    onValueChange = { mobileNumber = it }, 
                    label = "Mobile Number",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    AgroButton(text = strings.signup, onClick = { 
                        if (name.isNotEmpty() && mobileNumber.isNotEmpty()) {
                            android.util.Log.d("AUTH", "Signup attempt: $name, $mobileNumber")
                            viewModel.signup(name, mobileNumber) { error ->
                                if (error == null) {
                                    android.util.Log.d("AUTH", "Signup SUCCESS")
                                    navController.navigate(Screen.LanguageSelector.route)
                                } else {
                                    android.util.Log.e("AUTH", "Signup FAILED: $error")
                                    android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                }
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Please fill all fields", android.widget.Toast.LENGTH_SHORT).show()
                            scope.launch { snackbarHostState.showSnackbar("Please fill all fields") }
                        }
                    })
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { navController.popBackStack() }) {
                    Text(strings.alreadyHaveAccount)
                }
            }
        }
    }
}
