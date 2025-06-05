package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFileFactory
import icons.OdooIcons
import templates.OdooAccessTemplate
import utils.ErrorDialogUtil

class OdooAccessFileAction : AnAction("Odoo Access File", "Create a new Odoo access CSV file", OdooIcons.ODOO_ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val ideView = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val psiDirectory = ideView.orChooseDirectory ?: return

        val fileName = Messages.showInputDialog(
            project,
            "Enter CSV file name (e.g., ir.model.access.csv):",
            "New Odoo Access File",
            OdooIcons.ODOO_ICON
        ) ?: return

        if (fileName.isNotBlank()) {
            val accessFileName = if (fileName.endsWith(".csv")) fileName else "$fileName.csv"

            val existingFile = psiDirectory.findFile(accessFileName)
            if (existingFile != null) {
                val fullPath = existingFile.virtualFile.path
                ErrorDialogUtil.showFileExistsError(project, fullPath)
                return
            }

            val content = OdooAccessTemplate.createAccessCSV(fileName)

            ApplicationManager.getApplication().runWriteAction {
                val psiFile = PsiFileFactory.getInstance(project)
                    .createFileFromText(accessFileName, PlainTextFileType.INSTANCE, content)
                val createdFile = psiDirectory.add(psiFile)

                if (createdFile != null) {
                    val virtualFile = createdFile.containingFile.virtualFile
                    if (virtualFile != null) {
                        FileEditorManager.getInstance(project).openFile(virtualFile, true)
                    }
                }
            }
        }
    }
}