package completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import com.intellij.psi.xml.XmlAttribute
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.xml.XmlTag
import com.intellij.openapi.components.service
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import indexing.OdooModelIndex
import indexing.OdooModelFieldIndex
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.psi.xml.XmlAttributeValue
import icons.SuggestionIcons
import javax.swing.Icon


class OdooXmlCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
//        val project: Project = parameters.position.project
//        val xmlTag: XmlTag = PsiTreeUtil.getParentOfType(parameters.position, XmlTag::class.java) ?: return
//
//        val recordTag = generateSequence(xmlTag) { it.parentTag }
//            .firstOrNull { it?.name == "record" } ?: return
//
//        val modelFieldTag = recordTag.findFirstSubTag("field")?.takeIf {
//            it.getAttributeValue("name") == "model"
//        }
//
//        val model = modelFieldTag?.value?.text ?: return
//
//        val fieldList = project.service<OdooModelProjectService>().getFieldsForModel(model)
//        for (field in fieldList) {
//            resultSet.addElement(LookupElementBuilder.create(field).withTypeText("Model Field"))-
//
//        }
        resultSet.addElement(
            LookupElementBuilder.create("odoo_btn_object")
                .withIcon(SuggestionIcons.ODOO_XML_BUTTON_ACTION)
                .withPresentableText("Odoo btn object")
                .withTypeText("Add Object Button")
                .withInsertHandler { ctx, _ ->
                    val text = "<button name=\"button_method_name\" type=\"object\" string=\"Button Label\" class=\"btn-primary\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Action Button
        resultSet.addElement(
            LookupElementBuilder.create("odoo_btn_action")
                .withIcon(SuggestionIcons.ODOO_XML_BUTTON_ACTION)
                .withPresentableText("Odoo btn action")
                .withTypeText("Add Action Button")
                .withInsertHandler { ctx, _ ->
                    val text = "<button name=\"%(action_id)d\" type=\"action\" string=\"Action Button\" class=\"btn-secondary\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Domain
        resultSet.addElement(
            LookupElementBuilder.create("odoo_domain")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo domain")
                .withTypeText("Add Domain to Field")
                .withInsertHandler { ctx, _ ->
                    val text = "domain=\"[('field_name', '=', value)]\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )
        // Options
        resultSet.addElement(
            LookupElementBuilder.create("odoo_options")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo Options")
                .withTypeText("Add Options to Field")
                .withInsertHandler { ctx, _ ->
                    val text = "options=\"{'currency_field': 'currency_id'}\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Field
        resultSet.addElement(
            LookupElementBuilder.create("odoo_field")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo field")
                .withTypeText("Add Field in Form/View")
                .withInsertHandler { ctx, _ ->
                    val text = "<field name=\"field_name\" string=\"Field Label\" required=\"True\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Groups
        resultSet.addElement(
            LookupElementBuilder.create("odoo_groups")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo groups")
                .withTypeText("Add Groups Restriction")
                .withInsertHandler { ctx, _ ->
                    val text = "groups=\"base.group_user\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Label
        resultSet.addElement(
            LookupElementBuilder.create("odoo_label")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo label")
                .withTypeText("Add Label")
                .withInsertHandler { ctx, _ ->
                    val text = "<label for=\"field_name\" string=\"Label Text\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Separator
        resultSet.addElement(
            LookupElementBuilder.create("odoo_separator")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo separator")
                .withTypeText("Add Separator")
                .withInsertHandler { ctx, _ ->
                    val text = "<separator string=\"Section Title\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, text)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + text.length)
                }
        )

        // Form View
        resultSet.addElement(
            LookupElementBuilder.create("odoo_form_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo form_view")
                .withTypeText("Create Form View Template")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")

                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val project = ctx.project

                    // Find existing record tags to match their indentation
                    val documentText = document.text
                    var baseIndent = "    " // Default 4 spaces

                    // Look for existing <record> tags to match their indentation
                    val recordPattern = Regex("""^(\s*)<record""")
                    documentText.lines().forEach { line ->
                        val match = recordPattern.find(line)
                        if (match != null) {
                            baseIndent = match.groups[1]?.value ?: "    "
                            return@forEach
                        }
                    }

                    // If no existing record found, look for the <odoo> tag and add one level
                    if (baseIndent == "    ") {
                        val odooPattern = Regex("""^(\s*)<odoo""")
                        documentText.lines().forEach { line ->
                            val match = odooPattern.find(line)
                            if (match != null) {
                                val odooIndent = match.groups[1]?.value ?: ""
                                baseIndent = odooIndent + "    " // Add 4 spaces for children
                                return@forEach
                            }
                        }
                    }

                    val snippet = """
                |<record id="${modelName}_view_form" model="ir.ui.view">
                |${baseIndent}    <field name="name">${modelDotName}.view.form</field>
                |${baseIndent}    <field name="model">${modelDotName}</field>
                |${baseIndent}    <field name="arch" type="xml">
                |${baseIndent}        <form string="Form Title">
                |${baseIndent}            <header>
                |${baseIndent}                <!-- Buttons and statusbar go here -->
                |${baseIndent}            </header>
                |${baseIndent}            <sheet>
                |${baseIndent}                <div class="oe_title">
                |${baseIndent}                    <h1>
                |${baseIndent}                        <field name="name"/>
                |${baseIndent}                    </h1>
                |${baseIndent}                </div>
                |${baseIndent}                <group>
                |${baseIndent}                    <group>
                |${baseIndent}                        <field name="field1"/>
                |${baseIndent}                        <field name="field2"/>
                |${baseIndent}                    </group>
                |${baseIndent}                    <group>
                |${baseIndent}                        <field name="field3"/>
                |${baseIndent}                        <field name="field4"/>
                |${baseIndent}                    </group>
                |${baseIndent}                </group>
                |${baseIndent}                <notebook>
                |${baseIndent}                    <field name="line_ids">
                |${baseIndent}                        <field name="line_field1"/>
                |${baseIndent}                        <field name="line_field2"/>
                |${baseIndent}                    </field>
                |${baseIndent}                </notebook>
                |${baseIndent}            </sheet>
                |${baseIndent}        </form>
                |${baseIndent}    </field>
                |${baseIndent}</record>
                """.trimMargin()

                    val endOffset = ctx.selectionEndOffset
                    document.replaceString(startOffset, endOffset, snippet)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    // Add newline and position cursor
                    val newCaretPosition = startOffset + snippet.length
                    document.insertString(newCaretPosition, "\n")
                    ctx.editor.caretModel.moveToOffset(newCaretPosition + 1)
                })
        )



        // List View
        resultSet.addElement(
            LookupElementBuilder.create("odoo_list_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo list view")
                .withTypeText("Create List View Template")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val snippet = """
                <record id="${modelName}_view_list" model="ir.ui.view">
                    <field name="name">${modelDotName}.view.list</field>
                    <field name="model">${modelDotName}</field>
                    <field name="arch" type="xml">
                        <list string="List Title" decoration-danger="state=='cancel'" decoration-success="state=='done'">
                            <field name="name"/>
                            <field name="date"/>
                            <field name="partner_id"/>
                            <field name="amount"/>
                            <field name="state"/>
                        </list>
                    </field>
                </record>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }
                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        // Search View
        resultSet.addElement(
            LookupElementBuilder.create("odoo_search_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo search view")
                .withTypeText("Create Search View Template")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val snippet = """
                <record id="${modelName}_view_search" model="ir.ui.view">
                    <field name="name">${modelDotName}.view.search</field>
                    <field name="model">${modelDotName}</field>
                    <field name="arch" type="xml">
                        <search string="Search Title">
                            <field name="name" string="Name" filter_domain="[('name','ilike',self)]"/>
                            <field name="partner_id"/>
                            <field name="date"/>
                            <filter string="Draft" name="draft" domain="[('state','=','draft')]"/>
                            <filter string="Done" name="done" domain="[('state','=','done')]"/>
                            <separator/>
                            <filter string="My Records" name="my_records" domain="[('user_id','=',uid)]"/>
                            <group expand="0" string="Group By">
                                <filter string="Partner" name="partner" context="{'group_by':'partner_id'}"/>
                                <filter string="Month" name="month" context="{'group_by':'date:month'}"/>
                                <filter string="Status" name="status" context="{'group_by':'state'}"/>
                            </group>
                        </search>
                    </field>
                </record>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        // Action Window
        resultSet.addElement(
            LookupElementBuilder.create("odoo_action")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo action")
                .withTypeText("Create Action Window")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val modelFormalName = modelName.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = """
                <record id="${modelName}_action" model="ir.actions.act_window">
                    <field name="name">${modelFormalName}</field>
                    <field name="res_model">${modelDotName}</field>
                    <field name="view_mode">list,form</field>
                    <field name="context">{}</field>
                    <field name="help" type="html">
                        <p class="o_view_nocontent_smiling_face">
                            Create your first record
                        </p>
                    </field>
                </record>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )
        resultSet.addElement(
            LookupElementBuilder.create("odoo_inherit_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo View Inherit")
                .withTypeText("Create Inherit View")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val modelFormalName = modelName.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = """
                <record id="${modelName}_view_type_view_inherit" model="ir.ui.view">
                    <field name="name">${modelDotName}.view_type.view.inherit</field>
                    <field name="model">${modelDotName}</field>
                    <field name="inherit_id" ref="module.original_form_view_id"/>
                    <field name="arch" type="xml">
                        <field name="arch" type="xml">
                            <!-- Add fields to an existing group -->
                            <xpath expr="//group[1]" position="inside">
                                <field name="new_field1"/>
                                <field name="new_field2"/>
                            </xpath>
                        </field>
                </record>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_pivot_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo View Pivot")
                .withTypeText("Create Pivot View")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val modelFormalName = modelName.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = """
                <record id="${modelName}_view_pivot" model="ir.ui.view">
                    <field name="name">${modelDotName}.view.pivot</field>
                    <field name="model">$modelDotName</field>
                    <field name="arch" type="xml">
                        <pivot string="$modelFormalName Pivot">
                            <field name="name"/>
                            <field name="state"/>
                        </pivot>
                    </field>
                </record>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_calender_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo View Calender")
                .withTypeText("Create Calender View")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val modelFormalName = modelName.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = """
                <record id="${modelName}_view_calender" model="ir.ui.view">
                    <field name="name">$modelDotName.view.calender</field>
                    <field name="model">$modelDotName</field>
                    <field name="arch" type="xml">
                        <calender string="$modelFormalName Calender">
                            <field name="name"/>
                            <field name="state"/>
                        </calender>
                    </field>
                </record>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )


        // Menu Item Root
        resultSet.addElement(
            LookupElementBuilder.create("odoo_menu_item_root")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo menu item root")
                .withTypeText("Add Root Menu Item")
                .withInsertHandler { ctx, _ ->
                    val file = ctx.file
                    val module = file.virtualFile?.parent?.parent?.name ?: "module_name"
                    val moduleFormalName = module.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = "<menuitem id=\"${module}_menu_root\" name=\"$moduleFormalName\" sequence=\"${(1..50).random()}\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        // Menu Item Category
        resultSet.addElement(
            LookupElementBuilder.create("odoo_menu_item_categ")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo menu item categ")
                .withTypeText("Add Category Menu Item")
                .withInsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelFormalName = modelName.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = "<menuitem id=\"unique_id_menu_categ\" name=\"$modelFormalName\" parent=\"\" sequence=\"${(1..50).random()}\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        // Menu Item Action
        resultSet.addElement(
            LookupElementBuilder.create("odoo_menu_item_action")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo menu item action")
                .withTypeText("Add Action Menu Item")
                .withInsertHandler { ctx, _ ->
                    val fileName = ctx.file.name
                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelFormalName = modelName.split('_').joinToString(" ") { it.replaceFirstChar { c -> c.uppercaseChar() } }
                    val snippet = "<menuitem id=\"unique_id_menu_categ\" name=\"$modelFormalName\" parent=\"\" action=\"\" sequence=\"${(1..50).random()}\"/>"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_button_box")
                .withPresentableText("Odoo button box")
                .withIcon(SuggestionIcons.ODOO_XML_BUTTON_ACTION)
                .withTypeText("Add Button Box")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val buttonBoxXml = """
                <div class="oe_button_box" name="button_box">
                    <button name="Button Action"
                            class="oe_stat_button"
                            icon="fa-bars"
                            type="object"
                            invisible=""
                            groups="">
                        <div class="o_stat_info">
                            <field name="" class="o_stat_value"/>
                            <span class="o_stat_text"></span>
                        </div>
                    </button>
                </div>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, buttonBoxXml)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + buttonBoxXml.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + buttonBoxXml.length)
                })
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_tag")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo Tag")
                .withTypeText("Add Odoo Tag")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val snippet = """
                <?xml version="1.0" encoding="utf-8" ?>
                <odoo>
                    
                </odoo>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        // Smart Button
        resultSet.addElement(
            LookupElementBuilder.create("odoo_button_smart")
                .withPresentableText("Odoo button smart")
                .withIcon(SuggestionIcons.ODOO_XML_BUTTON_ACTION)
                .withTypeText("Add Smart Button")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val snippet = """
                <button name="Button Action"
                        class="oe_stat_button"
                        icon="fa-bars"
                        type="object"
                        invisible=""
                        groups="">
                    <div class="o_stat_info">
                        <field name="" class="o_stat_value"/>
                        <span class="o_stat_text"/>
                    </div>
                </button>
            """.trimIndent()

                    val project = ctx.project
                    val document = ctx.document
                    val startOffset = ctx.startOffset
                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }

                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        // Status Bar State
        resultSet.addElement(
            LookupElementBuilder.create("odoo_field_state")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo field state")
                .withTypeText("Add 'state' Field in Status Bar")
                .withInsertHandler { ctx, _ ->
                    val snippet = "<field name=\"state\" widget=\"statusbar\" statusbar_visible=\"\"/>\n"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        // Field Widget
        resultSet.addElement(
            LookupElementBuilder.create("odoo_field_widget")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo field widget")
                .withTypeText("Add Widget in Fields")
                .withInsertHandler { ctx, _ ->
                    val snippet = "widget=\"\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_field_invisible")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo field invisible")
                .withTypeText("Add Invisible Condition in Fields")
                .withInsertHandler { ctx, _ ->
                    val snippet = "invisible=\"state != 'done'\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_field_readonly")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo field readonly")
                .withTypeText("Add Readonly Condition in Fields")
                .withInsertHandler { ctx, _ ->
                    val snippet = "readonly=\"not can_edit\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_field_required")
                .withIcon(SuggestionIcons.ODOO_XML_FIELD_ATTRIBUTES)
                .withPresentableText("Odoo field required")
                .withTypeText("Add Required Condition in Fields")
                .withInsertHandler { ctx, _ ->
                    val snippet = "required=\"action == 'exist'\""
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        // Chatter
        resultSet.addElement(
            LookupElementBuilder.create("odoo_chatter")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo chatter")
                .withTypeText("Add Chatter In Form")
                .withInsertHandler { ctx, _ ->
                    val snippet = "<chatter/>\n"
                    ctx.document.replaceString(ctx.startOffset, ctx.selectionEndOffset, snippet)
                    ctx.editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )
        resultSet.addElement(
            LookupElementBuilder.create("odoo_notebook")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo notebook")
                .withTypeText("Add Notebook Block")
                .withInsertHandler { ctx, _ ->
                    // Get editor and document info
                    val editor = ctx.editor
                    val document = editor.document

                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                        .takeWhile { it.isWhitespace() }

                    val snippet = """<notebook>
${indent}    <page string="New Page">
${indent}        <!-- Page content goes here -->
${indent}    </page>
${indent}</notebook>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )
        resultSet.addElement(
            LookupElementBuilder.create("odoo_xpath")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo xpath")
                .withTypeText("Add Xpath Block")
                .withInsertHandler { ctx, _ ->
                    // Get editor and document info
                    val editor = ctx.editor
                    val document = editor.document

                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                        .takeWhile { it.isWhitespace() }

                    val snippet = """<xpath expr="//div" position="inside">
${indent}    <field name="name"/>
${indent}</xpath>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )
        resultSet.addElement(
            LookupElementBuilder.create("odoo_page")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo page")
                .withTypeText("Add Page Element")
                .withInsertHandler { ctx, _ ->
                    val editor = ctx.editor
                    val document = editor.document

                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                        .takeWhile { it.isWhitespace() }

                    val snippet = """<page string="">
${indent}    <!-- Page content goes here -->
${indent}</page>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_header")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo header")
                .withTypeText("Add Header with Button + Statusbar")
                .withInsertHandler { ctx, _ ->
                    val editor = ctx.editor
                    val document = editor.document
                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString().takeWhile { it.isWhitespace() }

                    val snippet = """<header>
${indent}    <button name="" string="" class="oe_highlight" states="" type=""/>
${indent}    <field name="state" widget="statusbar" statusbar_visible="" statusbar_colors="{'draft':'blue','done':'green'}"/>
${indent}</header>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_sheet")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo sheet")
                .withTypeText("Add <sheet> with <group> and <field>")
                .withInsertHandler { ctx, _ ->
                    val editor = ctx.editor
                    val document = editor.document
                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString().takeWhile { it.isWhitespace() }

                    val snippet = """<sheet>
${indent}    <group>
${indent}        <field name="name"/>
${indent}        <field name="description"/>
${indent}    </group>
${indent}</sheet>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_record")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo Record")
                .withTypeText("Add Record Tag")
                .withInsertHandler { ctx, _ ->
                    val editor = ctx.editor
                    val document = editor.document
                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString().takeWhile { it.isWhitespace() }

                    val snippet = """<record id="" model="">
${indent}
${indent}</record>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_kanban_view")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_TEMPLATE)
                .withPresentableText("Odoo kanban view")
                .withTypeText("Create Kanban View Template")
                .withInsertHandler(InsertHandler { ctx, _ ->
                    val project = ctx.project
                    val fileName = ctx.file.name
                    val editor = ctx.editor
                    val document = editor.document

                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString()

                    val modelName = fileName.removeSuffix(".xml").removeSuffix("_views")
                    val modelDotName = modelName.replace("_",".")
                    val snippet = """<record id="${modelName}_view_kanban" model="ir.ui.view">
${indent}   <field name="name">${modelDotName}.view.kanban</field>
${indent}   <field name="model">${modelDotName}</field>
${indent}   <field name="arch" type="xml">
${indent}       <kanban class="o_kanban_example">
${indent}           <field name="name"/>
${indent}           <templates>
${indent}               <t t-name="kanban-box">
${indent}                   <div class="oe_kanban_global_click">
${indent}                       <strong><field name="name"/></strong>
${indent}                       <div><field name="description"/></div>
${indent}                   </div>
${indent}               </t>
${indent}           </templates>
${indent}       </kanban>
${indent}   </field>
${indent}</record>
            """.trimIndent()

                    val endOffset = ctx.selectionEndOffset

                    document.replaceString(startOffset, endOffset, snippet)

                    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                    PsiDocumentManager.getInstance(project).commitDocument(document)

                    if (psiFile != null) {
                        val range = TextRange(startOffset, startOffset + snippet.length)
                        CodeStyleManager.getInstance(project).reformatText(psiFile, range.startOffset, range.endOffset)
                    }
                    ctx.editor.caretModel.moveToOffset(startOffset + snippet.length)
                })
        )

        resultSet.addElement(
            LookupElementBuilder.create("odoo_group_tag")
                .withIcon(SuggestionIcons.ODOO_XML_VIEW_ELEMENTS)
                .withPresentableText("Odoo group Tag")
                .withTypeText("Add Group Tag")
                .withInsertHandler { ctx, _ ->
                    // Get editor and document infoAdd commentMore actions
                    val editor = ctx.editor
                    val document = editor.document

                    val offset = ctx.startOffset
                    val line = document.getLineNumber(offset)
                    val startOffset = document.getLineStartOffset(line)
                    val indent = document.charsSequence.subSequence(startOffset, offset).toString()
                        .takeWhile { it.isWhitespace() }

                    val snippet = """<group string="">
${indent}    <field name="name"/>
${indent}</group>"""

                    document.replaceString(ctx.startOffset, ctx.tailOffset, snippet)
                    editor.caretModel.moveToOffset(ctx.startOffset + snippet.length)
                }
        )


    }
}

class OdooWidgetAttributeCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val odooWidgets: Map<String, String> = mapOf(
            "Search..." to "Search...",
            "badge" to "Badge",
            "CopyClipboardChar" to "Copy Text to Clipboard",
            "CopyClipboardURL" to "Copy URL to Clipboard",
            "email" to "Email",
            "image_url" to "Image",
            "sms_widget" to "Multiline Text",
            "text_emojis" to "Multiline Text",
            "text" to "Multiline Text",
            "phone" to "Phone",
            "reference" to "Reference",
            "statinfo" to "Stat Info",
            "char_emojis" to "Text",
            "char" to "Text",
            "url" to "URL",
            "ace" to "Ace Editor",
            "handle" to "Handle",
            "integer" to "Integer",
            "monetary" to "Monetary",
            "percentage" to "Percentage",
            "percentpie" to "PercentPie",
            "progressbar" to "Progress Bar",
            "float" to "Float",
            "date" to "Date",
            "daterange" to "Date Range",
            "remaining_days" to "Remaining Days",
            "datetime" to "Date & Time",
            "boolean_icon" to "Boolean Icon",
            "boolean" to "Checkbox",
            "boolean_favorite" to "Favorite",
            "boolean_toggle" to "Toggle",
            "selection_badge" to "Badges",
            "state_selection" to "Label Selection",
            "priority" to "Priority",
            "radio" to "Radio",
            "selection" to "Selection",
            "statusbar" to "Status",
            "binary" to "File",
            "image" to "Image",
            "pdf_viewer" to "PDF Viewer",
            "many2many" to "Relational table (many2many)",
            "one2many" to "Relational table (one2many)",
            "many2many_tags" to "Tags",
            "many2one" to "Many2one",
            "signature" to "Signature",
            "html" to "Html",
            "float_time" to "Time"
        )


        odooWidgets.forEach { (technicalName, displayName) ->
            resultSet.addElement(
                LookupElementBuilder.create(technicalName)
                    .withIcon(SuggestionIcons.ODOO_XML_FIELD_WIDGET)
                    .withPresentableText(technicalName)
                    .withTypeText(displayName, true)
            )
        }
    }
}

class OdooxmlModelCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val position = parameters.position
        val xmlTag = PsiTreeUtil.getParentOfType(position, XmlTag::class.java) ?: return
        val nameAttr = xmlTag.getAttribute("name")?.value

        // Handle <field name="model"> or <field name="name">res...</field>
        if (xmlTag.name == "field" && (nameAttr == "model" || nameAttr == "res_model")) {
            suggestModels(parameters, resultSet)
            return
        }

        // Handle tags with res_model attribute (e.g., <record res_model="...">)
        val resModelAttr = xmlTag.getAttribute("res_model")?.value
        val attrTextRange = xmlTag.getAttribute("res_model")?.valueElement?.textRange

        if (resModelAttr != null && attrTextRange != null &&
            attrTextRange.contains(position.textOffset)) {
            suggestModels(parameters, resultSet)
        }
    }

    private fun suggestModels(parameters: CompletionParameters, resultSet: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val modelNames = FileBasedIndex.getInstance().getAllKeys(OdooModelIndex.NAME, project)

        for (model in modelNames) {
            resultSet.addElement(
                LookupElementBuilder.create(model)
                    .withTypeText("Odoo Model", true)
            )
        }
    }
}

class OdooRecordModelCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val position = parameters.position
        val xmlAttribute = PsiTreeUtil.getParentOfType(position, XmlAttribute::class.java) ?: return
        val xmlTag = xmlAttribute.parent as? XmlTag ?: return

        // Ensure we are in <record model="...">
        if (xmlTag.name == "record" && xmlAttribute.name == "model") {
            suggestModels(parameters, resultSet)
        }
    }

    private fun suggestModels(parameters: CompletionParameters, resultSet: CompletionResultSet) {
        val project = parameters.editor.project ?: return
        val modelNames = FileBasedIndex.getInstance().getAllKeys(OdooModelIndex.NAME, project)

        for (model in modelNames) {
            resultSet.addElement(
                LookupElementBuilder.create(model)
                    .withTypeText("Odoo Model", true)
            )
        }
    }
}

class OdooXmlFieldNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(
        parameters: CompletionParameters,
        context: ProcessingContext,
        resultSet: CompletionResultSet
    ) {
        val position = parameters.position
        val xmlAttributeValue = PsiTreeUtil.getParentOfType(position, XmlAttributeValue::class.java) ?: return
        val xmlAttribute = xmlAttributeValue.parent as? XmlAttribute ?: return

        if (xmlAttribute.name != "name") return

        val fieldTag = xmlAttribute.parent as? XmlTag ?: return
        if (fieldTag.name != "field") return

        val project = position.project
        val viewTags = setOf("form", "tree", "list", "kanban", "pivot", "graph", "calendar", "search")

        var currentTag: XmlTag? = fieldTag

        while (currentTag != null) {
            // Case 1: Check for view-type tag
            if (currentTag.name in viewTags) {
                var recordTag = PsiTreeUtil.getParentOfType(currentTag, XmlTag::class.java, true)
                while (recordTag != null) {
                    if (recordTag.name == "record") {
                        val fieldTags = recordTag.findSubTags("field")

                        for (fieldTag in fieldTags) {
                            val nameAttribute = fieldTag.getAttributeValue("name")
                            if (nameAttribute == "model") {
                                val modelValue = fieldTag.value.text
                                if (!modelValue.isNullOrBlank()) {
                                    suggestFields(modelValue, project, resultSet)
                                    return
                                }
                            }
                        }
                    }
                    recordTag = PsiTreeUtil.getParentOfType(recordTag, XmlTag::class.java, true)
                }
                return
            }

            if (currentTag.name == "record") {
                println("2entered")
                val modelAttr = currentTag.getAttributeValue("model")
                if (!modelAttr.isNullOrBlank()) {
                    suggestFields(modelAttr, project, resultSet)
                }
                return
            }

            currentTag = PsiTreeUtil.getParentOfType(currentTag, XmlTag::class.java)
        }
    }



    private fun findAncestorTag(tag: XmlTag, tagName: String): XmlTag? {
        var currentTag: XmlTag? = tag
        while (currentTag != null) {
            if (currentTag.name == tagName) return currentTag
            currentTag = PsiTreeUtil.getParentOfType(currentTag, XmlTag::class.java, true)
        }
        return null
    }

    private fun suggestFields(model: String, project: Project, resultSet: CompletionResultSet) {
        val fields = FileBasedIndex.getInstance()
            .getValues(OdooModelFieldIndex.NAME, model, GlobalSearchScope.allScope(project))
            .flatten()

        for (field in fields) {
            resultSet.addElement(
                LookupElementBuilder.create(field)
                    .withTypeText("Odoo Field", true)
            )
        }
    }
}




