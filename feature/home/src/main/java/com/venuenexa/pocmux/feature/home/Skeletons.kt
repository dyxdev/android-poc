package com.venuenexa.pocmux.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.venuenexa.pocmux.core.designsystem.ShimmerBox
import com.venuenexa.pocmux.core.designsystem.spacing

private val FeaturedSkeletonWidth = 280.dp
private val ThumbnailSkeleton = 104.dp
private val LineHeight = 14.dp
private const val SKELETON_PHOTO_ASPECT = 1.8f
private const val SHORT_LINE = 0.5f
private const val LONG_LINE = 0.8f

// Esqueletos con la misma forma que las tarjetas reales para que la carga no mueva el contenido.
@Composable
fun FeaturedSkeleton(modifier: Modifier = Modifier) {
    SkeletonCard(modifier = modifier.width(FeaturedSkeletonWidth), shape = MaterialTheme.shapes.large) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            ShimmerBox(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(SKELETON_PHOTO_ASPECT)
                    .clip(MaterialTheme.shapes.small),
            )
            SkeletonLines()
        }
    }
}

@Composable
fun ListSkeleton(modifier: Modifier = Modifier) {
    SkeletonCard(modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            ShimmerBox(Modifier.size(ThumbnailSkeleton).clip(MaterialTheme.shapes.small))
            SkeletonLines(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SkeletonCard(
    modifier: Modifier,
    shape: Shape,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        content()
    }
}

@Composable
private fun SkeletonLines(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        ShimmerBox(Modifier.fillMaxWidth(LONG_LINE).height(LineHeight).clip(MaterialTheme.shapes.extraSmall))
        ShimmerBox(Modifier.fillMaxWidth(SHORT_LINE).height(LineHeight).clip(MaterialTheme.shapes.extraSmall))
        ShimmerBox(Modifier.fillMaxWidth(SHORT_LINE).height(LineHeight).clip(MaterialTheme.shapes.extraSmall))
    }
}
