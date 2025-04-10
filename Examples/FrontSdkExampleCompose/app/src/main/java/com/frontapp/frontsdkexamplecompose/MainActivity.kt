package com.frontapp.frontsdkexamplecompose


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack

import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.frontapp.frontsdkexamplecompose.ui.theme.FrontSdkExampleComposeTheme
import com.frontapp.frontsdk.FrontChatConfig
import com.frontapp.frontsdk.FrontChatView
import com.frontapp.frontsdk.FrontChatViewModel
import com.frontapp.frontsdk.LoadingStatus
import com.frontapp.frontsdk.RouteName

class MainActivity : ComponentActivity() {
    private val viewModel = FrontChatViewModel()
    private var isShowingFrontChat by mutableStateOf(false)

    // Set to the value of your chat id obtained from the code snippet shown here:
    // https://help.front.com/en/articles/2049#install_front_chat_on_your_website
    private val chatId = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FrontSdkExampleComposeTheme {
                MainScreen()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isShowingFrontChat) {
                    isShowingFrontChat = false
                } else {
                    finish()
                }
            }
        })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val didEncounterUnrecoverableError by viewModel.didEncounterUnrecoverableError.observeAsState(false)
        val loadingStatus by viewModel.loadingStatus.observeAsState(LoadingStatus.IDLE)

        Box(modifier = Modifier.fillMaxSize()) {
            if (loadingStatus == LoadingStatus.FAILED_LOADING) { // Example of how to handle failed loading
                FailedToLoadDialog()
            } else if (didEncounterUnrecoverableError == true) { // Example of how to handle an exception from the webview that FrontChatView uses
                UnrecoverableExceptionDialog()
            } else {
                LaunchFrontChatButton()

                AnimatedFrontChatView()
            }
        }
    }

    @Composable
    fun LaunchFrontChatButton() {
        // A simple button to launch FrontChatView
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { isShowingFrontChat = true }) {
                viewModel.reset() // Reset the view model state
                Text("Open Chat")
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AnimatedFrontChatView() {
        AnimatedVisibility(
            visible = isShowingFrontChat,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        ) {
            Scaffold(
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text("Welcome") },
                        navigationIcon = {
                            IconButton(onClick = {
                                // Example of how to switch to the user's conversation history
                                viewModel.navigate(route = RouteName.CONVERSATION_HISTORY)
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                // Example of how to end the current conversation and start a new one for the user
                                viewModel.restart()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh"
                                )
                            }
                            IconButton(onClick = { isShowingFrontChat = false }) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand"
                                )
                            }
                        })
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)
                ) {
                    val config = FrontChatConfig(
                        chatId = chatId
                        // To test with verified users, uncomment below and set values as described here:
                        // https://dev.frontapp.com/docs/identify-users
                        /*
                        ,email = "<user email>",
                        userHash = "<user hash generated by your backend using the verification secret",
                        name = "<user name>",
                        customFields = mapOf(
                            "custom_field_1" to CustomFieldValue.StringValue("value_1")
                        ),
                        contact = Contact(
                            email = "<user email>",
                            customFields = mapOf(
                                "custom_field_1" to CustomFieldValue.StringValue("value_1")
                            )
                        )
                        */
                    )

                    FrontChatView(config = config, viewModel = viewModel)
                }

            }
        }
    }
    @Composable
    fun FailedToLoadDialog() {
        AlertDialog(

            title = {
                Text(text = "Error")
            },
            text = {
                Text(text = "Failed to load the chat.")
            },
            onDismissRequest = {
                isShowingFrontChat = false
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.reset(); isShowingFrontChat = false }
                ) {
                    Text("Ok")
                }
            }
        )
    }

    @Composable
    fun UnrecoverableExceptionDialog() {
        AlertDialog(
            title = {
                Text(text = "Error")
            },
            text = {
                Text(text = viewModel.unrecoverableErrorMessage.value ?: "An unknown error occurred.")
            },
            onDismissRequest = {
                viewModel.reset()
                isShowingFrontChat = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.reset()
                        viewModel.restart()
                    }
                ) {
                    Text("Start new chat")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        isShowingFrontChat = false
                        viewModel.reset()
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}
