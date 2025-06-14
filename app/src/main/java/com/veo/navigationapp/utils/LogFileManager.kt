package com.veo.navigationapp.utils

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Log file manager
 * 
 * @author Haisheng Wang
 * @email haislien@163.com
 * @description Responsible for application log file storage and management, supporting log writing, file rotation and cleanup functions
 */
class LogFileManager(private val context: Context) {
    
    companion object {
        private const val TAG = "LogFileManager"
        private const val LOG_DIR_NAME = "NavigationLogs"
        private const val MAX_LOG_FILES = 10 // Keep maximum 10 log files
        private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
    }
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val logDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    
    /**
     * Get log directory path
     */
    fun getLogDirectory(): File {
        // Prefer external storage app-specific directory
        val externalDir = context.getExternalFilesDir(null)
        val logDir = if (externalDir != null) {
            File(externalDir, LOG_DIR_NAME)
        } else {
            // Use internal storage if external storage unavailable
            File(context.filesDir, LOG_DIR_NAME)
        }
        
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        
        return logDir
    }
    
    /**
     * Get current log file
     */
    private fun getCurrentLogFile(): File {
        val logDir = getLogDirectory()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return File(logDir, "navigation_log_$today.txt")
    }
    
    /**
     * Write log to file
     */
    fun writeLog(tag: String, level: String, message: String) {
        try {
            val logFile = getCurrentLogFile()
            
            // Check file size, create new file if exceeds limit
            if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
                rotateLogFile(logFile)
            }
            
            val timestamp = logDateFormat.format(Date())
            val logEntry = "$timestamp [$level] $tag: $message\n"
            
            FileWriter(logFile, true).use { writer ->
                writer.append(logEntry)
                writer.flush()
            }
            
        } catch (e: IOException) {
            Log.e(TAG, "Failed to write log file", e)
        }
    }
    
    /**
     * Rotate log file
     */
    private fun rotateLogFile(currentFile: File) {
        val timestamp = dateFormat.format(Date())
        val newFileName = currentFile.nameWithoutExtension + "_" + timestamp + ".txt"
        val newFile = File(currentFile.parent, newFileName)
        currentFile.renameTo(newFile)
        
        // Clean up old log files
        cleanOldLogFiles()
    }
    
    /**
     * Clean up old log files
     */
    private fun cleanOldLogFiles() {
        val logDir = getLogDirectory()
        val logFiles = logDir.listFiles { file ->
            file.name.startsWith("navigation_log_") && file.name.endsWith(".txt")
        }?.sortedByDescending { it.lastModified() }
        
        logFiles?.let { files ->
            if (files.size > MAX_LOG_FILES) {
                files.drop(MAX_LOG_FILES).forEach { file ->
                    file.delete()
                    Log.d(TAG, "Deleted old log file: ${file.name}")
                }
            }
        }
    }
    
    /**
     * Get all log files list
     */
    fun getLogFiles(): List<File> {
        val logDir = getLogDirectory()
        return logDir.listFiles { file ->
            file.name.startsWith("navigation_log_") && file.name.endsWith(".txt")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
    
    /**
     * Get complete path of log directory
     */
    fun getLogDirectoryPath(): String {
        return getLogDirectory().absolutePath
    }
    
    /**
     * Get complete path of current log file
     */
    fun getCurrentLogFilePath(): String {
        return getCurrentLogFile().absolutePath
    }
    
    /**
     * Check storage permissions and available space
     */
    fun checkStorageStatus(): StorageStatus {
        val logDir = getLogDirectory()
        
        return StorageStatus(
            isAvailable = logDir.exists() || logDir.mkdirs(),
            path = logDir.absolutePath,
            freeSpace = logDir.freeSpace,
            totalSpace = logDir.totalSpace
        )
    }
    
    /**
     * Storage status data class
     */
    data class StorageStatus(
        val isAvailable: Boolean,
        val path: String,
        val freeSpace: Long,
        val totalSpace: Long
    ) {
        fun getFreeSpaceMB(): Long = freeSpace / (1024 * 1024)
        fun getTotalSpaceMB(): Long = totalSpace / (1024 * 1024)
    }
}