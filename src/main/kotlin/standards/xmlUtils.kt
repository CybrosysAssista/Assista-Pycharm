package standards

import com.intellij.psi.PsiDirectory
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

fun getViews(root: PsiDirectory): MutableList<XmlTag> {
    val listOfViews = mutableListOf<XmlTag>()
    val viewDir = root.childrenOfType<PsiDirectory>().find {
        it.name == "views"
    }
    if (viewDir == null) return listOfViews
    viewDir.childrenOfType<XmlFile>().forEach {
        it.rootTag?.subTags?.forEach { tag ->
            if(it.name == "record" && tag.getAttribute("model")?.value == "ir.ui.view"){
                listOfViews.add(tag)
            }
        }
    }
    return listOfViews
}

fun filterStringForOdoo(string: String, replacer: String, replace: String): String {
    val res = string.replace(replacer, replace)
    if(res.contains(replacer)){
        return filterStringForOdoo(res, replacer, replace)
    }
    return res
}
