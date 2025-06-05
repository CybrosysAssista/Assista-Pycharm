package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import dialogs.ModuleNameDialog
import icons.PluginIcons
import utils.ModuleCreator

class CreateAdvancedModuleAction : AnAction("Advanced Module Template", "Create a advanced Odoo module structure", PluginIcons.ADVANCE_MODULE) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedDir = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!selectedDir.isDirectory) return

        val dialog = ModuleNameDialog(project)
        if (dialog.showAndGet()) {
            val moduleName = dialog.getModuleName()
            if (moduleName.isNotEmpty()) {
                createAdvancedModule(project, selectedDir, moduleName)
            }
        }
    }

    private fun createAdvancedModule(project: Project, parentDir: VirtualFile, moduleName: String) {
        ModuleCreator.createModuleTemplate(project, parentDir, moduleName,"OdooAdvanceModule")
    }
}
