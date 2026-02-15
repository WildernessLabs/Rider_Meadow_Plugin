package com.jetbrains.rider.plugins.meadow.notifications

import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Listens for DAP progress events via RD signals and displays Rider notifications.
 * 
 * The DAP adapter emits progressStart/progressUpdate/progressEnd events during deployment,
 * but IntelliJ Platform doesn't auto-render them like VSCode. This service bridges the gap
 * by listening for RD signals fired from MeadowDapEventInterceptor and displaying them
 * as Rider notifications.
 */
@Service(Service.Level.PROJECT)
class MeadowDapProgressNotificationService(private val project: Project) {
    
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("Meadow Deployment")
    
    private var currentNotification: Notification? = null
    
    init {
        // Subscribe to DAP progress events from backend
        val model = project.solution.meadowPluginModel
        val serviceLifetime = project.lifetime
        
        model.showProgressNotification.advise(serviceLifetime) { json ->
            handleProgressStart(json)
        }
        
        model.updateProgressNotification.advise(serviceLifetime) { json ->
            handleProgressUpdate(json)
        }
        
        model.hideProgressNotification.advise(serviceLifetime) { json ->
            handleProgressEnd(json)
        }
    }
    
    private fun handleProgressStart(json: String) {
        try {
            val data = Json.parseToJsonElement(json).jsonObject
            val title = data["title"]?.jsonPrimitive?.content ?: "Deploying..."
            val percentage = data["percentage"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            
            // Create and show notification
            currentNotification?.expire()
            currentNotification = notificationGroup
                .createNotification(title, "$percentage% complete", NotificationType.INFORMATION)  
            currentNotification?.notify(project)
        } catch (e: Exception) {
            // Ignore JSON parse errors
        }
    }
    
    private fun handleProgressUpdate(json: String) {
        try {
            val data = Json.parseToJsonElement(json).jsonObject
            val message = data["message"]?.jsonPrimitive?.content ?: ""
            val percentage = data["percentage"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            
            // Update existing notification content
            currentNotification?.expire()
            currentNotification = notificationGroup
                .createNotification("Deploying...", "$percentage% - $message", NotificationType.INFORMATION)
            currentNotification?.notify(project)
        } catch (e: Exception) {
            // Ignore JSON parse errors
        }
    }
    
    private fun handleProgressEnd(json: String) {
        try {
            val data = Json.parseToJsonElement(json).jsonObject
            val message = data["message"]?.jsonPrimitive?.content ?: "Deployment complete"
            
            // Show final notification
            currentNotification?.expire()
            notificationGroup
                .createNotification("Deployment", message, NotificationType.INFORMATION)
                .notify(project)
            currentNotification = null
        } catch (e: Exception) {
            // Ignore JSON parse errors
        }
    }
    
    companion object {
        fun getInstance(project: Project): MeadowDapProgressNotificationService =
            project.getService(MeadowDapProgressNotificationService::class.java)
    }
}
