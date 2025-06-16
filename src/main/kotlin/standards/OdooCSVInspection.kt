package standards

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

class OdooCSVInspection : LocalInspectionTool() {

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                // Only process ir.model.access.csv files
                if (file.name != "ir.model.access.csv") return

                processCSVFile(file, holder)
            }
        }
    }

    private fun processCSVFile(file: PsiFile, holder: ProblemsHolder) {
        val content = file.text
        val lines = content.split('\n').filter { it.trim().isNotEmpty() }

        if (lines.size < 2) return // Need at least header + one data row

        // Parse header
        val headers = parseCSVLine(lines[0])
        val modelIndex = headers.indexOfFirst { it.trim().startsWith("model_id") }
        val idIndex = headers.indexOfFirst { it.trim() == "id" }
        val nameIndex = headers.indexOfFirst { it.trim() == "name" }

        if (modelIndex == -1 || idIndex == -1 || nameIndex == -1) return

        // Process data rows
        for (i in 1 until lines.size) {
            val lineText = lines[i]
            val values = parseCSVLine(lineText)

            if (values.size <= maxOf(modelIndex, idIndex, nameIndex)) continue

            val modelName = values[modelIndex].replace("model_", "").trim()
            val expectedId = "access_$modelName"
            val expectedName = "access.${modelName.replace("_", ".")}"

            val currentId = values[idIndex].trim()
            val currentName = values[nameIndex].trim()

            // Find the actual PSI elements for error reporting
            val lineStartOffset = content.indexOf(lineText)
            if (lineStartOffset != -1) {
                // Check ID
                if (!currentId.startsWith(expectedId)) {
                    val idFieldOffset = findFieldOffset(lineText, values, idIndex)
                    if (idFieldOffset != -1) {
                        val idElement = file.findElementAt(lineStartOffset + idFieldOffset)
                        idElement?.let {
                            holder.registerProblem(it, "Id should start with $expectedId")
                        }
                    }
                }

                // Check Name
                if (!currentName.startsWith(expectedName)) {
                    val nameFieldOffset = findFieldOffset(lineText, values, nameIndex)
                    if (nameFieldOffset != -1) {
                        val nameElement = file.findElementAt(lineStartOffset + nameFieldOffset)
                        nameElement?.let {
                            holder.registerProblem(it, "Name should start with $expectedName")
                        }
                    }
                }
            }
        }
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' && (current.isEmpty() || current.last() != '\\') -> {
                    inQuotes = !inQuotes
                }
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim('"'))
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim('"'))

        return result
    }

    private fun findFieldOffset(line: String, values: List<String>, fieldIndex: Int): Int {
        if (fieldIndex >= values.size) return -1

        var offset = 0
        var currentField = 0
        var inQuotes = false

        for (i in line.indices) {
            val char = line[i]

            if (char == '"') {
                inQuotes = !inQuotes
            } else if (char == ',' && !inQuotes) {
                if (currentField == fieldIndex) {
                    return offset
                }
                currentField++
                offset = i + 1
            }
        }

        return if (currentField == fieldIndex) offset else -1
    }
}