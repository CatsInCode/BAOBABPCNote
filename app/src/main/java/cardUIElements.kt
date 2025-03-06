package com.example.bar

import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

data class cardUIElements(
    val cardId: String, // ID карточки
    val addButton: Button,
    val spinner: Spinner,
    val priceTextView: TextView,
    val editButton: ImageView,
)