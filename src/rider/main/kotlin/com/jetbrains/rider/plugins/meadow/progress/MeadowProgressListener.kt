package com.jetbrains.rider.plugins.meadow.progress

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.jetbrains.rd.platform.util.lifetime
import com.jetbrains.rd.util.reactive.adviseNotNull
import com.jetbrains.rider.plugins.meadow.model.meadowPluginModel
import com.jetbrains.rider.projectView.solution

/**
 * Listens to progressUpdate signals from backend and displays notifications.
 */
@Service(Service.Level.PROJECT)
class MeadowProgressListener(private val project: Project) {
    private val log = logger<MeadowProgressListener>()
    private val notificationGroup = NotificationGroupManager.getInstance()
        .getNotificationGroup("Meadow Deployment")
    
    init {
        log.info("[MEADOW] MeadowProgressListener initialized for project: ${project.name}")
        
        // Subscribe on UI thread to avoid threading issues with RD protocol
        invokeLater {
            project.solution.meadowPluginModel.progressUpdate.adviseNotNull(project.lifetime) { update ->
                log.info("[MEADOW] Progress update received: ${update.fileName} - ${update.percentage}% - ${update.status}")
                
                // Display progress notification
                val message = "${update.fileName}: ${update.percentage}% - ${update.status}"
                notificationGroup
                    .createNotification("Meadow Deployment", message, NotificationType.INFORMATION)
                    .notify(project)
            }
        }
    }
}
