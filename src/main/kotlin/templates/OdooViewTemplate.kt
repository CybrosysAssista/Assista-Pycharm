package templates

object OdooViewTemplate {
    fun createEmptyViewFile(): String {
        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Your custom views here -->
        </odoo>
        """.trimIndent()
    }

    fun createBasicViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelDotName = pureName.replace("_", ".")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Form View -->
            <record id="${pureName}_view_form" model="ir.ui.view">
                <field name="name">${modelDotName}.view.form</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <form string="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}">
                        <sheet>
                            <group>
                                <field name="name"/>
                                <!-- Add your fields here -->
                            </group>
                        </sheet>
                    </form>
                </field>
            </record>
            
            <!-- List View -->
            <record id="${pureName}_view_list" model="ir.ui.view">
                <field name="name">${modelDotName}.view.list</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <list string="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}">
                        <field name="name"/>
                        <!-- Add your fields here -->
                    </list>
                </field>
            </record>
            
            <!-- Action -->
            <record id="${pureName}_action" model="ir.actions.act_window">
                <field name="name">${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")}</field>
                <field name="res_model">${modelDotName}</field>
                <field name="view_mode">list,form</field>
                <field name="help" type="html">
                    <p class="o_view_nocontent_smiling_face">
                        Create your first ${pureName.replace("_", " ")}!
                    </p>
                </field>
            </record>
            
            <!-- Menu -->
            <menuitem id="${pureName}_menu"
                      name="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")}"
                      action="${pureName}_action"
                      sequence="10"/>
        </odoo>
        """.trimIndent()
    }

    fun createAdvancedViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelDotName = pureName.replace("_", ".")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Form View -->
            <record id="${pureName}_view_form" model="ir.ui.view">
                <field name="name">${modelDotName}.view.form</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <form string="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}">
                        <header>
                            <button name="action_confirm" string="Confirm" type="object" class="oe_highlight"/>
                            <button name="action_cancel" string="Cancel" type="object"/>
                            <field name="state" widget="statusbar" statusbar_visible="draft,confirmed,done"/>
                        </header>
                        <sheet>
                            <div class="oe_title">
                                <h1>
                                    <field name="name"/>
                                </h1>
                            </div>
                            <notebook>
                                <page string="General Information">
                                    <group>
                                        <group>
                                            <field name="field1"/>
                                            <field name="field2"/>
                                        </group>
                                        <group>
                                            <field name="field3"/>
                                            <field name="field4"/>
                                        </group>
                                    </group>
                                </page>
                                <page string="Lines">
                                    <field name="line_ids">
                                        <list editable="bottom">
                                            <field name="name"/>
                                            <field name="description"/>
                                            <field name="quantity"/>
                                            <field name="price"/>
                                            <field name="subtotal" sum="Total"/>
                                        </list>
                                    </field>
                                </page>
                                <page string="Notes">
                                    <field name="notes"/>
                                </page>
                            </notebook>
                        </sheet>
                        <chatter/>
                    </form>
                </field>
            </record>
            
            <!-- List View -->
            <record id="${pureName}_view_list" model="ir.ui.view">
                <field name="name">${modelDotName}.view.list</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <list string="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}" decoration-info="state=='draft'" decoration-success="state=='done'" decoration-warning="state=='confirmed'">
                        <field name="name"/>
                        <field name="field1"/>
                        <field name="field2"/>
                        <field name="create_date"/>
                        <field name="state"/>
                    </list>
                </field>
            </record>
            
            <!-- Search View -->
            <record id="${pureName}_view_search" model="ir.ui.view">
                <field name="name">${modelDotName}.view.search</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <search string="Search ${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}">
                        <field name="name"/>
                        <field name="field1"/>
                        <field name="field2"/>
                        <separator/>
                        <filter string="Draft" name="draft" domain="[('state','=','draft')]"/>
                        <filter string="Confirmed" name="confirmed" domain="[('state','=','confirmed')]"/>
                        <filter string="Done" name="done" domain="[('state','=','done')]"/>
                        <group expand="0" string="Group By">
                            <filter string="State" name="groupby_state" context="{'group_by': 'state'}"/>
                            <filter string="Creation Date" name="groupby_create_date" context="{'group_by': 'create_date:month'}"/>
                        </group>
                    </search>
                </field>
            </record>
            
