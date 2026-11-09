package com.example.incometracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.incometracker.backup.DriveBackupWorker
import com.example.incometracker.ui.ExpenseScreen
import com.example.incometracker.ui.IncomeScreen
import com.example.incometracker.ui.theme.IncomeExpenseTrackerTheme
import com.example.incometracker.viewmodel.ExpenseViewModel
import com.example.incometracker.viewmodel.IncomeViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

class MainActivity : ComponentActivity() {

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            Toast.makeText(this, "Google Drive Connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sign-in failed. Please retry.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as TrackerApplication
        val incomeDao = app.database.incomeDao()
        val expenseDao = app.database.expenseDao()

        setContent {
            IncomeExpenseTrackerTheme {
                val incomeViewModel: IncomeViewModel = viewModel { IncomeViewModel(incomeDao) }
                val expenseViewModel: ExpenseViewModel = viewModel { ExpenseViewModel(expenseDao) }
                var currentNav by remember { mutableStateOf("income") }

                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Income & Expense Manager") },
                            actions = {
                                IconButton(onClick = { requestGoogleDriveSignIn() }) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = "Connect Drive")
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentNav == "income",
                                onClick = { currentNav = "income" },
                                icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Income") },
                                label = { Text("Income") }
                            )
                            NavigationBarItem(
                                selected = currentNav == "expense",
                                onClick = { currentNav = "expense" },
                                icon = { Icon(Icons.Default.TrendingDown, contentDescription = "Expense") },
                                label = { Text("Expense") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(modifier = Modifier.padding(innerPadding)) {
                        when (currentNav) {
                            "income" -> IncomeScreen(viewModel = incomeViewModel)
                            "expense" -> ExpenseScreen(viewModel = expenseViewModel)
                        }
                    }
                }
            }
        }
    }

    private fun requestGoogleDriveSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        val client = GoogleSignIn.getClient(this, gso)
        signInLauncher.launch(client.signInIntent)
    }
}
