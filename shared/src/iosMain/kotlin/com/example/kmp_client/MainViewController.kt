package com.example.kmp_client

import com.example.kmp_client.di.App
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewControlle(): UIViewController = ComposeUIViewController { App() }