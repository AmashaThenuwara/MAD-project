package com.example.agriscout.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.FarmEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

    fun createFarmReport(context: Context, farm: FarmEntity, crops: List<CropEntity>): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val headerPaint = Paint().apply {
            color = Color.parseColor("#2E7D32")
        }
        val headerTextPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
            color = Color.WHITE
        }
        val sectionTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.parseColor("#2E7D32")
        }
        val subTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
        }

        // Draw header background
        canvas.drawRect(0f, 0f, 595f, 100f, headerPaint)
        
        // Draw header text
        canvas.drawText("AgriScout Farm Report", 50f, 65f, headerTextPaint)

        var yPosition = 140f
        val margin = 50f
        val lineHeight = 30f

        // 1. Farm Details
        canvas.drawText("Farm Details", margin, yPosition, sectionTitlePaint)
        canvas.drawLine(margin, yPosition + 10f, 545f, yPosition + 10f, headerPaint.apply { strokeWidth = 2f })
        yPosition += 40f

        canvas.drawText("Farm Name: ${farm.farmName}", margin, yPosition, subTitlePaint)
        yPosition += lineHeight
        canvas.drawText("Farmer: ${farm.farmerName}", margin, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("Location: ${farm.locationName}", margin, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("Land Size: ${farm.landSize} Hectares", margin, yPosition, textPaint)
        yPosition += lineHeight

        yPosition += 40f

        // 2. Crops
        canvas.drawText("Crop Details", margin, yPosition, sectionTitlePaint)
        canvas.drawLine(margin, yPosition + 10f, 545f, yPosition + 10f, headerPaint.apply { strokeWidth = 2f })
        yPosition += 40f

        crops.forEach { crop ->
            if (yPosition > 750) { 
                return@forEach 
            }

            canvas.drawText("• ${crop.commonName} (${crop.scientificName})", margin + 10f, yPosition, subTitlePaint)
            yPosition += lineHeight
            canvas.drawText("  Category: ${crop.category}", margin + 20f, yPosition, textPaint)
            yPosition += lineHeight
            canvas.drawText("  Origin: ${crop.origin}", margin + 20f, yPosition, textPaint)
            yPosition += lineHeight
            canvas.drawText("  Soil: ${crop.soilType} | Fertilizer: ${crop.fertilizerType}", margin + 20f, yPosition, textPaint)
            yPosition += 40f
        }

        pdfDocument.finishPage(page)

        val fileName = "Farm_${farm.farmName}_${System.currentTimeMillis()}.pdf"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        FileOutputStream(filePath).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return filePath.absolutePath
    }

    fun createDiseaseReport(
        context: Context,
        farmName: String,
        farmerName: String,
        locationName: String,
        cropName: String,
        diseaseName: String,
        notes: String,
        imagePath: String = "",
        latitude: Double = 0.0,
        longitude: Double = 0.0,
        date: Long = System.currentTimeMillis()
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val headerPaint = Paint().apply {
            color = Color.parseColor("#2E7D32")
        }
        val headerTextPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 28f
            color = Color.WHITE
        }
        val sectionTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
            color = Color.parseColor("#2E7D32")
        }
        val subTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
        }

        // Draw header background
        canvas.drawRect(0f, 0f, 595f, 100f, headerPaint)
        
        // Draw header text
        canvas.drawText("AgriScout Disease Report", 50f, 65f, headerTextPaint)

        var y = 140f
        val margin = 50f
        val lineHeight = 30f

        val entryStartY = y

        // Disease Details
        canvas.drawText("Disease Details", margin, y, sectionTitlePaint)
        canvas.drawLine(margin, y + 10f, 545f, y + 10f, headerPaint.apply { strokeWidth = 2f })
        y += 40f

        canvas.drawText("Disease: $diseaseName", margin, y, subTitlePaint)
        y += lineHeight

        // Farm Details
        canvas.drawText("Farm Details", margin, y + 10f, sectionTitlePaint)
        canvas.drawLine(margin, y + 20f, 545f, y + 20f, headerPaint.apply { strokeWidth = 2f })
        y += 50f

        canvas.drawText("Name: $farmName", margin, y, textPaint)
        y += lineHeight
        canvas.drawText("Farmer: $farmerName", margin, y, textPaint)
        y += lineHeight
        canvas.drawText("Location: $locationName", margin, y, textPaint)
        y += lineHeight
        canvas.drawText("Crop: $cropName", margin, y, textPaint)
        y += 40f

        // Report Details
        canvas.drawText("Report Details", margin, y, sectionTitlePaint)
        canvas.drawLine(margin, y + 10f, 545f, y + 10f, headerPaint.apply { strokeWidth = 2f })
        y += 40f

        canvas.drawText("Date: ${dateFormat.format(Date(date))}", margin, y, textPaint)
        y += lineHeight
        canvas.drawText("GPS: $latitude, $longitude", margin, y, textPaint)
        y += lineHeight

        val notesText = "Notes: $notes"
        if (notesText.length > 60) {
            canvas.drawText(notesText.substring(0, 60), margin, y, textPaint)
            y += lineHeight
            canvas.drawText(notesText.substring(60).take(60), margin, y, textPaint)
        } else {
            canvas.drawText(notesText, margin, y, textPaint)
        }

        if (imagePath.isNotEmpty() && !imagePath.startsWith("http")) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                try {
                    val options = BitmapFactory.Options().apply { inSampleSize = 4 }
                    val bitmap = BitmapFactory.decodeFile(imagePath, options)
                    if (bitmap != null) {
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 140, 140, true)
                        canvas.drawBitmap(scaledBitmap, 400f, entryStartY + 20f, null)
                        bitmap.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        pdfDocument.finishPage(page)

        val fileName = "Disease_${diseaseName}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        return try {
            FileOutputStream(file).use { pdfDocument.writeTo(it) }
            pdfDocument.close()
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}