package com.example.agriscout.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.DiseaseReportEntity
import com.example.agriscout.data.local.entity.FarmEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    fun exportToCsv(
        context: Context,
        reports: List<DiseaseReportEntity>,
        farms: List<FarmEntity>,
        crops: List<CropEntity>
    ): Uri? {
        val fileName = "AgriScout_Reports_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        val farmMap = farms.associateBy { it.farmId }
        val cropMap = crops.associateBy { it.cropId }

        try {
            val writer = file.printWriter()
            // Header
            writer.println("Report ID,Farm Name,Farmer,Crop,Disease Name,Date,Latitude,Longitude,Notes")

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            reports.forEach { report ->
                val farm = farmMap[report.farmId]
                val farmName = farm?.farmName ?: "Unknown"
                val farmer = farm?.farmerName ?: "Unknown"
                
                val crop = cropMap[report.cropId]
                val cropName = crop?.commonName ?: "Unknown"
                
                val date = dateFormat.format(Date(report.date))
                val notes = report.notes.replace("\n", " ").replace(",", ";")
                writer.println("${report.reportId},$farmName,$farmer,$cropName,${report.diseaseName},$date,${report.latitude},${report.longitude},\"$notes\"")
            }
            writer.close()
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportToPdf(
        context: Context,
        reports: List<DiseaseReportEntity>,
        farms: List<FarmEntity>,
        crops: List<CropEntity>
    ): Uri? {
        val fileName = "AgriScout_Reports_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        val farmMap = farms.associateBy { it.farmId }
        val cropMap = crops.associateBy { it.cropId }

        val pdfDocument = PdfDocument()
        val titlePaint = Paint().apply {
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subTitlePaint = Paint().apply {
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val textPaint = Paint().apply {
            textSize = 12f
        }
        val labelPaint = Paint().apply {
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        // Create a page
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        var y = 50f

        canvas.drawText("AgriScout - Detailed Disease Report", 40f, y, titlePaint)
        y += 40f

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        reports.forEachIndexed { index, report ->
            val farm = farmMap[report.farmId]
            val crop = cropMap[report.cropId]
            
            // Check for enough space (increased requirement for image)
            if (y > 650) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 50f
            }

            val entryStartY = y

            // Disease Heading
            canvas.drawText("${index + 1}. Disease: ${report.diseaseName}", 40f, y, subTitlePaint)
            y += 20f

            // Farm Details Section
            canvas.drawText("Farm Details:", 60f, y, labelPaint)
            y += 18f
            canvas.drawText("• Name: ${farm?.farmName ?: "N/A"}", 70f, y, textPaint)
            y += 18f
            canvas.drawText("• Farmer: ${farm?.farmerName ?: "N/A"}", 70f, y, textPaint)
            y += 18f
            canvas.drawText("• Crop: ${crop?.commonName ?: "N/A"}", 70f, y, textPaint)
            y += 22f

            // Report Details Section
            canvas.drawText("Report Details:", 60f, y, labelPaint)
            y += 18f
            canvas.drawText("• Date: ${dateFormat.format(Date(report.date))}", 70f, y, textPaint)
            y += 18f
            canvas.drawText("• GPS: ${report.latitude}, ${report.longitude}", 70f, y, textPaint)
            y += 18f
            
            // Handle long notes with a simple wrap
            val notes = "• Notes: ${report.notes}"
            if (notes.length > 60) {
                canvas.drawText(notes.substring(0, 60), 70f, y, textPaint)
                y += 15f
                canvas.drawText(notes.substring(60).take(60), 70f, y, textPaint)
            } else {
                canvas.drawText(notes, 70f, y, textPaint)
            }
            
            // Draw Disease Image if it exists
            if (report.imagePath.isNotEmpty()) {
                val imgFile = File(report.imagePath)
                if (imgFile.exists()) {
                    try {
                        val options = BitmapFactory.Options().apply {
                            inSampleSize = 4 // Scale down during decoding to save memory
                        }
                        val bitmap = BitmapFactory.decodeFile(report.imagePath, options)
                        if (bitmap != null) {
                            // Scale to a fixed size (e.g., 140x140)
                            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 140, 140, true)
                            // Draw image to the right of the text
                            canvas.drawBitmap(scaledBitmap, 410f, entryStartY + 10f, null)
                            bitmap.recycle() // Free memory
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            
            y += 45f // Extra space before next entry
            
            // Draw a separator line
            canvas.drawLine(40f, y - 10f, 555f, y - 10f, Paint().apply { color = Color.LTGRAY })
            y += 20f
        }

        pdfDocument.finishPage(page)

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            return null
        }
    }
}
