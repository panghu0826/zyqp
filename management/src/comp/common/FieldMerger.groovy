package comp.common

import com.guosen.webx.d.D
import com.guosen.webx.d.NamingStyleUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by kerry on 2016/6/17.
 */
@Component
class FieldMerger {
    @Autowired
    @Qualifier("d")
    D d

    public setD(D d) {
        this.d = d;
    }

    public static FieldMerger getInstance(D d) {
        FieldMerger f = new FieldMerger()
        f.setD(d)
        return f;
    }

    // 保证有该字段，尽管值可能是null
    private void setIfNull(Map one, String[] fieldList) {
        for (String field : fieldList) {
            one[field] = null
        }
    }

    /**
     * 一多关联的场景，sql join逻辑在java中实现
     *
     * @param list 一对多中多表查出来的子集
     * @param srcId 多表关联主表的字段名称
     * @param fields 主表需要的字段信息
     * @param table 主表
     */
    public void merge(List<Map> list, String srcId, String fields, String table, String keyCol = "id") {
        if (!list)
            return

        List args = list.collect { it[srcId] }

        String[] fieldList = fields.split(',')
        String[] fieldQueryedList = fieldList.collect { String field ->
            String tmp = field.contains(' as ') ? field.split('as')[1].trim() : field
            tmp.contains('_') ? NamingStyleUtils.toCamel(tmp, true) : tmp
        }
        String fieldsUnderscore4Query = fieldList.collect { NamingStyleUtils.toUnderline(it) }.join(',')

        String sql = "select ${NamingStyleUtils.toUnderline(keyCol)},${fieldsUnderscore4Query} from ${table} where ${NamingStyleUtils.toUnderline(keyCol)} in (${args.collect { '?' }.join(',')})"

        List ll = d.query(sql, args)
        if (!ll) {
            for (one in list)
                setIfNull(one, fieldQueryedList)
            return
        }

        final String keySetFlag = '_merged'

        for (one in ll) {
            List rowList = list.grep { it[srcId] == one.get(keyCol) }
            if (!rowList)
                continue

            for (row in rowList) {
                row[keySetFlag] = 1
                for (field in fieldQueryedList) {
                    row[field] = one[field]
                }
            }
        }

        for (one in list) {
            if (!one[keySetFlag])
                setIfNull(one, fieldQueryedList)
            else
                one.remove(keySetFlag)
        }
    }

    /**
     * 一多关联的场景，sql join逻辑在java中实现
     *
     * @param list 一对多中主表查出来的子集
     * @param srcId 主表关联多表的字段名称
     * @param targetId 多表关联主表的外键字段名称
     * @param fields 多表需要的字段信息
     * @param table 多表
     */
    public void mergeSubList(List list, String srcId, String targetId, String fields, String table, String subListTitle = "subList") {
        if (!list)
            return

        List args = list.collect { it[srcId] }

        String[] fieldList = fields.split(',')
        String fieldsUnderscore = fieldList.collect { NamingStyleUtils.toUnderline(it) }.join(',')
        String targetIdUnderscore = NamingStyleUtils.toUnderline(targetId)

        String sql = "select ${fieldsUnderscore},${targetIdUnderscore} from ${table} where ${targetIdUnderscore} in (${args.collect { '?' }.join(',')})"

        List ll = d.query(sql, args)
        if (!ll)
            return

        for (one in list) {
            List subList = ll.grep { it[targetId] == one[srcId] }
            if (!subList)
                continue

            one[subListTitle] = subList
        }
    }


}
