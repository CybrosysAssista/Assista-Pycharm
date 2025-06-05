package actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import dialogs.OdooModelFileDialog
import utils.ErrorDialogUtil
import icons.OdooIcons

class OdooModelFileAction : AnAction("Odoo Model File", "Create a new Odoo model file", OdooIcons.ODOO_ICON) {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val ideView = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val psiDirectory = ideView.orChooseDirectory ?: return

        val dialog = OdooModelFileDialog(project)
        if (dialog.showAndGet()) {
            val selectedFileType = dialog.getSelectedFileType()
            val fileName = dialog.getFileName()

            try {
                when (selectedFileType) {
                    OdooModelFileDialog.FileType.INIT -> {
                        createInitFile(psiDirectory)
                    }
                    OdooModelFileDialog.FileType.MANIFEST -> {
                        createManifestFile(psiDirectory)
                    }
                    OdooModelFileDialog.FileType.MODEL -> {
                        createModelFile(psiDirectory, fileName)
                    }
                    OdooModelFileDialog.FileType.CONTROLLER -> {
                        createControllerFile(psiDirectory, fileName)
                    }
                }
            } catch (ex: Exception) {
                Messages.showErrorDialog(project, "Error creating file: ${ex.message}", "Error")
            }
        }
    }

    private fun createInitFile(directory: PsiDirectory) {
        val content = """
            # -*- coding: utf-8 -*-
            from . import models
            from . import controllers
        """.trimIndent()
        createOrUpdateFile(directory, "__init__.py", content)
    }

    private fun createManifestFile(directory: PsiDirectory) {
        val content = """
            # -*- coding: utf-8 -*-
            {
                'name': 'Module Name',
                'version': '1.0',
                'category': 'Uncategorized',
                'summary': 'Module Summary',
                'description': '''Module Description''',
                'author': 'Your Company',
                'website': 'https://www.yourcompany.com',
                'depends': ['base'],
                'data': [
                    'security/ir.model.access.csv',
                    'views/views.xml',
                ],
                'installable': True,
                'application': False,
                'auto_install': False,
            }
        """.trimIndent()
        createOrUpdateFile(directory, "__manifest__.py", content)
    }

    private fun createModelFile(directory: PsiDirectory, fileName: String) {
        val baseName = fileName.removeSuffix(".py")
        val modelSnake = convertToSnakeCase(baseName)
        val modelClass = convertToCamelCase(baseName)
        val modelName = modelSnake.replace("_", ".")

        val content = """
            # -*- coding: utf-8 -*-
            from odoo import api, fields, models
            
            
            class $modelClass(models.Model):
                ""${'"'} This model represents $modelName.""${'"'}
                _name = '${modelSnake.replace("_", ".")}'
                _description = '$modelClass'

                name = fields.Char(string='Name', required=True)
                active = fields.Boolean(default=True)

                @api.model_create_multi
                def create(self, vals):
                    ""${'"'}Override the default create method to customize record creation logic.""${'"'}
                    return super($modelClass, self).create(vals)
        """.trimIndent()

        createOrUpdateFile(directory, "$baseName.py", content)
    }

    private fun createControllerFile(directory: PsiDirectory, fileName: String) {
        val baseName = fileName.removeSuffix(".py")
        val snakeName = convertToSnakeCase(baseName)
        val className = "${convertToCamelCase(baseName)}Controller"

        val content = """
            # -*- coding: utf-8 -*-
            from odoo import http
            from odoo.http import request
    
    
            class $className(http.Controller):
                ""${'"'}Controller class to handle HTTP routes.""${'"'}
                @http.route('/$snakeName', auth='public', website=True)
                def index(self, **kw):
                    return request.render('your_module.template_id', {'sample_data': 'Sample Data'})
        """.trimIndent()

        createOrUpdateFile(directory, "${baseName}.py", content)
    }

    private fun createOrUpdateFile(directory: PsiDirectory, fileName: String, content: String) {
        val project = directory.project
        val fileType = com.intellij.openapi.fileTypes.FileTypeManager.getInstance()
            .getFileTypeByFileName(fileName)

        val psiFile = PsiFileFactory.getInstance(project)
            .createFileFromText(fileName, fileType, content)

        com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
            val existing = directory.findFile(fileName)
            if (existing != null) {
                ErrorDialogUtil.showFileExistsError(project, fileName)
                return@runWriteAction
            }

            val addedFile = directory.add(psiFile)

            val virtualFile = (addedFile as? com.intellij.psi.PsiFile)?.virtualFile
            if (virtualFile != null) {
                com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                    .openFile(virtualFile, true)
            }
        }
    }


    private fun convertToSnakeCase(name: String): String {
        return name.foldIndexed("") { i, acc, c ->
            acc + if (i != 0 && c.isUpperCase()) "_${c.lowercase()}" else c.lowercase()
        }.replace(" ", "_")
    }

    private fun convertToCamelCase(name: String): String {
        return name.split("[_\\s]".toRegex())
            .joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }
}
