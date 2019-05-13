String encoding = 'utf-8'

def merge = {dist, files ->
	def ll = files.readLines().grep{it.trim()}
	def lines = []
	for(one in ll){
		lines.addAll(new File(one).readLines(encoding))
		if(one.contains('uploadify.min'))
			lines << ';'
	}

	dist.withPrintWriter(encoding){w ->
		for(line in lines)
			w.println line
	}
}

merge(new File('all.js'), '''
underscore.min.js
jquery-1.8.3.min.js
jquery.ui.core.min.js
jquery.ui.datepicker.min.js
jquery-ui-timepicker.min.js
jquery.ui.widget.min.js
jquery.ui.mouse.min.js
jquery.ui.sortable.min.js
jquery-lhgdialog-4.2.0.min.js
uploadify/jquery.uploadify.min.js
angular.min.js
'''
)

merge(new File('ng.ext.js'), '''
ng.ext.core.js
ng.ext.filter-grid.js
ng.ext.tree.js
ng.ext.uploadify.js
'''
)