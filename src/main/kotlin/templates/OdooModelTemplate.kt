package templates

object OdooModelTemplate {
    fun createCustomFile(className: String): String {
        val formattedClassName = className.split("_").joinToString("") { it.capitalize() }
        return """
        # -*- coding: utf-8 -*-

        from odoo import api, fields, models

        class $formattedClassName(models.Model):
            _name = '${className.replace(".", "_")}'
            _description = 'Description'
            
            name = fields.Char(string='Customer Name', required=True)
            # Add your fields here
            
            # Add your methods here
        """.trimIndent()
    }

    fun createInitFile(): String {
        return """
        # -*- coding: utf-8 -*-

        from . import models
        # from . import controllers
        """.trimIndent()
    }

    fun createManifestFile(): String {
        return """
        {
            'name': 'ModuleName',
            'version': 'Version',
            'summary': 'Summary',
            'description': 'Description',
            'category': 'Category',
            'author': 'Author',
            'website': 'Website',
            'license': 'License',
            'depends': ['Depends'],
            'data': ['Data'],
            'demo': ['Demo'],
            'installable': True,
            'auto_install': False,
        }
        """.trimIndent()
    }

    fun createModelFile(): String {
        return """
        # -*- coding: utf-8 -*-

        from odoo import api, fields, models

        class Model(models.Model):
            _name = 'module.name'
            _description = 'Model Description'
            
            name = fields.Char(string='Customer Name', required=True)
            # Add your fields here
            
            # Add your methods here
        """.trimIndent()
    }

    fun createControllerFile(): String {
        return """
        # -*- coding: utf-8 -*-

        from odoo import http
        from odoo.http import request

        class Controller(http.Controller):
            @http.route('/path', type='http', auth='public', website=True)
            def handler(self, **kw):
                return request.render('module.template_id', {
                    'values': [],
                })
        """.trimIndent()
    }
}
