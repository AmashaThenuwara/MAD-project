package com.example.agriscout.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.FarmEntity
import java.io.File
import java.io.FileOutputStream

object PdfGenerator {

    fun createFarmReport(context: Context, farm: FarmEntity, crops: List<CropEntity>): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = Color.BLACK
        }
        val subTitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 16f
            color = Color.BLACK
        }
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.BLACK
        }

        var yPosition = 50f
        val margin = 50f
        val lineHeight = 30f

        // 1. Farm Details
        canvas.drawText("Farm Report", margin, yPosition, titlePaint)
        yPosition += 50f

        canvas.drawText("Farm Name: ${farm.farmName}", margin, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("Farmer: ${farm.farmerName}", margin, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("Location: ${farm.locationName}", margin, yPosition, textPaint)
        yPosition += lineHeight
        canvas.drawText("Land Size: ${farm.landSize} Hectares", margin, yPosition, textPaint)
        yPosition += lineHeight

        yPosition += 30f

        // 2. Crops
        canvas.drawText("Crops List:", margin, yPosition, titlePaint)
        yPosition += 40f

        crops.forEach { crop ->
            if (yPosition > 750) { 
                return@forEach 
            }

            canvas.drawText("- ${crop.commonName} (${crop.scientificName})", margin + 20f, yPosition, subTitlePaint)
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
        cropName: String,
        diseaseName: String,
        notes: String
    ): String {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        var y = 50f
        canvas.drawText("Disease Report", 50f, y, paint.apply { textSize = 24f; typeface = Typeface.DEFAULT_BOLD })
        y += 60f

        canvas.drawText("Farm: $farmName", 50f, y, paint.apply { textSize = 16f })
        y += 40f
        canvas.drawText("Crop: $cropName", 50f, y, paint)
        y += 40f
        canvas.drawText("Disease: $diseaseName", 50f, y, paint.apply { color = Color.RED })
        y += 40f
        canvas.drawText("Notes: $notes", 50f, y, paint.apply { color = Color.BLACK })

        pdfDocument.finishPage(page)

        val fileName = "Disease_${diseaseName}_${System.currentTimeMillis()}.pdf"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        FileOutputStream(filePath).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        return filePath.absolutePath
    }
}