            <!-- Calendar View -->
            <record id="${pureName}_view_calendar" model="ir.ui.view">
                <field name="name">${modelDotName}.view.calendar</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <calendar string="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}" date_start="create_date" color="state">
                        <field name="name"/>
                        <field name="state"/>
                    </calendar>
                </field>
            </record>
            
            <!-- Kanban View -->
            <record id="${pureName}_view_kanban" model="ir.ui.view">
                <field name="name">${modelDotName}.view.kanban</field>
                <field name="model">${modelDotName}</field>
                <field name="arch" type="xml">
                    <kanban default_group_by="state" class="o_kanban_small_column" sample="1">
                        <field name="name"/>
                        <field name="state"/>
                        <field name="field1"/>
                        <field name="field2"/>
                        <templates>
                            <t t-name="kanban-box">
                                <div class="oe_kanban_global_click">
                                    <div class="oe_kanban_details">
                                        <strong class="o_kanban_record_title">
                                            <field name="name"/>
                                        </strong>
                                        <div class="o_kanban_tags_section">
                                            <ul>
                                                <li><field name="field1"/></li>
                                                <li><field name="field2"/></li>
                                            </ul>
                                        </div>
                                    </div>
                                </div>
                            </t>
                        </templates>
                    </kanban>
                </field>
            </record>
            
