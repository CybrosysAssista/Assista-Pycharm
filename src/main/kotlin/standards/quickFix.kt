package standards

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.xml.XmlTag
import com.jetbrains.python.psi.*

open class ManifestQuickFix : LocalQuickFix {

    override fun getFamilyName(): String {
        return "Quick fix"
    }

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val element = descriptor.psiElement
        val manifest = element.children.find { elem -> elem is PyExpressionStatement }
        if(manifest != null && manifest.children.isNotEmpty()){
            manifest.children[0].children.forEach { elem ->
                if(elem is PyKeyValueExpression) {
                    val key = replaceQuotes(elem.key.text)
                    this.checkManifestKeys(elem, key, project)
                }
            }
        }
    }

    open fun checkManifestKeys(elem: PyKeyValueExpression, key: String, project: Project){

    }

}

public fun bulidQuickFix(currentKey: String, newValue: String): ManifestQuickFix{
    return object: ManifestQuickFix(){
        override fun checkManifestKeys(elem: PyKeyValueExpression, key: String, project: Project) {
            if(key == currentKey){
                val factory = PyElementGenerator.getInstance(project)
                val newVersionExpression = factory.createExpressionFromText(LanguageLevel.forElement(elem), newValue)
                elem.value?.replace(newVersionExpression)
                triggerChanges(currentKey, newValue, elem.containingFile, project)
            }
        }
    }
}

open class ReplaceQuickFix: LocalQuickFix {
    override fun getFamilyName(): String {
        return "Replace"
    }

    override fun applyFix(p0: Project, p1: ProblemDescriptor) {

    }
}

public fun buildReplaceQuickFix(elem: XmlTag, newValue: String, attr: String, mode: String ="normal", name: String = "Replace"): LocalQuickFix {
    return object: ReplaceQuickFix(){
        override fun getFamilyName(): String {
            return name
        }
        override fun applyFix(p0: Project, p1: ProblemDescriptor) {
            when (mode) {
                "normal" -> {
                    elem.setAttribute(attr, newValue)
                }
                "val_replace" -> {
                    elem.value.text = newValue
                }
                "setAttr" -> {
                    elem.setAttribute(attr, newValue)
                }
            }
        }
    }
}

public fun buildPyQuickFix(elem: PsiElement, newValue: PsiElement, mode: String, name: String = "Replace", root: PsiElement? = null): LocalQuickFix {
    return object: ReplaceQuickFix(){
        override fun getFamilyName(): String {
            return name
        }

        override fun applyFix(p0: Project, p1: ProblemDescriptor) {
            if(mode == "after" && root != null){
                root.addAfter(newValue, elem)
            }
            if(mode == "before" && root != null){
                root.addBefore(newValue, elem)
            }
            if(mode == "add"){
                elem.add(newValue)
            }
            if(mode == "replace"){
                elem.replace(newValue)
            }
        }

    }
}

public fun buildPySetNameQuickFix(elem: PyFunction, newValue: String, name: String = "Replace"): LocalQuickFix {
    return object: ReplaceQuickFix(){
        override fun getFamilyName(): String {
            return name
        }
        override fun applyFix(p0: Project, p1: ProblemDescriptor) {
            elem.setName(newValue)
        }
    }
}

public fun buildXmlQuickFix(elem: PsiElement, newValue: PsiElement, mode: String = "add", name: String = "Add"):LocalQuickFix {
    return object: ReplaceQuickFix(){
        override fun getFamilyName(): String {
            return name
        }

        override fun applyFix(p0: Project, p1: ProblemDescriptor) {
            if(mode == "add"){
                elem.add(newValue)
            }
            if(mode == "add_start"){
                val firstElem = elem.firstChild
                elem.addBefore(newValue, firstElem)
            }
        }
    }
}
