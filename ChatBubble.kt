package com.example.africanschools.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity

@Composable
fun ChatBubble(message: Message) {
    val backgroundColor = if (message.isUser) {
        MaterialTheme.colors.primary
    } else {
        MaterialTheme.colors.surface
    }
    
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }
    
    val density = LocalDensity.current
    val animatedScale = remember { Animatable(0.8f) }
    
    LaunchedEffect(message) {
        animatedScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = horizontalAlignment
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .graphicsLayer {
                    scaleX = animatedScale.value
                    scaleY = animatedScale.value
                    alpha = animatedScale.value
                }
        ) {
            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(
                        color = backgroundColor,
                        shape = bubbleShape
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.body1,
                    color = if (message.isUser) {
                        Color.White
                    } else {
                        MaterialTheme.colors.onSurface
                    },
                    modifier = Modifier.widthIn(max = 256.dp)
                )
            }
            
            // Timestamp
            Text(
                text = "Just now", // You can add actual timestamp to Message data class
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp),
                textAlign = if (message.isUser) TextAlign.End else TextAlign.Start
            )
        }
    }
}