            <!-- Actions -->
            <record id="${pureName}_action" model="ir.actions.act_window">
                <field name="name">${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")}</field>
                <field name="res_model">${modelDotName}</field>
                <field name="view_mode">list,form,kanban,calendar</field>
                <field name="search_view_id" ref="${pureName}_view_search"/>
                <field name="help" type="html">
                    <p class="o_view_nocontent_smiling_face">
                        Create your first ${pureName.replace("_", " ")}!
                    </p>
                </field>
            </record>
            
            <!-- Menu -->
            <menuitem id="${pureName}_menu"
                      name="${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")}"
                      action="${pureName}_action"
                      sequence="10"/>
        </odoo>
        """.trimIndent()
    }

    fun createInheritViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelDotName = pureName.replace("_", ".")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Inherit Form View -->
            <record id="${pureName}_view_form" model="ir.ui.view">
                <field name="name">${modelDotName}.view.form.inherit.module</field>
                <field name="model">${modelDotName}</field>
                <field name="inherit_id" ref="module.original_form_view_id"/>
                <field name="arch" type="xml">
                    <!-- Add fields to an existing group -->
                    <xpath expr="//group[1]" position="inside">
                        <field name="new_field1"/>
                        <field name="new_field2"/>
                    </xpath>
                    
                    <!-- Add a new page to the notebook -->
                    <xpath expr="//notebook" position="inside">
                        <page string="New Page">
                            <group>
                                <field name="new_field3"/>
                                <field name="new_field4"/>
                            </group>
                        </page>
                    </xpath>
                    
                    <!-- Modify an existing field -->
                    <xpath expr="//field[@name='existing_field']" position="attributes">
                        <attribute name="readonly">1</attribute>
                        <attribute name="string">New Label</attribute>
                    </xpath>
                    
                    <!-- Replace a field -->
                    <xpath expr="//field[@name='to_replace']" position="replace">
                        <field name="replacement_field"/>
                    </xpath>
                    
                    <!-- Add before a specific field -->
                    <xpath expr="//field[@name='reference_field']" position="before">
                        <field name="before_field"/>
                    </xpath>
                    
                    <!-- Add after a specific field -->
                    <xpath expr="//field[@name='reference_field']" position="after">
                        <field name="after_field"/>
                    </xpath>
                </field>
            </record>
            
            <!-- Inherit List View -->
            <record id="${pureName}_view_list" model="ir.ui.view">
                <field name="name">${modelDotName}.view.list.inherit.module</field>
                <field name="model">${modelDotName}</field>
                <field name="inherit_id" ref="module.original_list_view_id"/>
                <field name="arch" type="xml">
                    <xpath expr="//field[@name='reference_field']" position="after">
                        <field name="new_field"/>
                    </xpath>
                </field>
            </record>
        </odoo>
        """.trimIndent()
    }

    fun createReportViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelDotName = pureName.replace("_", ".")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Report Action -->
            <record id="${pureName}_action_report" model="ir.actions.report">
                <field name="name">${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")} Report</field>
                <field name="model">${modelDotName}</field>
                <field name="report_type">qweb-pdf</field>
                <field name="report_name">${modelDotName}.report_${pureName}</field>
                <field name="report_file">${modelDotName}.report_${pureName}</field>
                <field name="binding_model_id" ref="model_${pureName.replace(".", "_")}"/>
                <field name="binding_type">report</field>
            </record>
            
            <!-- Report Template -->
            <template id="report_${pureName}_document">
                <t t-call="web.external_layout">
                    <t t-set="doc" t-value="doc.with_context(lang=doc.partner_id.lang)" />
                    <div class="page">
                        <div class="oe_structure"/>
                        <h2>
                            <span t-field="doc.name"/>
                        </h2>
                        
                        <div class="row mt32 mb32">
                            <div class="col-auto">
                                <strong>Field 1:</strong>
                                <p t-field="doc.field1"/>
                            </div>
                            <div class="col-auto">
                                <strong>Field 2:</strong>
                                <p t-field="doc.field2"/>
                            </div>
                            <div class="col-auto">
                                <strong>Date:</strong>
                                <p t-field="doc.create_date"/>
                            </div>
                        </div>
                        
                        <table class="table table-sm o_main_table">
                            <thead>
                                <tr>
                                    <th name="th_description" class="text-left">Description</th>
                                    <th name="th_quantity" class="text-right">Quantity</th>
                                    <th name="th_price" class="text-right">Price</th>
                                    <th name="th_subtotal" class="text-right">Subtotal</th>
                                </tr>
                            </thead>
                            <tbody>
                                <t t-foreach="doc.line_ids" t-as="line">
                                    <tr>
                                        <td name="td_description"><span t-field="line.description"/></td>
                                        <td name="td_quantity" class="text-right"><span t-field="line.quantity"/></td>
                                        <td name="td_price" class="text-right"><span t-field="line.price"/></td>
                                        <td name="td_subtotal" class="text-right"><span t-field="line.subtotal"/></td>
                                    </tr>
                                </t>
                            </tbody>
                        </table>
                        
                        <div class="row">
                            <div class="col-4 offset-8">
                                <table class="table table-sm">
                                    <tr class="border-black">
                                        <td><strong>Total</strong></td>
                                        <td class="text-right">
                                            <span t-field="doc.total"/>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                        </div>
                        
                        <div class="oe_structure"/>
                    </div>
                </t>
            </template>
            
            <template id="report_${pureName}">
                <t t-call="web.html_container">
                    <t t-foreach="docs" t-as="doc">
                        <t t-call="${modelDotName}.report_${pureName}_document" t-lang="doc.partner_id.lang"/>
                    </t>
                </t>
            </template>
        </odoo>
        """.trimIndent()
    }

    fun createSecurityGroupsViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelTitle = pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")
        val moduleName = pureName.removeSuffix("_groups").removeSuffix("_group")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Custom Groups -->
            <record id="${moduleName}_group_user" model="res.groups">
                <field name="name">${modelTitle} User</field>
                <field name="category_id" ref="base.module_category_hidden"/>
                <field name="comment">Basic access to ${modelTitle}</field>
                <field name="implied_ids" eval="[(4, ref('base.group_user'))]"/>
            </record>
            
            <record id="${moduleName}_group_manager" model="res.groups">
                <field name="name">${modelTitle} Manager</field>
                <field name="category_id" ref="base.module_category_hidden"/>
                <field name="comment">Full access to ${modelTitle}</field>
                <field name="implied_ids" eval="[(4, ref('${moduleName}_group_user'))]"/>
            </record>
            
            <!-- Sample Users Data -->
            <record id="${moduleName}_group_demo_user" model="res.users">
                <field name="name">${modelTitle} Demo User</field>
                <field name="login">${pureName}_demo_user</field>
                <field name="password">${pureName}_demo_user</field>
                <field name="email">${pureName}_demo_user@example.com</field>
                <field name="groups_id" eval="[(4, ref('${moduleName}_group_user'))]"/>
            </record>
        </odoo>
        """.trimIndent()
    }

    fun createSecurityRuleViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName).removeSuffix("_rules")
        val modelDotName = pureName.replace("_", ".")
        val modelUnderscore = modelDotName.replace(".", "_")
        val moduleName = pureName

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Access Rights -->
            <record id="access_${pureName}_user" model="ir.model.access">
                <field name="name">${modelDotName}.user</field>
                <field name="model_id" ref="model_${modelUnderscore}"/>
                <field name="group_id" ref="${moduleName}_group_user"/>
                <field name="perm_read" eval="1"/>
                <field name="perm_write" eval="1"/>
                <field name="perm_create" eval="1"/>
                <field name="perm_unlink" eval="0"/>
            </record>
            
            <record id="access_${pureName}_manager" model="ir.model.access">
                <field name="name">${modelDotName}.manager</field>
                <field name="model_id" ref="model_${modelUnderscore}"/>
                <field name="group_id" ref="${moduleName}_group_manager"/>
                <field name="perm_read" eval="1"/>
                <field name="perm_write" eval="1"/>
                <field name="perm_create" eval="1"/>
                <field name="perm_unlink" eval="1"/>
            </record>
            
            <!-- Record Rules -->
            <record id="${pureName}_rule_user" model="ir.rule">
                <field name="name">${modelDotName}: Users access only their own records</field>
                <field name="model_id" ref="model_${modelUnderscore}"/>
                <field name="domain_force">[('create_uid', '=', user.id)]</field>
                <field name="perm_read" eval="1"/>
                <field name="perm_write" eval="1"/>
                <field name="perm_create" eval="1"/>
                <field name="perm_unlink" eval="0"/>
                <field name="groups" eval="[(4, ref('${moduleName}_group_user'))]"/>
            </record>
            
            <record id="${pureName}_rule_manager" model="ir.rule">
                <field name="name">${modelDotName}: Managers access all records</field>
                <field name="model_id" ref="model_${modelUnderscore}"/>
                <field name="domain_force">[(1, '=', 1)]</field>
                <field name="perm_read" eval="1"/>
                <field name="perm_write" eval="1"/>
                <field name="perm_create" eval="1"/>
                <field name="perm_unlink" eval="1"/>
                <field name="groups" eval="[(4, ref('${moduleName}_group_manager'))]"/>
            </record>
        </odoo>
        """.trimIndent()
    }

    fun createSequenceViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelDotName = pureName.replace("_", ".")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <data noupdate="1">
                <!-- Sequence -->
                <record id="seq_${pureName}" model="ir.sequence">
                    <field name="name">${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")} Sequence</field>
                    <field name="code">${modelDotName}</field>
                    <field name="prefix">${pureName.uppercase().take(3)}/%(year)s/</field>
                    <field name="padding">5</field>
                    <field name="company_id" eval="False"/>
                </record>
            </data>
        </odoo>
        """.trimIndent()
    }

    fun createSettingsViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)
        val modelDotName = pureName.replace("_", ".")

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <!-- Inherit Res Config Settings Form -->
            <record id="res_config_settings_view_form" model="ir.ui.view">
                <field name="name">res.config.settings.view.form.inherit.${pureName}</field>
                <field name="model">res.config.settings</field>
                <field name="inherit_id" ref="base.res_config_settings_view_form"/>
                <field name="arch" type="xml">
                    <xpath expr="//div[@id='settings']" position="inside">
                        <div class="app_settings_block" data-string="${
            pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")
        } Settings">
                            <h2>${pureName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }.replace("_", " ")} Settings</h2>
                            <group>
                                <field name="your_field_setting"/>
                                <!-- Add more fields if needed -->
                            </group>
                        </div>
                    </xpath>
                </field>
            </record>
        </odoo>
        """.trimIndent()
    }

    fun createCronJobViewFile(modelName: String): String {
        val pureName = extractModelNameFromFile(modelName)

        return """
        <?xml version="1.0" encoding="utf-8"?>
        <odoo>
            <data noupdate="1">
                <!-- Scheduled Action -->
                <record id="ir_cron_${pureName}" model="ir.cron">
                    <field name="name">${pureName.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }} Scheduler</field>
                    <field name="model_id" ref="model_${pureName.replace(".", "_")}"/>
                    <field name="state">code</field>
                    <field name="code">
                        model.method_to_run()
                    </field>
                    <field name="interval_number">1</field>
                    <field name="interval_type">days</field>
                    <field name="numbercall">-1</field>
                    <field name="active" eval="True"/>
                </record>
            </data>
        </odoo>
        """.trimIndent()
    }

    fun extractModelNameFromFile(modelName: String): String {
        val planName = modelName.replace(".xml", "")
        val finalName = if (planName.endsWith("_views")) planName.removeSuffix("_views") else planName
        return finalName
    }
}