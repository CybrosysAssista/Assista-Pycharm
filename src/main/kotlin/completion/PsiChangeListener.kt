package completion

import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.BulkFileListener

class PsiChangeListener(private val service: OdooModelProjectService) : BulkFileListener {
    override fun after(events: MutableList<out VFileEvent>) {
        service.invalidateCache()
    }
}