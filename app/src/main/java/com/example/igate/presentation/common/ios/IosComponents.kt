package com.example.igate.presentation.common.ios

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.igate.theme.BrandBlue

// iOS Standard Animation Specs
fun <T> iosSpring() = spring<T>(
    dampingRatio = 0.85f, // Slightly bouncy, very smooth
    stiffness = Spring.StiffnessMediumLow
)

// Glassmorphism / Frosted Glass Effect Modifier
fun Modifier.iosGlassmorphism(
    blurRadius: Float = 16f,
    surfaceColor: Color = Color(0x99F7F7F8), // Semi-transparent light mode
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = this
    .clip(shape)
    .blur(blurRadius.dp)
    .background(surfaceColor)


// Cupertino-style Segmented Control
@Composable
fun IosSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFFE5E5EA), RoundedCornerShape(12.dp)) // iOS light grey track
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    // Selection background indicator (animated)
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White, RoundedCornerShape(8.dp))
                                // Subtle shadow for depth mimicking iOS
                                .background(Color(0x10000000), RoundedCornerShape(8.dp)) 
                        )
                    }

                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isSelected) Color.Black else Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
