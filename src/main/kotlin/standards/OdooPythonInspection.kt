package standards

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.python.psi.*
import java.util.*

public fun replaceQuotes(string: String): String {
    return string.replace("['\"]".toRegex(), "")
}

class OdooPythonInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object: PyElementVisitor(){
            private val propertiesComponent = ApplicationManager.getApplication().getService(PropertiesComponent::class.java)
            private val modeKey = "odooStatusBarMode"
            private var mode = propertiesComponent.getValue(modeKey, "Odoo")
            var mainModel: String = ""

            var ListPyFunctions: MutableList<PyFunction> = mutableListOf()

            override fun visitPyFile(node: PyFile) {
                if(node.children.isNotEmpty()){
                    val fixElement = PyElementGenerator.getInstance(node.project).createFromText(LanguageLevel.forElement(node), PsiComment::class.java, "# -*- coding: utf-8 -*-")
                    val quickFix = buildPyQuickFix(node.children[0], fixElement, "before", "Add", node)
                    if(node.children.isEmpty() || !node.children[0].text.contains("utf-8")){
                        holder.registerProblem(node, "Python file should start with utf-8 encoding", quickFix)
                    }
                }
                if(node.name == "__manifest__.py"){
                    val authorVal = "Odoo"
                    val websiteVal = "https://www.odoo.com"
                    val module = holder.file.containingDirectory
                    val manifest = node.children.find { elem -> elem is PyExpressionStatement }
                    if(manifest != null && manifest.children.isNotEmpty()){
                        val manifestDict = manifest.children[0]
                        val hooks = listOf("pre_init_hook", "post_init_hook", "uninstall_hook", "post_load")
                        manifestDict.children.forEach { elem ->
                            if(elem is PyKeyValueExpression){
                                val key = replaceQuotes(elem.key.text)
                                if(elem.value?.text in listOf("[]", "''", "\"\"")){
                                    holder.registerProblem(elem, "Remove empty key values")
                                    holder.registerProblem(node, "Remove empty key values")
                                }
                                if(key == "version"){
                                    val regex = Regex("""(\d+)\.(\d+)\.(\d+)\.(\d+)\.(\d+)""")
                                    val matcher = elem.value?.text?.let { regex.matchEntire(it) }
                                    if(matcher == null || matcher.groupValues.size != 6){
                                        holder.registerProblem(node, "Use 5 version numbers where the first numbers indicate the Odoo target version and the last three numbers are used as the semantic version of the module")
                                    }
                                }
                                if(key in hooks){
                                    val hasHooksPy = module.files.find { elemX ->
                                        elemX.name == "hooks.py"
                                    }
                                    val value = elem.value?.text?.let { replaceQuotes(it) }
                                    if(hasHooksPy is PyFile){
                                        val hasHook = hasHooksPy.children.find { x ->
                                            x is PyFunction && x.name == value
                                        }
                                        if(hasHook == null){
                                            holder.registerProblem(node, "Add $value in hooks.py")
                                        }
                                    } else if (hasHooksPy == null) {
                                        holder.registerProblem(node, "Add $value in hooks.py")
                                    }
                                }
                            }
                        }
                        // Required keys
                        val version = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "version"
                        }
                        val license = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "license"
                        }
                        val images = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "images"
                        }
                        val author = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "author"
                        }
                        val website = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "website"
                        }
                        val category = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "category"
                        }
                        val description = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "description"
                        }
                        val summary = manifestDict.children.find { elem ->
                            elem is PyKeyValueExpression && replaceQuotes(elem.key.text) == "summary"
                        }
                        if(description == null){
                            holder.registerProblem(node, "Description is required")
                        } else if ((description is PyKeyValueExpression) && description.value?.text == "\"\""){
                            holder.registerProblem(node, "Description is required")
                        }
                        if(summary == null){
                            holder.registerProblem(node, "Summary is required")
                        } else if ((description is PyKeyValueExpression) && description.value?.text == "\"\""){
                            holder.registerProblem(node, "Summary is required")
                        }
                        if(summary != null && description != null && (summary is PyKeyValueExpression) && (description is PyKeyValueExpression) && summary.value?.text == description.value?.text){
                            holder.registerProblem(node, "Summary and Description should be different")
                        }
                        if(version == null){
                            holder.registerProblem(node, "Version is required")
                        }
                        if(license == null){
                            holder.registerProblem(node, "License is required")
                        }
                        if(images == null || (images is PyKeyValueExpression && images.value != null && images.value!!.text != null && replaceQuotes(images.value!!.text!!).all { it == ' ' || it == '[' || it == ']' })){
                            holder.registerProblem(node, "Images is required")
                        }
                        if(category == null){
                            holder.registerProblem(node, "Category is required, Navigate to Apps: You can see the categories on the left side")
                        }
                    }
                    val allowedDirectory = listOf(
                        "controllers", "data", "demo", "examples", "lib", "models", "reports", "security", "static",
                        "templates", "views", "wizards", "i18n", "tests", "doc"
                    )
                    module?.children?.forEach { dir ->
                        if(dir is PsiDirectory){
                            if(!dir.name.startsWith(".")){
                                if(dir.name !in allowedDirectory){
                                    holder.registerProblem(node, "${dir.name} is not on allowed directories." +
                                            "Allowed directories are ${allowedDirectory.joinToString(separator = ", ", transform = { it })}")
                                }
                            }
                        }
                    }
                }
                if(node.name == "__init__.py"){
                    val listPyImports: MutableList<String> = mutableListOf()
                    node.fromImports.forEach{ impt ->
                        if(impt is PyFromImportStatement){
                            listPyImports.add(impt.text.split(" ").last())
                        }
                    }
                    listPyImports.forEachIndexed { index, _ ->
                        if(listPyImports[index] != listPyImports.sortedWith(String.CASE_INSENSITIVE_ORDER)[index]){
                            val wrongImport = node.fromImports.find { impt -> impt.text.endsWith(listPyImports[index]) }
                            if(wrongImport != null){
                                holder.registerProblem(wrongImport, "Import is not alphabetically ordered")
                            }
                        }
                    }
                }
            }

            override fun visitPyElement(node: PyElement) {
                super.visitPyElement(node)
                if (node is PyFromImportStatement) {
                    val ogImports = node.children.map { elem -> elem.node.text }
                    val main = ogImports[0]
                    val imports = ogImports.drop(1)
                    val sorted = imports.sortedWith(String.CASE_INSENSITIVE_ORDER)
                    val isSorted = imports == sorted
                    val newText = "from $main import ${sorted.joinToString(separator = ", ", transform = { it })}"
                    if (!isSorted) {
                        val elem = PyElementGenerator.getInstance(node.project).createFromText(LanguageLevel.forElement(node), PyFromImportStatement::class.java, newText)
                        val quickFix = buildPyQuickFix(node, elem, "replace")
                        holder.registerProblem(node, "Import is not alphabetically ordered", quickFix)
                    }
                }
            }

            override fun visitPyFunction(node: PyFunction) {
                super.visitPyFunction(node)
                if(node.statementList.children.isNotEmpty()){
                    val firstElem = node.statementList.children.first()
                    if(firstElem !is PyExpressionStatement  && node.identifyingElement != null && holder.file.parent?.name != "tests"){
                        holder.registerProblem(node.identifyingElement!!, "Function missing Docstring")
                    }
                }
                val dec = node.decoratorList?.decorators
                if(dec != null){
                    if(dec[0].name == "onchange" && dec[0].arguments.isNotEmpty()){
                        val fieldName = replaceQuotes(dec[0].arguments[0].text)
                        val ogFunctionName = "_onchange_$fieldName"
                        if (node.name != ogFunctionName){
                            val quickFix = buildPySetNameQuickFix(node, ogFunctionName)
                            holder.registerProblem(node.identifyingElement!!, "Onchange function name should follow Odoo standards. Function name should be $ogFunctionName", quickFix)
                        }
                    }
                    if(dec[0].name == "constrains" && dec[0].arguments.isNotEmpty()){
                        val fieldName = replaceQuotes(dec[0].arguments[0].text)
                        val ogFunctionName = "_check_$fieldName"
                        if (node.name != ogFunctionName){
                            val quickFix = buildPySetNameQuickFix(node, ogFunctionName)
                            holder.registerProblem(node.identifyingElement!!, "Constrains function name should follow Odoo standards. Function name should be $ogFunctionName", quickFix)
                        }
                    }
                }
            }

            override fun visitPyClass(node: PyClass) {
                super.visitPyClass(node)
                val firstElem = node.statementList.children.first()
                if(firstElem !is PyExpressionStatement && holder.file.parent?.name != "tests"){
                    if (node.identifyingElement != null){
                        holder.registerProblem(node.identifyingElement!!, "Class missing Docstring")
                    } else {
                        holder.registerProblem(firstElem, "Class missing Docstring")
                    }
                }
                val name = node.statementList.children.find { elem ->
                    elem is PyAssignmentStatement &&
                            elem.children.isNotEmpty() &&
                            elem.children[0].text == "_name"
                }
                val description = node.statementList.children.find { elem ->
                    elem is PyAssignmentStatement &&
                            elem.children.isNotEmpty() &&
                            elem.children[0].text == "_description"
                }
                if(name != null && description == null){
                    holder.registerProblem(name, "Provide a _description for the class")
                }
            }

            override fun visitPyAssignmentStatement(node: PyAssignmentStatement) {
                super.visitPyAssignmentStatement(node)
                if(node.parent.parent is PyClass && node.children.size > 1){
                    val classObj = node.parent.parent as PyClass
                    if(node.children[0].text == "_name" && classObj.superClassExpressions.isNotEmpty() && !classObj.superClassExpressions[0].text.contains("AbstractModel")){
                        val modelName = replaceQuotes(node.children[1].text)
                        if(mainModel == ""){
                            mainModel = modelName
                            val fileName = holder.file.name
                            val ogFileName = "${modelName.replace(".", "_")}.py"
                            if(fileName != ogFileName){
                                holder.registerProblem(classObj.parent, "File name must follow Odoo standards. File name should be $ogFileName")
                            }
                        }
                        val modelList = modelName.split(".", "_").joinToString(separator = "", transform = { it.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(
                                Locale.getDefault()
                            ) else it.toString()
                        } })
                        if((classObj).name != modelList && classObj.identifyingElement != null && classObj.superClassExpressions.isNotEmpty() && !classObj.superClassExpressions[0].text.contains("AbstractModel")){
                            holder.registerProblem(classObj.identifyingElement!!,
                                "Class name is not as per Odoo standards. Class name should be $modelList")
                        }
                        if(!modelName.all { it == '.' || it == '_' || it.isLowerCase() }){
                            holder.registerProblem(node.children[0], "_name can only contain lowercase letters, dot and underscore")
                        }
                    }
                    else if(node.children[1].text.startsWith("fields.Many2one")){
                        if(!node.children[0].text.endsWith("_id")){
                            holder.registerProblem(node.children[0], "Many2one field name should follow Odoo standards. Field name should end with _id")
                        }
                    }
                    else if(node.children[1].text.startsWith("fields.Many2many")){
                        if(!node.children[0].text.endsWith("_ids")){
                            holder.registerProblem(node.children[0], "Many2many field name should follow Odoo standards. Field name should end with _ids")
                        }
                    }
                    else if(node.children[1].text.startsWith("fields.One2many")){
                        if(!node.children[0].text.endsWith("_ids")){
                            holder.registerProblem(node.children[0], "One2many field name should follow Odoo standards. Field name should end with _ids")
                        }
                    }
                    if(node.children[1] is PyCallExpression){
                        val field = node.children[1] as PyCallExpression
                        val compute = field.getKeywordArgument("compute")
                        if(compute != null){
                            val functionName = replaceQuotes(compute.text)
                            val ogFuncName = "_compute_${node.children[0].text}"
                            val funcObj = classObj.methods.find { elem -> elem.name == functionName }
                            if(functionName != ogFuncName && !ListPyFunctions.contains(funcObj)){
                                if(funcObj != null){
                                    holder.registerProblem(funcObj.identifyingElement!!, "Compute function name should follow Odoo standards. Function name should be $ogFuncName")
                                }
                            }
                            if(funcObj != null){
                                ListPyFunctions.add(funcObj)
                            }
                        }
                        val search = field.getKeywordArgument("search")
                        if(search != null){
                            val functionName = replaceQuotes(search.text)
                            val ogFuncName = "_search_${node.children[0].text}"
                            if(functionName != ogFuncName){
                                val funcObj = classObj.methods.find { elem -> elem.name == functionName }
                                if(funcObj != null){
                                    holder.registerProblem(funcObj, "Search function name should follow Odoo standards. Function name should be $ogFuncName")
                                }
                            }
                        }
                        val inverse = field.getKeywordArgument("inverse")
                        if(inverse != null){
                            val functionName = replaceQuotes(inverse.text)
                            val ogFuncName = "_inverse_${node.children[0].text}"
                            if(functionName != ogFuncName){
                                val funcObj = classObj.methods.find { elem -> elem.name == functionName }
                                if(funcObj != null){
                                    holder.registerProblem(funcObj, "Inverse function name should follow Odoo standards. Function name should be $ogFuncName")
                                }
                            }
                        }
                        val required = field.getKeywordArgument("required")
                        val readonly = field.getKeywordArgument("readonly")
                        val related = field.getKeywordArgument("related")
                        val store = field.getKeywordArgument("store")
                        val string = field.getKeywordArgument("string")
                        val help = field.getKeywordArgument("help")
                        val default = field.getKeywordArgument("default")
                        if(required != null && readonly != null){
                            if(replaceQuotes(required.text) == "True" && replaceQuotes(readonly.text) == "True"){
                                holder.registerProblem(readonly, "Field contain both readonly and required as True")
                                holder.registerProblem(required, "Field contain both readonly and required as True")
                            }
                        }
                        if(required != null && replaceQuotes(required.text) == "False"){
                            holder.registerProblem(required, "For all fields required is False. Don't have to specify it")
                        }
                        if(related == null && compute == null && readonly != null && replaceQuotes(readonly.text) == "False"){
                            holder.registerProblem(readonly, "For stored fields readonly is False. Don't have to specify it")
                        }
                        if(related == null && compute == null && store != null && replaceQuotes(store.text) == "True"){
                            holder.registerProblem(store, "For non compute or non related fields store is True. Don't have to specify it")
                        }
                        if((related != null || compute != null) && store != null && replaceQuotes(store.text) == "False"){
                            holder.registerProblem(store, "For compute or related fields store is False. Don't have to specify it")
                        }
                        if((related != null || compute != null) && readonly != null && replaceQuotes(readonly.text) == "True"){
                            holder.registerProblem(readonly, "For compute or related fields readonly is True. Don't have to specify it")
                        }
                        if((related != null || compute != null) && default != null){
                            holder.registerProblem(default, "For compute or related fields default value is not needed")
                        }
                        val fieldName = node.children[0].text
                        var fieldNameString: String? = null
                        if(fieldName.endsWith("_id")){
                            fieldNameString = fieldName.replace("_id", "").split("_").joinToString(separator = " ", transform = { it.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            } })
                        } else if(fieldName.endsWith("_ids")){
                            fieldNameString = fieldName.replace("_ids", "").split("_").joinToString(separator = " ", transform = { it.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            } })
                        }
                        else {
                            fieldNameString = fieldName.split("_").joinToString(separator = " ", transform = { it.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(
                                    Locale.getDefault()
                                ) else it.toString()
                            } })
                        }
                        if(string != null){
                            val stringText = replaceQuotes(string.text).replace("_", "")
                            if(stringText.first().isLowerCase()){
                                holder.registerProblem(string, "String should start with a capital letter")
                            }
                            if(fieldNameString.equals(stringText, ignoreCase = true)){
                                holder.registerProblem(string, "String attribute is not needed since field name is same as string")
                            }
                        }
                        if (string != null && help != null){
                            if(replaceQuotes(string.text) == replaceQuotes(help.text)){
                                holder.registerProblem(help, "String and Help attribute can't be same. Either remove help or change it")
                            }
                        }
                        if(default is PyReferenceExpression && !default.text.startsWith("fields.Date")){
                            holder.registerProblem(default, "Use lambda to call a default function")
                        }
                        if(default is PyLambdaExpression){
                            val defaultFunction = default.children.find { elem -> elem is PyCallExpression }
                            if (defaultFunction != null){
                                val actualFunction = defaultFunction.text.split('.').last()
                                val ogFuncName = "_default_$fieldName()"
                                if(actualFunction != ogFuncName){
                                    holder.registerProblem(default, "Default function name should follow Odoo standards. Function name should be $ogFuncName")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}