package completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.jetbrains.python.PythonLanguage
import javax.swing.Icon
import icons.SuggestionIcons

class OdooFieldCompletionContributor : CompletionContributor() {
    val categoryIcon: Map<String, Icon> = mapOf(
        "odoo_py_field"    to SuggestionIcons.ODOO_PY_FIELD,
        "odoo_py_api_decorators"    to SuggestionIcons.ODOO_PY_API_DECORATORS,
        "odoo_py_import"    to SuggestionIcons.ODOO_PY_IMPORT,
        "odoo_py_exception" to SuggestionIcons.ODOO_PY_EXCEPTIONS,
        "odoo_py_class" to SuggestionIcons.ODOO_PY_CLASS,
        "odoo_py_function" to   SuggestionIcons.ODOO_PY_FUNCTION,
        "odoo_py_notification" to SuggestionIcons.ODOO_PY_NOTIFICATION
    )
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(PythonLanguage.getInstance()),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    resultSet: CompletionResultSet
                ) {
                    addFieldCompletions(resultSet)

                    addApiDecoratorCompletions(resultSet)

                    addImportCompletions(resultSet)

                    addExceptionCompletions(resultSet)

                    addClassTemplateCompletions(resultSet)
                }

                private fun addFieldCompletions(resultSet: CompletionResultSet) {
                    fun addField(
                        keyword: String,
                        label: String,
                        description: String,
                        codeSnippet: String,
                        category: String? = null
                    ) {
                        var builder = LookupElementBuilder.create(keyword)
                            .withPresentableText(label)
                            .withTypeText(description)
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, codeSnippet)
                                val newOffset = ctx.startOffset + codeSnippet.length
                                ctx.document.insertString(newOffset, "\n")
                                ctx.editor.caretModel.moveToOffset(newOffset + 1)
                            }

                        if (category in categoryIcon) {
                            builder = builder.withIcon(categoryIcon[category])
                        }

                        resultSet.addElement(builder)
                    }

                    addField(
                        "odoo field boolean",
                        "Boolean Field",
                        "Insert Boolean field",
                        "fields.Boolean(string='Active')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_char",
                        "Char Field",
                        "Insert Char field",
                        "fields.Char(string='Name')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_text",
                        "Text Field",
                        "Insert Text field",
                        "fields.Text(string='Description')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_float",
                        "Float Field",
                        "Insert Float field",
                        "fields.Float(string='Amount', digits=(16, 2))",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_integer",
                        "Integer Field",
                        "Insert Integer field",
                        "fields.Integer(string='Quantity')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_selection",
                        "Selection Field",
                        "Insert Selection field",
                        """
                                    |fields.Selection([
                                    |           ('draft', 'Draft'),
                                    |           ('done', 'Done')
                                    |      ], string='Status', default='draft')
                                    """.trimMargin(),
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_date",
                        "Date Field",
                        "Insert Date field",
                        "fields.Date(string='Start Date')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_datetime",
                        "Datetime Field",
                        "Insert Datetime field",
                        "fields.Datetime(string='Timestamp')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_many2one",
                        "Many2one Field",
                        "Insert Many2one field",
                        "fields.Many2one('model.name', string='Partner', ondelete='restrict')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_one2many",
                        "One2many Field",
                        "Insert One2many field",
                        "fields.One2many('model.name', 'inverse_field_name', string='Order Lines')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_many2many",
                        "Many2many Field",
                        "Insert Many2many field",
                        "fields.Many2many('model.name', string='Tags')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_html",
                        "HTML Field",
                        "Insert Html field",
                        "fields.Html(sanitize=True, string='Content')",
                        "odoo_py_field"
                    )

                    addField(
                        "odoo_field_binary",
                        "Binary Field",
                        "Insert Binary field",
                        "fields.Binary(attachment=True, string='File')",
                        "odoo_py_field"
                    )
                }

                private fun addApiDecoratorCompletions(resultSet: CompletionResultSet) {
                    val category = "odoo_py_api_decorators"
                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_api_create_multi")
                            .withPresentableText("Odoo api create multi")
                            .withTypeText("Add @api.model_create_multi decorator")
                            .withIcon(categoryIcon[category])
                            .withInsertHandler { ctx, _ ->
                                val text = "@api.model_create_multi"
                                ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                                ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                            }

                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_api_depends")
                            .withPresentableText("Odoo api depends")
                            .withTypeText("Add @api.depends decorator")
                            .withIcon(categoryIcon[category])
                            .withInsertHandler { ctx, _ ->
                                val text = "@api.depends('field_name')"
                                ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                                ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_api_onchange")
                            .withPresentableText("Odoo api onchange")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Add @api.onchange decorator")
                            .withInsertHandler { ctx, _ ->
                                val text = "@api.onchange('field_name')"
                                ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                                ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_api_constrains")
                            .withPresentableText("Odoo api constrains")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Add @api.constrains decorator")
                            .withInsertHandler { ctx, _ ->
                                val text = "@api.constrains('field_name')"
                                ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                                ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_api_returns")
                            .withPresentableText("Odoo api returns")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Add @api.returns decorator")
                            .withInsertHandler { ctx, _ ->
                                val text = "@api.returns('model.name')"
                                ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                                ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                            }
                    )
                }

                private fun addImportCompletions(resultSet: CompletionResultSet) {
                    val category = "odoo_py_import"
                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_fields")
                            .withPresentableText("Odoo import fields")
                            .withTypeText("Import Odoo fields")
                            .withIcon(categoryIcon[category])
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo import fields"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_models")
                            .withPresentableText("Odoo import models")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import Odoo models")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo import models"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_api")
                            .withPresentableText("Odoo import api")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import Odoo API")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo import api"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_tools")
                            .withPresentableText("Odoo import tools")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import Odoo tools")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo import tools"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_config")
                            .withPresentableText("Odoo import config")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import Odoo config")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo.tools import config"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_date_utils")
                            .withPresentableText("Odoo import date utils")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import Odoo date_utils")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo.tools import date_utils"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_common")
                            .withPresentableText("Odoo import common")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import common Odoo modules")
                            .withInsertHandler { ctx, _ ->
                                ctx.document.replaceString(
                                    ctx.startOffset,
                                    ctx.selectionEndOffset,
                                    "from odoo import api, fields, models"
                                )
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_import_exceptions")
                            .withPresentableText("Odoo import exceptions")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Import Odoo exceptions")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val snippet = "from odoo.exceptions import UserError, ValidationError, AccessError, MissingError\n"

                                document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)

                                val newOffset = ctx.startOffset + snippet.length
                                editor.caretModel.moveToOffset(newOffset)
                            }
                    )
                }



                private fun addExceptionCompletions(resultSet: CompletionResultSet) {
                    val category = "odoo_py_exception"
                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_user_error")
                            .withPresentableText("Odoo user error")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Raise UserError")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val snippet = "raise UserError(_('Error message'))\n"

                                document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)

                                val newOffset = ctx.startOffset + snippet.length
                                editor.caretModel.moveToOffset(newOffset)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_validation_error")
                            .withPresentableText("Odoo validation error")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Raise ValidationError")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val snippet = "raise ValidationError(_('Validation error message'))\n"

                                document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)

                                val newOffset = ctx.startOffset + snippet.length
                                editor.caretModel.moveToOffset(newOffset)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_access_error")
                            .withPresentableText("Odoo access error")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Raise AccessError")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val snippet = "raise AccessError(_('Access error message'))\n"

                                document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)

                                val newOffset = ctx.startOffset + snippet.length
                                editor.caretModel.moveToOffset(newOffset)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_missing_error")
                            .withPresentableText("Odoo missing error")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Raise MissingError")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val snippet = "raise MissingError(_('Record not found'))\n"

                                document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)

                                val newOffset = ctx.startOffset + snippet.length
                                editor.caretModel.moveToOffset(newOffset)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_redirect_warning")
                            .withPresentableText("Odoo redirect warning")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Raise RedirectWarning")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val snippet = "raise RedirectWarning(_('Warning message'), action_id, _('Button text'))\n"

                                document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)

                                val newOffset = ctx.startOffset + snippet.length
                                editor.caretModel.moveToOffset(newOffset)
                            }
                    )

                }

                private fun addClassTemplateCompletions(resultSet: CompletionResultSet) {
                    var category = "odoo_py_class"
                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_class")
                            .withPresentableText("Odoo class")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Create New Odoo Model Class")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document
                                val file = ctx.file
                                val fileName = file.name
                                val expectedModelName = fileName.replace(".py","")
                                val modelClassName = expectedModelName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
                                val modelTechnicalName = expectedModelName.replace("_",".")
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """class ${modelClassName}(models.Model):
${indent}    _name = '${modelTechnicalName}'
${indent}    _description = 'New Model Description'
${indent}    _order = 'id desc'  # Default ordering

${indent}    name = fields.Char()"""

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_transient_class")
                            .withPresentableText("Odoo transient class")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Create New Odoo TransientModel Class")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document
                                val file = ctx.file
                                val fileName = file.name
                                val expectedModelName = fileName.replace(".py","")
                                val modelClassName = expectedModelName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
                                val modelTechnicalName = expectedModelName.replace("_",".")
                                println(fileName)
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }
                                val snippet = """class ${modelClassName}(models.TransientModel):
${indent}    _name = '${modelTechnicalName}'
${indent}    _description = 'Wizard Description'

${indent}    name = fields.Char()"""
                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_abstract_class")
                            .withPresentableText("Odoo abstract class")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Create New Odoo AbstractModel Class")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document
                                val file = ctx.file
                                val fileName = file.name
                                val expectedModelName = fileName.replace(".py","")
                                val modelClassName = expectedModelName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
                                val modelTechnicalName = expectedModelName.replace("_",".")
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }
                                val snippet = """class ${modelClassName}(models.AbstractModel):
${indent}    _name = '${modelTechnicalName}'
${indent}    _description = 'Abstract Model Description'

${indent}    name = fields.Char()"""
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_extension_inheritance")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Extension Inheritance")
                            .withTypeText("Create Odoo Extension Inheritance")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document
                                val file = ctx.file
                                val fileName = file.name
                                print(ctx)
                                val expectedModelName = fileName.replace(".py","")
                                val modelClassName = expectedModelName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
                                val modelTechnicalName = expectedModelName.replace("_",".")
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }
                                val snippet = """class ${modelClassName}(models.Model):
${indent}    _name = '${modelTechnicalName}'
${indent}    _inherit = 'model.to.inherit'

${indent}    name = fields.Char()"""
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_classical_inheritance")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Classical Inheritance")
                            .withTypeText("Create Odoo Classical Inheritance")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document
                                val file = ctx.file
                                val fileName = file.name
                                print(ctx)
                                val expectedModelName = fileName.replace(".py","")
                                val modelClassName = expectedModelName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
                                val modelTechnicalName = expectedModelName.replace("_",".")
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }
                                val snippet = """class ${modelClassName}(models.Model):
${indent}    _inherit = '${modelTechnicalName}'

${indent}    new_field = fields.Char()"""
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_delegation_inheritance")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Delegation Inheritance")
                            .withTypeText("Create Odoo Delegation Inheritance")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document
                                val file = ctx.file
                                val fileName = file.name
                                print(ctx)
                                val expectedModelName = fileName.replace(".py","")
                                val modelClassName = expectedModelName.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
                                val modelTechnicalName = expectedModelName.replace("_",".")
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }
                                val snippet = """class ${modelClassName}(models.Model):
${indent}    _name = '${modelTechnicalName}'
${indent}    _inherits = {'res.partner': 'partner_id'}
${indent}
${indent}    partner_id = fields.Many2one('res.partner', required=True, ondelete='cascade')"""
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    category = "odoo_py_function"

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_create_method")
                            .withPresentableText("Odoo Create Method")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Create standard Create methods")
                            .withInsertHandler { ctx, _ ->
                                // Get editor and document info
                                val editor = ctx.editor
                                val document = editor.document

                                // Calculate indentation using the document content
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """@api.model_create_multi
${indent}def create(self, vals):
${indent}    ""${'"'}Create a new record with the given values.""${'"'}
${indent}    return super().create(vals)"""
                                // Insert the snippet and move cursor to end
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_write_method")
                            .withPresentableText("Odoo Write Method")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Create standard Write method")
                            .withInsertHandler { ctx, _ ->
                                // Get editor and document info
                                val editor = ctx.editor
                                val document = editor.document

                                // Calculate indentation using the document content
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """def write(self, vals):
${indent}    ""${'"'}Update the record with the given values.""${'"'}
${indent}    return super().write(vals)"""
                                // Insert the snippet and move cursor to end
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_unlink_method")
                            .withPresentableText("Odoo Unlink Method")
                            .withIcon(categoryIcon[category])
                            .withTypeText("Create standard Unlink methods")
                            .withInsertHandler { ctx, _ ->
                                // Get editor and document info
                                val editor = ctx.editor
                                val document = editor.document

                                // Calculate indentation using the document content
                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """def unlink(self):
${indent}    ""${'"'}Delete the current record.""${'"'}
${indent}    return super().unlink()"""
                                // Insert the snippet and move cursor to end
                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )


                    // Computed field method
                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_computed_field")
                            .withPresentableText("Odoo computed field")
                            .withTypeText("Create computed field with method")
                            .withIcon(categoryIcon[category])
                            .withInsertHandler { ctx, _ ->
                                // Get editor and document info
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """computed_field = fields.Float(string='Computed Field', compute='_compute_computed_field', store=True)

${indent}@api.depends('dependency_field')
${indent}def _compute_computed_field(self):
${indent}    ""${'"'}Compute the value of the field computed_field.""${'"'}
${indent}    for record in self:
${indent}        record.computed_field = 0.0  # Your computation here"""

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    // onchange method
                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_onchange_method")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo onchange method")
                            .withTypeText("Create onchange method")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """@api.onchange('field_name')
${indent}def _onchange_field_name(self):
${indent}    ""${'"'}Triggered when the field_name changes to update related values.""${'"'}
${indent}    if self.field_name:
${indent}        # Your onchange logic here
${indent}        self.another_field = self.field_name"""

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    category = "odoo_py_notification"

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_return_action")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Return Action")
                            .withTypeText("Create Return Action")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """return {
${indent}    'type': 'ir.actions.act_window',
${indent}    'name': 'Window Title',
${indent}    'res_model': 'model.name',
${indent}    'view_mode': 'list,form',
${indent}    'target': 'current/new',
${indent}    'domain': [('field_name', '>', value)],
${indent}    'context': {'default_field_name': field_value},
${indent}}
                                """.trimIndent()

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_rainbow_man_notification")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Rainbow Man Notification")
                            .withTypeText("Create Rainbow Man Notification")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """return {
${indent}    'effect': {
${indent}        'fadeout': 'slow',
${indent}        'message': f'Message to Display',
${indent}        'type': 'rainbow_man',
${indent}        }
${indent}    }
                                """.trimIndent()

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_warning_notification")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Warning Notification")
                            .withTypeText("Create Warning Notification(Sticky)")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """return {
${indent}    'type': 'ir.actions.client',
${indent}    'tag': 'display_notification',
${indent}    'params': {
${indent}        'title': _("Warning head"),
${indent}        'type': 'warning',
${indent}        'message': _("This is the detailed warning"),
${indent}        'sticky': True,
${indent}    },
${indent}}
                                """.trimIndent()

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_success_notification")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Success Notification")
                            .withTypeText("Create Success Notification(Sticky)")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """return {
${indent}    'type': 'ir.actions.client',
${indent}    'tag': 'display_notification',
${indent}    'params': {
${indent}        'title': _("Warning head"),
${indent}        'type': 'success',
${indent}        'message': _("This is the detailed warning"),
${indent}        'sticky': True,
${indent}    },
${indent}}
                                """.trimIndent()

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )

                    resultSet.addElement(
                        LookupElementBuilder.create("odoo_info_notification")
                            .withIcon(categoryIcon[category])
                            .withPresentableText("Odoo Info Notification")
                            .withTypeText("Create Info Notification(Sticky)")
                            .withInsertHandler { ctx, _ ->
                                val editor = ctx.editor
                                val document = editor.document

                                val offset = ctx.startOffset
                                val line = document.getLineNumber(offset)
                                val startOffset = document.getLineStartOffset(line)
                                val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                                    .takeWhile { it.isWhitespace() }

                                val snippet = """return {
${indent}    'type': 'ir.actions.client',
${indent}    'tag': 'display_notification',
${indent}    'params': {
${indent}        'title': _("Warning head"),
${indent}        'type': 'info',
${indent}        'message': _("This is the detailed warning"),
${indent}        'sticky': True,
${indent}    },
${indent}}
                                """.trimIndent()

                                document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                                editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                            }
                    )
                }
            }
        )
    }
}