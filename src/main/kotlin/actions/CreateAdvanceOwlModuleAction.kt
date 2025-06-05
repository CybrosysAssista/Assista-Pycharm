package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dialogs.ModuleNameDialog
import icons.PluginIcons
import utils.ModuleCreator

class CreateAdvanceOwlModuleAction : AnAction("Owl Advance Module Template", "Create an Advance Owl module structure", PluginIcons.OWL_ADVANCE_MODULE) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedDir = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!selectedDir.isDirectory) return

        val dialog = ModuleNameDialog(project)
        if (dialog.showAndGet()) {
            val moduleName = dialog.getModuleName()
            if (moduleName.isNotEmpty()) {
                createOwlAdvanceModule(project, selectedDir, moduleName)
            }
        }
    }

    private fun createOwlAdvanceModule(project: Project, parentDir: VirtualFile, moduleName: String) {
        ModuleCreator.createModuleTemplate(project, parentDir, moduleName,"OwlAdvanceModule")
    }
}