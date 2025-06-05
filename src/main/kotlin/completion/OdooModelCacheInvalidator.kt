package completion

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.util.messages.MessageBusConnection

class OdooModelCacheInvalidator(project: Project) {
    private val service = project.service<OdooModelProjectService>()
    private val connection: MessageBusConnection = project.messageBus.connect()

    init {
        connection.subscribe(VirtualFileManager.VFS_CHANGES, PsiChangeListener(service))
    }
}