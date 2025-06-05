package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dialogs.ModuleNameDialog
import icons.PluginIcons
import utils.ModuleCreator

class CreateWebsiteThemeModuleAction : AnAction("Website Theme Module Template", "Create a Website Theme module structure", PluginIcons.THEME_MODULE) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedDir = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        if (!selectedDir.isDirectory) return

        val dialog = ModuleNameDialog(project)
        if (dialog.showAndGet()) {
            val moduleName = dialog.getModuleName()
            if (moduleName.isNotEmpty()) {
                createThemeModule(project, selectedDir, moduleName)
            }
        }
    }

    private fun createThemeModule(project: Project, parentDir: VirtualFile, moduleName: String) {
        ModuleCreator.createModuleTemplate(project, parentDir, moduleName,"WebsiteThemeModule")
    }
}
