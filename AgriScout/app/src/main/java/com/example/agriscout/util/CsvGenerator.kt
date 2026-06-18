package com.example.agriscout.util

import android.content.Context
import android.os.Environment
import com.example.agriscout.data.local.entity.CropEntity
import com.example.agriscout.data.local.entity.FarmEntity
import java.io.File
import java.io.FileWriter

object CsvGenerator {

    fun createFarmCsvReport(context: Context, farm: FarmEntity, crops: List<CropEntity>): String {
        val fileName = "Farm_${farm.farmName}_${System.currentTimeMillis()}.csv"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        try {
            val writer = FileWriter(filePath)

            // Write Farm Details
            writer.append("Farm Report\n")
            writer.append("Farm Name,Farmer,Location,Land Size,Details\n")
            writer.append("${escapeCsv(farm.farmName)},${escapeCsv(farm.farmerName)},${escapeCsv(farm.locationName)},${farm.landSize},${escapeCsv(farm.locationDetails)}\n")
            writer.append("\n")

            // Write Crop Details
            writer.append("Crops List\n")
            writer.append("Common Name,Scientific Name,Category,Origin,Soil Type,Fertilizer Type\n")

            for (crop in crops) {
                writer.append("${escapeCsv(crop.commonName)},${escapeCsv(crop.scientificName)},${escapeCsv(crop.category)},${escapeCsv(crop.origin)},${escapeCsv(crop.soilType)},${escapeCsv(crop.fertilizerType)}\n")
            }

            writer.flush()
            writer.close()

            return filePath.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            val escaped = value.replace("\"", "\"\"")
            return "\"$escaped\""
        }
        return value
    }
}
