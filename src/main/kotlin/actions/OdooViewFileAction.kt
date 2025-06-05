package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiDirectory
import dialogs.OdooViewFileDialog
import icons.OdooIcons
import templates.OdooViewTemplate
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.vfs.VirtualFile

class OdooViewFileAction : AnAction("Odoo View File", "Create a new Odoo view file", OdooIcons.ODOO_ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val ideView = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val psiDirectory = ideView.orChooseDirectory ?: return

        val dialog = OdooViewFileDialog(project)
        if (dialog.showAndGet()) {
            val selectedViewType = dialog.getSelectedViewType()
            val fileName = dialog.getFileName()

            if (fileName.isBlank()) {
                Messages.showErrorDialog(project, "File name cannot be empty", "Error")
                return
            }

            val viewFileName = if (fileName.endsWith(".xml")) fileName else "$fileName.xml"

            val fileContent = when (selectedViewType) {
                OdooViewFileDialog.ViewType.EMPTY -> OdooViewTemplate.createEmptyViewFile()
                OdooViewFileDialog.ViewType.BASIC -> OdooViewTemplate.createBasicViewFile(fileName)
                OdooViewFileDialog.ViewType.ADVANCED -> OdooViewTemplate.createAdvancedViewFile(fileName)
                OdooViewFileDialog.ViewType.INHERIT -> OdooViewTemplate.createInheritViewFile(fileName)
                OdooViewFileDialog.ViewType.REPORT -> OdooViewTemplate.createReportViewFile(fileName)
                OdooViewFileDialog.ViewType.SECURITY_GROUP -> OdooViewTemplate.createSecurityGroupsViewFile(fileName)
                OdooViewFileDialog.ViewType.SECURITY_RULE -> OdooViewTemplate.createSecurityRuleViewFile(fileName)
                OdooViewFileDialog.ViewType.SEQUENCE -> OdooViewTemplate.createSequenceViewFile(fileName)
                OdooViewFileDialog.ViewType.SETTINGS -> OdooViewTemplate.createSettingsViewFile(fileName)
                OdooViewFileDialog.ViewType.CRON_JOB -> OdooViewTemplate.createCronJobViewFile(fileName)
            }

            try {
                var virtualFileCreated: VirtualFile? = null

                ApplicationManager.getApplication().runWriteAction {
                    val psiFile = PsiFileFactory.getInstance(project)
                        .createFileFromText(
                            viewFileName,
                            FileTypeManager.getInstance().getFileTypeByExtension("xml"),
                            fileContent
                        )
                    psiDirectory.add(psiFile)
                    virtualFileCreated = psiDirectory.virtualFile.findChild(viewFileName)
                }

                virtualFileCreated?.let { virtualFile ->
                    PsiNavigationSupport.getInstance()
                        .createNavigatable(project, virtualFile, 0)
                        .navigate(true)
                } ?: Messages.showErrorDialog(project, "Could not find created file to open.", "Error")

            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Error creating file: ${ex.message}", "Error")
            }
        }
    }
}
