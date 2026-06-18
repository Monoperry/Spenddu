package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun DuolingoCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    borderColor: Color = DuoDarkGray,
    shadowColor: Color = DuoShadowGray,
    borderWidth: Dp = 2.dp,
    shadowDepth: Dp = 4.dp,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .then(clickModifier)
            .padding(bottom = shadowDepth),
        propagateMinConstraints = true
    ) {
        // Shadow Layer (placed underneath, shifted down)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowDepth)
                .background(shadowColor, shape)
        )
        // Content Layer
        Box(
            modifier = Modifier
                .background(backgroundColor, shape)
                .border(borderWidth, borderColor, shape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun DuolingoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DuoGreen,
    shadowColor: Color = DuoGreenShadow,
    borderColor: Color = DuoDarkGray,
    borderWidth: Dp = 2.dp,
    shadowDepth: Dp = 4.dp,
    enabled: Boolean = true,
    testTag: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Tactile push down feeling
    val yOffset by animateDpAsState(
        targetValue = if (isPressed) shadowDepth else 0.dp,
        label = "button_press_offset"
    )

    val customModifier = modifier
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .clickable(
            enabled = enabled,
            interactionSource = interactionSource,
            indication = null, // Disable standard ripple for custom 3D bounce
            onClick = onClick
        )
        .padding(bottom = shadowDepth)

    Box(
        modifier = customModifier,
        propagateMinConstraints = true
    ) {
        // Shadow Layer (Always fixed at bottom)
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = shadowDepth)
                .background(
                    if (enabled) shadowColor else DuoShadowGray,
                    RoundedCornerShape(16.dp)
                )
        )
        // Main Button Surface (Moves down when pressed)
        Box(
            modifier = Modifier
                .offset(y = yOffset)
                .background(
                    if (enabled) backgroundColor else DuoBorderGray,
                    RoundedCornerShape(16.dp)
                )
                .border(
                    borderWidth,
                    if (enabled) borderColor else DuoShadowGray,
                    RoundedCornerShape(16.dp)
                )
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

@Composable
fun DuolingoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    testTag: String? = null
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DuoDarkGray,
                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
            )
        }
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DuoDarkGray,
                unfocusedTextColor = DuoDarkGray,
                focusedContainerColor = DuoLightGray,
                unfocusedContainerColor = DuoLightGray,
                focusedBorderColor = DuoBlue,
                unfocusedBorderColor = DuoBorderGray,
                focusedLabelColor = DuoBlue,
                unfocusedLabelColor = DuoDarkGray,
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .then(tagModifier)
                .border(2.dp, DuoBorderGray, RoundedCornerShape(16.dp))
        )
    }
}

@Composable
fun DuolingoTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null
) {
    val backgroundColor = if (selected) DuoBlue else Color.White
    val borderColor = DuoDarkGray
    val shadowColor = if (selected) DuoBlueShadow else DuoShadowGray
    val textColor = if (selected) Color.White else DuoDarkGray

    DuolingoCard(
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        shadowColor = shadowColor,
        borderWidth = 2.dp,
        shadowDepth = 4.dp,
        shape = RoundedCornerShape(14.dp),
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}
