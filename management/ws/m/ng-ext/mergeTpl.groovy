String targetFile = 'treeTable.html'

def ll = new File('tpl/' + targetFile).readLines()
ll.eachWithIndex{it, i ->
	if(i == ll.size() - 1)
		println "'" + it + "'"
	else
		println "'" + it + "' + "
}