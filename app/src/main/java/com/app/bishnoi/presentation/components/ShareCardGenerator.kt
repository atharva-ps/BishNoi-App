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
        userProfileUrl: String?,
        userName: String,
        userDesignation: String?,
        userCity: String?,
        userState: String?
    ): File? = withContext(Dispatchers.IO) {
        try {
            // Card dimensions (Instagram post size)
            val cardWidth = 1080
            val cardHeight = 1080

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

    private fun drawPostImage(canvas: Canvas, postBitmap: Bitmap, width: Int, height: Int) {
        // Scale and center crop the post image
        val srcRect = Rect(0, 0, postBitmap.width, postBitmap.height)
        val destRect = Rect(0, 0, width, height)

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
        val cardPadding = 32f
        val cardBottomMargin = 24f
        val cardCornerRadius = 32f

        // Card dimensions
        val infoCardHeight = 200f
        val infoCardTop = cardHeight - infoCardHeight - cardBottomMargin

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

        // Draw profile photo
        val profileSize = 120f
        val profileLeft = cardPadding + 40f
        val profileTop = infoCardTop + (infoCardHeight - profileSize) / 2

        if (profileBitmap != null) {
            // Draw circular profile photo
            val profilePaint = Paint().apply {
                isAntiAlias = true
            }

            val circleRect = RectF(
                profileLeft,
                profileTop,
                profileLeft + profileSize,
                profileTop + profileSize
            )

            canvas.save()
            canvas.clipRect(circleRect)

            // Create circular clip path
            val path = android.graphics.Path()
            path.addCircle(
                profileLeft + profileSize / 2,
                profileTop + profileSize / 2,
                profileSize / 2,
                android.graphics.Path.Direction.CW
            )
            canvas.clipPath(path)

            val profileSrcRect = Rect(0, 0, profileBitmap.width, profileBitmap.height)
            val profileDestRect = RectF(
                profileLeft,
                profileTop,
                profileLeft + profileSize,
                profileTop + profileSize
            )
            canvas.drawBitmap(profileBitmap, profileSrcRect, profileDestRect, profilePaint)
            canvas.restore()
        } else {
            // Draw placeholder circle
            val placeholderPaint = Paint().apply {
                color = 0xFFE0E0E0.toInt()
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            canvas.drawCircle(
                profileLeft + profileSize / 2,
                profileTop + profileSize / 2,
                profileSize / 2,
                placeholderPaint
            )
        }

        // Draw text info
        val textLeft = profileLeft + profileSize + 32f
        val textTop = infoCardTop + 50f

        // User name
        val namePaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 44f
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
            isAntiAlias = true
        }
        canvas.drawText(userName, textLeft, textTop, namePaint)

        // User designation
        if (!userDesignation.isNullOrBlank()) {
            val designationPaint = Paint().apply {
                color = 0xFF616161.toInt()
                textSize = 32f
                isAntiAlias = true
            }
            canvas.drawText(userDesignation, textLeft, textTop + 50f, designationPaint)
        }

        // User location
        val location = buildString {
            if (!userCity.isNullOrBlank()) append(userCity)
            if (!userState.isNullOrBlank()) {
                if (isNotEmpty()) append(", ")
                append(userState)
            }
        }

        if (location.isNotBlank()) {
            val locationPaint = Paint().apply {
                color = 0xFF757575.toInt()
                textSize = 28f
                isAntiAlias = true
            }
            canvas.drawText(location, textLeft, textTop + 95f, locationPaint)
        }
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
