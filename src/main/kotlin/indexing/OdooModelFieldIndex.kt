    package indexing

    import com.intellij.util.indexing.*
    import com.intellij.util.io.*
    import com.intellij.psi.PsiFile
    import com.intellij.openapi.fileTypes.FileType
    import com.jetbrains.python.PythonFileType
    import com.jetbrains.python.psi.*
    import com.intellij.util.indexing.FileContent

    class OdooModelFieldIndex : FileBasedIndexExtension<String, List<String>>() {

        companion object {
            val NAME: ID<String, List<String>> = ID.create("OdooModelFieldIndex")
        }

        override fun getName(): ID<String, List<String>> = NAME

        override fun getIndexer(): DataIndexer<String, List<String>, FileContent> {
            return DataIndexer { inputData ->
                val result = mutableMapOf<String, List<String>>()
                val pyFile = inputData.psiFile as? PyFile ?: return@DataIndexer result

                for (cls in pyFile.topLevelClasses) {
                    val isModel = cls.superClassExpressions.any {
                        it.text.contains("Model")
                    }

                    if (!isModel) continue

                    var modelName: String? = null
                    val fieldNames = mutableListOf<String>()

                    for (stmt in cls.statementList.statements) {
                        if (stmt is PyAssignmentStatement) {
                            val lhs = stmt.leftHandSideExpression?.text ?: continue

                            if (lhs == "_name") {
                                modelName = (stmt.assignedValue as? PyStringLiteralExpression)?.stringValue
                            } else {
                                // Collect fields like name = fields.Char(...)
                                val assignedValue = stmt.assignedValue
                                if (assignedValue is PyCallExpression) {
                                    val callee = assignedValue.callee?.text ?: ""
                                    if (callee.startsWith("fields.")) {
                                        fieldNames.add(lhs)
                                    }
                                }
                            }
                        }
                    }
                    if (modelName != null && fieldNames.isNotEmpty()) {
                        result[modelName] = fieldNames
                    }
                }
                return@DataIndexer result
            }
        }

        override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

        override fun getValueExternalizer(): DataExternalizer<List<String>> = ListOfStringDataExternalizer

        override fun getInputFilter(): FileBasedIndex.InputFilter =
            DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE)

        override fun dependsOnFileContent(): Boolean = true

        override fun getVersion(): Int = 1
    }
