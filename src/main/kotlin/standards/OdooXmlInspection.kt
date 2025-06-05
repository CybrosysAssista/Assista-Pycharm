package standards

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.util.siblings
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag

val Menu: MutableMap<String, String> = mutableMapOf()

class OdooXmlInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : XmlElementVisitor() {
            override fun visitXmlTag(tag: XmlTag) {
                when (tag.name) {
                    "record" -> checkRecord(tag)
                    "field" -> {
                        if (tag.parentTag?.name == "record" && tag.getAttribute("name")?.value == "name") {
                            checkRecordName(tag)
                        }
                        val nameAttribute = tag.attributes.firstOrNull()
                        if (nameAttribute != null && nameAttribute.name != "name") {
                            holder.registerProblem(nameAttribute, "Name attribute should be first for field tag")
                        } else if (nameAttribute == null) {
                            holder.registerProblem(tag, "Missing 'name' attribute for field tag")
                        }
                    }
                    "data" -> checkDataTag(tag)
                    "menuitem" -> checkMenuItem(tag)
                    "template" -> checkTemplate(tag)
                }
            }

            private fun checkRecord(tag: XmlTag) {
                val idAttribute = tag.getAttribute("id")
                val modelAttribute = tag.getAttribute("model")

                if (idAttribute == null) {
                    holder.registerProblem(tag, "Missing 'id' attribute")
                } else if (modelAttribute == null) {
                    holder.registerProblem(tag, "Missing 'model' attribute")
                } else if (modelAttribute.textOffset < idAttribute.textOffset) {
                    holder.registerProblem(modelAttribute, "'model' attribute should appear after 'id'")
                }
            }

            private fun checkRecordName(parentX: XmlTag) {
                val parent = parentX.parentTag
                val modelAttr = parent?.getAttribute("model")
                val name = parentX.value.text

                if (modelAttr?.value == "ir.ui.view") {
                    val model = parent.subTags.firstOrNull { it.getAttribute("name")?.value == "model" }
                    val arch = parent.subTags.firstOrNull { it.getAttribute("name")?.value == "arch" }

                    model?.let {
                        val viewModelName = it.value.text + "."
                        val tagName = arch?.subTags?.firstOrNull()?.name?.takeUnless { it == "xpath" } ?: ""
                        val expectedName = "${viewModelName}view.$tagName"
                        if (tagName.isNotEmpty() && name != expectedName) {
                            holder.registerProblem(parentX, "View name should follow Odoo standards. Expected: $expectedName")
                        } else if (!name.startsWith(viewModelName)) {
                            holder.registerProblem(parentX, "View name should start with model name: $viewModelName")
                        }
                    }
                }
            }

            private fun checkDataTag(tag: XmlTag) {
                val noUpdateAttribute = tag.getAttribute("noupdate")
                val hasSiblingData = tag.siblings(withSelf = false).any { it is XmlTag && it.name == "data" }
                val odooTag = tag.parentTag?.takeIf { it.name == "odoo" }

                if (odooTag?.getAttribute("noupdate")?.value in arrayOf("1", "true")) return
                if (noUpdateAttribute == null) {
                    holder.registerProblem(tag, "Data tag should have noupdate=\"1\" when used for not-updatable data")
                } else if (noUpdateAttribute.value in arrayOf("0", "false") ||
                    (noUpdateAttribute.value in arrayOf("1", "true") && hasSiblingData)) {
                    holder.registerProblem(noUpdateAttribute, "Data tag should have noupdate=\"1\" when used for not-updatable data")
                }
            }

            private fun checkMenuItem(tag: XmlTag) {
                val actionAttribute = tag.getAttribute("action")
                val idAttribute = tag.getAttribute("id")
                val name = tag.getAttribute("name")?.value?.replace(" ", "_")?.lowercase()

                if (idAttribute == null) {
                    holder.registerProblem(tag, "Missing 'id' attribute for menuitem")
                } else if (name != null) {
                    val modelName = actionAttribute?.value?.removeSuffix("_action") ?: ""
                    val expectedId = if (actionAttribute != null) "${modelName}_menu" else "${name}_menu"
                    if (idAttribute.value != expectedId) {
                        holder.registerProblem(idAttribute, "Menu ID should follow pattern: $expectedId")
                    }
                }
            }

