package standards

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import net.seesharpsoft.intellij.plugins.csv.psi.CsvRecord
import net.seesharpsoft.intellij.plugins.csv.psi.CsvVisitor

class OdooCSVInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object: CsvVisitor(){
            var heading: CsvRecord? = null
            override fun visitRecord(o: CsvRecord) {
                if(heading == null){
                    heading = o
                    return
                }
                if(holder.file.name == "ir.model.access.csv"){
                    val model = heading?.children?.find {
                        it.text.startsWith("model_id")
                    }
                    val modelIndex = heading?.children?.indexOf(model)
                    if(modelIndex != null && modelIndex != -1 && o.children.size > (modelIndex + 1)){
                        val modelName = o.children[modelIndex].text.replace("model_", "")
                        val id = "access_$modelName"
                        val name = "access.${modelName.replace("_", ".")}"
                        heading?.children?.forEach {
                            if(it.text == "id"){
                                val idIndex = heading?.children?.indexOf(it)
                                val curId = o.children[idIndex!!].text
                                if(!curId.startsWith(id)){
                                    holder.registerProblem(o.children[idIndex], "Id should start with $id")
                                }
                            }
                            if(it.text == "name"){
                                val nameIndex = heading?.children?.indexOf(it)
                                val curName = o.children[nameIndex!!].text
                                if(!curName.startsWith(name)){
//                                    val quickFix = buildCsvReplaceQuickFix(o.children[nameIndex] as CsvField, name)
                                    holder.registerProblem(o.children[nameIndex], "Name should start with $name")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}