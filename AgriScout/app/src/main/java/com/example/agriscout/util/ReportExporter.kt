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
        val textPaint = Paint().apply {
            textSize = 14f
            color = Color.DKGRAY
        }

        // Create a page
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        // Draw main header background
        canvas.drawRect(0f, 0f, 595f, 100f, headerPaint)
        
        // Draw header text
        canvas.drawText("All Disease Reports", 50f, 65f, headerTextPaint)

        var y = 140f
        val margin = 50f
        val lineHeight = 30f

        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        reports.forEachIndexed { index, report ->
            val farm = farmMap[report.farmId]
            val crop = cropMap[report.cropId]
            
            // Check for enough space (increased requirement for image)
            if (y > 650) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                // New page header
                canvas.drawRect(0f, 0f, 595f, 60f, headerPaint)
                canvas.drawText("All Disease Reports (Cont.)", 50f, 40f, headerTextPaint.apply { textSize = 20f })
                y = 100f
            }

            val entryStartY = y

            // Disease Heading
            canvas.drawText("${index + 1}. ${report.diseaseName}", margin, y, sectionTitlePaint)
            canvas.drawLine(margin, y + 10f, 545f, y + 10f, headerPaint.apply { strokeWidth = 2f })
            y += 40f

            // Details
            canvas.drawText("Farm: ${farm?.farmName ?: "N/A"}", margin, y, textPaint)
            y += lineHeight
            canvas.drawText("Farmer: ${farm?.farmerName ?: "N/A"}", margin, y, textPaint)
            y += lineHeight
            canvas.drawText("Crop: ${crop?.commonName ?: "N/A"}", margin, y, textPaint)
            y += lineHeight
            canvas.drawText("Date: ${dateFormat.format(Date(report.date))}", margin, y, textPaint)
            y += lineHeight
            canvas.drawText("GPS: ${report.latitude}, ${report.longitude}", margin, y, textPaint)
            y += lineHeight
            
            val notes = "Notes: ${report.notes}"
            if (notes.length > 60) {
                canvas.drawText(notes.substring(0, 60), margin, y, textPaint)
                y += lineHeight
                canvas.drawText(notes.substring(60).take(60), margin, y, textPaint)
            } else {
                canvas.drawText(notes, margin, y, textPaint)
            }
            y += 40f // padding below entry
            
            // Draw Disease Image if it exists
            if (report.imagePath.isNotEmpty() && !report.imagePath.startsWith("http")) {
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
