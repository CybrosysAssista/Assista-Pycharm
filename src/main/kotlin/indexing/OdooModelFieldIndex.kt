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
                    val fieldNames = mutableMapOf<String, Map<String, String>>()

                    for (stmt in cls.statementList.statements) {
                        if (stmt is PyAssignmentStatement) {
                            val lhs = stmt.leftHandSideExpression?.text ?: continue

                            when (lhs) {
                                "_name" -> {
                                    modelName = (stmt.assignedValue as? PyStringLiteralExpression)?.stringValue
                                }
                                "_inherit" -> {
                                    // Use _inherit only if _name wasn't already set
                                    if (modelName == null) {
                                        modelName = (stmt.assignedValue as? PyStringLiteralExpression)?.stringValue
                                    }
                                }
                                else -> {
                                    val assignedValue = stmt.assignedValue
                                    if (assignedValue is PyCallExpression) {
                                        val callee = assignedValue.callee?.text ?: ""
                                        if (callee.startsWith("fields.")) {

                                            // Get arguments and create dictionary
                                            val argumentList = assignedValue.argumentList
                                            val argumentDict = mutableMapOf<String, String>()

                                            // Add field type to dictionary
                                            argumentDict["field_type"] = callee

                                            val arguments = argumentList?.arguments
                                            arguments?.forEachIndexed { index, argument ->
                                                when (argument) {
                                                    is PyKeywordArgument -> {
                                                        // Named argument: keyword=value
                                                        val keyword = argument.keyword ?: "unknown_key_$index"
                                                        val value = argument.valueExpression?.text ?: "null"
                                                        argumentDict[keyword] = value
                                                    }
                                                    else -> {
                                                        // Positional argument - use index as key
                                                        val value = argument.text
                                                        argumentDict["arg_$index"] = value
                                                    }
                                                }
                                            }

                                            fieldNames[lhs] = argumentDict

                                            println("Field: $lhs")
                                            println("Arguments dictionary: $argumentDict")

                                            // Print in formatted way
                                            println("Field definition:")
                                            argumentDict.forEach { (key, value) ->
                                                println("  $key: $value")
                                            }
                                            println() // Add blank line for readability
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (modelName != null && fieldNames.isNotEmpty()) {
                        // Convert each field and its attributes to a simple string format
                        val fieldList = fieldNames.map { (fieldName, attributes) ->
                            // Format: fieldName|attribute1=value1|attribute2=value2
                            val attributeString = attributes.entries.joinToString("|") { "${it.key}=${it.value}" }
                            "field_name=$fieldName|$attributeString"
                        }

                        result[modelName] = fieldList

                        println("Model: $modelName")
                        println("Field list: $fieldList")
                        println("=".repeat(50)) // Separator between models
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
