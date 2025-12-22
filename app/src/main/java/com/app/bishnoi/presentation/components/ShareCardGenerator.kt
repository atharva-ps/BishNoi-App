package com.app.bishnoi.presentation.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.app.bishnoi.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap

object ShareCardGenerator {

    suspend fun generateShareCard(
        context: Context,
        postImageUrl: String,
        postFormat: String,
        userProfileUrl: String?,
        userName: String,
        userDesignation: String?,
        userCity: String?,
        userState: String?
    ): File? = withContext(Dispatchers.IO) {
        try {
            // Card dimensions (Instagram post size)
            val cardWidth: Int
            val cardHeight: Int

            if (postFormat == "VERTICAL") {
                cardWidth = 1080
                cardHeight = 1350   // 4:5
            } else {
                cardWidth = 1080
                cardHeight = 566    // 1.91:1
            }

            // Create bitmap
            val bitmap = Bitmap.createBitmap(cardWidth, cardHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Load images
            val postBitmap = loadImageFromUrl(context, postImageUrl)
            val profileBitmap = userProfileUrl?.let { loadImageFromUrl(context, it) }
            val logoBitmap = loadLogoFromResources(context)

            if (postBitmap == null) return@withContext null

            // Draw background (post image)
            drawPostImage(canvas, postBitmap, cardWidth, cardHeight)

            // Draw bottom info card
            drawInfoCard(
                canvas,
                profileBitmap,
                userName,
                userDesignation,
                userCity,
                userState,
                cardWidth,
                cardHeight
            )

            // Draw app logo
            drawAppLogo(canvas, logoBitmap, cardWidth, cardHeight)

            // Save to cache
            val file = File(context.cacheDir, "share_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }

            bitmap.recycle()
            postBitmap.recycle()
            profileBitmap?.recycle()
            logoBitmap?.recycle()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun loadImageFromUrl(context: Context, url: String): Bitmap? {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? BitmapDrawable)?.bitmap
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun loadLogoFromResources(context: Context): Bitmap? {
        return try {
            val drawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher_foreground)
            val bitmap = createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun drawPostImage(
        canvas: Canvas,
        postBitmap: Bitmap,
        destWidth: Int,
        destHeight: Int
    ) {
        val srcWidth = postBitmap.width
        val srcHeight = postBitmap.height

        val srcAspect = srcWidth.toFloat() / srcHeight
        val destAspect = destWidth.toFloat() / destHeight

        val srcRect: Rect = if (srcAspect > destAspect) {
            // Crop horizontally
            val newWidth = (srcHeight * destAspect).toInt()
            val xOffset = (srcWidth - newWidth) / 2
            Rect(xOffset, 0, xOffset + newWidth, srcHeight)
        } else {
            // Crop vertically
            val newHeight = (srcWidth / destAspect).toInt()
            val yOffset = (srcHeight - newHeight) / 2
            Rect(0, yOffset, srcWidth, yOffset + newHeight)
        }

        val destRect = Rect(0, 0, destWidth, destHeight)
        canvas.drawBitmap(postBitmap, srcRect, destRect, null)
    }


    private fun drawInfoCard(
        canvas: Canvas,
        profileBitmap: Bitmap?,
        userName: String,
        userDesignation: String?,
        userCity: String?,
        userState: String?,
        cardWidth: Int,
        cardHeight: Int
    ) {
        val cardPadding = 40f
        val cardBottomMargin = 40f
        val cardCornerRadius = 24f

        // Responsive card height based on content
        val hasDesignation = !userDesignation.isNullOrBlank()
        val hasLocation = !userCity.isNullOrBlank() || !userState.isNullOrBlank()
        val infoCardHeight = when {
            cardHeight > 1000 -> if (hasDesignation && hasLocation) 180f else 160f
            else -> if (hasDesignation && hasLocation) 150f else 130f
        }

        val infoCardTop = cardHeight - infoCardHeight - cardBottomMargin

        // Draw shadow for depth
        val shadowPaint = Paint().apply {
            color = 0x40000000 // 25% black
            style = Paint.Style.FILL
            isAntiAlias = true
            maskFilter = android.graphics.BlurMaskFilter(16f, android.graphics.BlurMaskFilter.Blur.NORMAL)
        }

        val shadowRect = RectF(
            cardPadding + 4f,
            infoCardTop + 4f,
            cardWidth - cardPadding + 4f,
            cardHeight - cardBottomMargin + 4f
        )
        canvas.drawRoundRect(shadowRect, cardCornerRadius, cardCornerRadius, shadowPaint)

        // Draw white card background with rounded corners
        val cardPaint = Paint().apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val cardRect = RectF(
            cardPadding,
            infoCardTop,
            cardWidth - cardPadding,
            cardHeight - cardBottomMargin
        )
        canvas.drawRoundRect(cardRect, cardCornerRadius, cardCornerRadius, cardPaint)

        // Profile photo configuration
        val profileSize = if (cardHeight > 1000) 110f else 90f
        val profileLeft = cardPadding + 30f
        val profileTop = infoCardTop + (infoCardHeight - profileSize) / 2

        if (profileBitmap != null) {
            // Draw circular profile photo with proper center-crop
            val profilePaint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
                isDither = true
            }

            // Add subtle border/ring around profile
            val borderPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            canvas.save()

            // Create circular clip path
            val centerX = profileLeft + profileSize / 2
            val centerY = profileTop + profileSize / 2
            val radius = profileSize / 2

            val path = android.graphics.Path()
            path.addCircle(centerX, centerY, radius, android.graphics.Path.Direction.CW)
            canvas.clipPath(path)

            // Calculate center-crop dimensions for profile image
            val srcBitmap = profileBitmap
            val srcWidth = srcBitmap.width.toFloat()
            val srcHeight = srcBitmap.height.toFloat()
            val srcAspect = srcWidth / srcHeight

            val srcRect: Rect = if (srcAspect > 1f) {
                // Landscape: crop sides
                val newWidth = (srcHeight).toInt()
                val xOffset = ((srcWidth - newWidth) / 2).toInt()
                Rect(xOffset, 0, xOffset + newWidth, srcHeight.toInt())
            } else {
                // Portrait: crop top/bottom
                val newHeight = (srcWidth).toInt()
                val yOffset = ((srcHeight - newHeight) / 2).toInt()
                Rect(0, yOffset, srcWidth.toInt(), yOffset + newHeight)
            }

            val profileDestRect = RectF(
                profileLeft,
                profileTop,
                profileLeft + profileSize,
                profileTop + profileSize
            )

            canvas.drawBitmap(srcBitmap, srcRect, profileDestRect, profilePaint)
            canvas.restore()

            // Draw border ring
            canvas.drawCircle(centerX, centerY, radius, borderPaint)

        } else {
            // Draw placeholder circle with icon
            val placeholderPaint = Paint().apply {
                color = 0xFFF5F5F5.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val borderPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
                isAntiAlias = true
            }

            val centerX = profileLeft + profileSize / 2
            val centerY = profileTop + profileSize / 2
            val radius = profileSize / 2

            canvas.drawCircle(centerX, centerY, radius, placeholderPaint)
            canvas.drawCircle(centerX, centerY, radius, borderPaint)

            // Draw user icon placeholder
            val iconPaint = Paint().apply {
                color = 0xFFBDBDBD.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            // Simple user icon - head
            canvas.drawCircle(centerX, centerY - radius * 0.15f, radius * 0.3f, iconPaint)
            // Simple user icon - body
            val bodyPath = android.graphics.Path().apply {
                addCircle(centerX, centerY + radius * 0.5f, radius * 0.45f, android.graphics.Path.Direction.CW)
            }
            canvas.drawPath(bodyPath, iconPaint)
        }

        // Text configuration
        val textLeft = profileLeft + profileSize + 24f
        val availableTextWidth = cardWidth - textLeft - cardPadding - 200f // Reserve space for logo

        // Calculate vertical centering for text
        val lineHeight = 42f
        val totalLines = 1 + (if (hasDesignation) 1 else 0) + (if (hasLocation) 1 else 0)
        val totalTextHeight = totalLines * lineHeight
        val textStartY = infoCardTop + (infoCardHeight - totalTextHeight) / 2 + 36f

        // User name - Bold and prominent
        val namePaint = Paint().apply {
            color = 0xFF212121.toInt()
            textSize = if (cardHeight > 1000) 42f else 36f
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
        }

        // Truncate name if too long
        val truncatedName = truncateText(userName, namePaint, availableTextWidth)
        canvas.drawText(truncatedName, textLeft, textStartY, namePaint)

        var currentY = textStartY

        // User designation - Medium weight
        if (hasDesignation) {
            currentY += lineHeight
            val designationPaint = Paint().apply {
                color = 0xFF616161.toInt()
                textSize = if (cardHeight > 1000) 32f else 28f
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.NORMAL
                )
                isAntiAlias = true
            }
            val truncatedDesignation = truncateText(userDesignation!!, designationPaint, availableTextWidth)
            canvas.drawText(truncatedDesignation, textLeft, currentY, designationPaint)
        }

        // User location - Light weight with icon-like indicator
        if (hasLocation) {
            currentY += lineHeight
            val location = buildString {
                if (!userCity.isNullOrBlank()) append(userCity)
                if (!userState.isNullOrBlank()) {
                    if (isNotEmpty()) append(", ")
                    append(userState)
                }
            }

            val locationPaint = Paint().apply {
                color = 0xFF757575.toInt()
                textSize = if (cardHeight > 1000) 28f else 24f
                isAntiAlias = true
            }

            // Draw location pin icon (simple marker shape)
            val pinSize = 16f
            val pinX = textLeft
            val pinY = currentY - pinSize

            val pinPaint = Paint().apply {
                color = 0xFF757575.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val pinPath = android.graphics.Path().apply {
                // Create location pin shape
                moveTo(pinX + pinSize/2, pinY)
                lineTo(pinX + pinSize, pinY + pinSize * 0.7f)
                lineTo(pinX + pinSize/2, pinY + pinSize)
                lineTo(pinX, pinY + pinSize * 0.7f)
                close()
                addCircle(pinX + pinSize/2, pinY + pinSize * 0.35f, pinSize * 0.25f, android.graphics.Path.Direction.CW)
            }
            canvas.drawPath(pinPath, pinPaint)

            val truncatedLocation = truncateText(location, locationPaint, availableTextWidth - pinSize - 8f)
            canvas.drawText(truncatedLocation, textLeft + pinSize + 8f, currentY, locationPaint)
        }
    }

    // Helper function to truncate text with ellipsis
    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        val ellipsis = "..."
        val ellipsisWidth = paint.measureText(ellipsis)

        if (paint.measureText(text) <= maxWidth) {
            return text
        }

        var truncated = text
        while (paint.measureText(truncated + ellipsis) > maxWidth && truncated.isNotEmpty()) {
            truncated = truncated.dropLast(1)
        }

        return truncated + ellipsis
    }

    private fun drawAppLogo(
        canvas: Canvas,
        logoBitmap: Bitmap?,
        cardWidth: Int,
        cardHeight: Int
    ) {
        if (logoBitmap == null) return

        val logoWidth = 180f
        val logoHeight = (logoBitmap.height * (logoWidth / logoBitmap.width))
        val logoRight = cardWidth - 50f
        val logoBottom = cardHeight - 40f

        val logoSrcRect = Rect(0, 0, logoBitmap.width, logoBitmap.height)
        val logoDestRect = RectF(
            logoRight - logoWidth,
            logoBottom - logoHeight,
            logoRight,
            logoBottom
        )

        canvas.drawBitmap(logoBitmap, logoSrcRect, logoDestRect, null)
    }
}