            private fun checkTemplate(tag: XmlTag) {
                val idAttribute = tag.getAttribute("id")
                if (idAttribute == null) {
                    holder.registerProblem(tag, "Missing 'id' attribute for template")
                }
            }

            override fun visitXmlAttribute(attribute: XmlAttribute) {
                if (attribute.parent.name == "menuitem") {
                    attribute.parent.getAttribute("action")?.value?.let { action ->
                        attribute.parent.getAttribute("name")?.value?.replace(' ', '_')?.lowercase()?.let { name ->
                            Menu[action] = name
                        }
                    }
                }

                if (attribute.parent.name == "record" && attribute.name == "id") {
                    checkRecordId(attribute, attribute.parent)
                }
            }

            private fun checkRecordId(attribute: XmlAttribute, parent: XmlTag) {
                val modelAttr = parent.getAttribute("model")
                val id = attribute.value ?: return

                when (modelAttr?.value) {
                    "ir.ui.view" -> checkViewId(attribute, parent, id)
                    "ir.rule" -> checkRuleId(attribute, parent, id)
                    "res.groups" -> checkGroupId(attribute, id)
                    "ir.actions.act_window" -> checkWindowActionId(attribute, id)
                }
            }

            private fun checkViewId(attribute: XmlAttribute, parent: XmlTag, id: String) {
                val model = parent.subTags.firstOrNull { it.getAttribute("name")?.value == "model" }
                val arch = parent.subTags.firstOrNull { it.getAttribute("name")?.value == "arch" }

                model?.let {
                    val viewModelName = it.value.text.replace(".", "_")
                    val tagName = arch?.subTags?.firstOrNull()?.name?.takeUnless { name -> name == "xpath" } ?: ""
                    val expectedId = if (tagName.isNotEmpty()) "${viewModelName}_view_$tagName" else ""
                    if (tagName.isNotEmpty() && id != expectedId) {
                        holder.registerProblem(attribute, "View ID should follow pattern: $expectedId")
                    } else if (!id.startsWith(viewModelName)) {
                        holder.registerProblem(attribute, "View ID should start with model name: $viewModelName")
                    }
                }
            }

            private fun checkRuleId(attribute: XmlAttribute, parent: XmlTag, id: String) {
                val modelId = parent.subTags.firstOrNull { it.getAttribute("name")?.value == "model_id" }
                val domainForce = parent.subTags.firstOrNull { it.getAttribute("name")?.value == "domain_force" }

                modelId?.let {
                    val modelName = it.getAttribute("ref")?.value?.replace("model_", "") ?: return
                    val isMultiCompany = domainForce?.value?.text?.contains("company_id") == true
                    val concernedGroup = if (isMultiCompany) "company" else "public"
                    val expectedId = "${modelName}_rule_$concernedGroup"
                    if (id != expectedId) {
                        holder.registerProblem(attribute, "Rule ID should follow pattern: $expectedId")
                    }
                }
            }

            private fun checkGroupId(attribute: XmlAttribute, id: String) {
                val moduleName = holder.file.parent?.parent?.name ?: return
                val expectedPrefix = "${moduleName}_group_"
                if (!id.startsWith(expectedPrefix)) {
                    holder.registerProblem(attribute, "Group ID should follow pattern: ${expectedPrefix}<group_name>")
                }
            }

            private fun checkWindowActionId(attribute: XmlAttribute, id: String) {
                val label = Menu[id]
                val modelName = id.split("_").firstOrNull() ?: ""
                val expectedId = if (label != null) "${modelName}_action" else id
                if (label != null && id != expectedId) {
                    holder.registerProblem(attribute, "Window action ID should follow pattern: ${modelName}_action")
                } else if (!id.startsWith(modelName)) {
                    holder.registerProblem(attribute, "Window action ID should start with model name: $modelName")
                }
            }
        }
    }
}