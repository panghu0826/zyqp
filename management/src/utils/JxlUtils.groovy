package utils

import jxl.*
import jxl.write.Label
import jxl.write.WritableWorkbook
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.text.SimpleDateFormat

/**
 * Created by kerry on 2016/6/1.
 * excel读写工具类
 */
class JxlUtils {
    private static Logger log = LoggerFactory.getLogger(JxlUtils.class)

    /**
     *
     * @param is
     * @param cols eg. a..<g
     * @param beginRow
     * @return
     */
    static List<Map<String, String>> read(InputStream is, List<String> cols, int beginRow = 1, int sheetIndex = 0, int maxRow = 1000) {
        List<Map<String, String>> ll = []
        if (!is || !cols)
            return ll

        Workbook wb
        try {
            TimeZone zone = TimeZone.getTimeZone("GMT");
            def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.setTimeZone(zone);

            wb = Workbook.getWorkbook(is)

            def sheet = wb.sheets[sheetIndex]
            for (i in beginRow..<sheet.rows) {
                Map<String, String> item = [:]
                cols.eachWithIndex { cc, j ->
                    def cell = sheet.getCell(j, i)
                    String content = null
                    if (cell.getType() == CellType.DATE) {
                        DateCell dc = (DateCell) cell;
                        Date date = dc.getDate();    //获取单元格的date类型
                        content = sdf.format(date);
                    } else {
                        content = cell.getContents()
                    }
                    if (content == null)
                        content = ''

                    item[cc] = content
                }
                if (item[cols[0]])
                    ll << item
            }
        } catch (Exception e) {
            log.error('parse excel error', e)
        } finally {
            if (wb != null)
                wb.close()
            is.close()
        }

        ll
    }

    static byte[] write(String sheetTitle, List<List> ll) {
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        WritableWorkbook wb = Workbook.createWorkbook(os)

        try {
            def sheet = wb.createSheet(sheetTitle, 0)

            int i = 0
            for (one in ll) {
                int j = 0
                for (inner in one) {
                    Label label = new Label(j, i, inner.toString())
                    sheet.addCell(label)
                    j++
                }
                i++
            }

            wb.write()
            wb.close()
        } catch (Exception e) {
            log.error('write excel error', e)
        }

        os.toByteArray()
    }

    /**
     *
     * @param is
     * @param cols eg. a..<g
     * @param beginRow
     * @return
     */
    static List<Map<String, String>> readIssueExcel(InputStream is, List<String> cols, int beginRow = 1, int sheetIndex = 0) {
        List<Map<String, String>> ll = []
        if (!is || !cols)
            return ll

        Workbook wb
        try {
            TimeZone zone = TimeZone.getTimeZone("GMT");
            def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            sdf.setTimeZone(zone);

            wb = Workbook.getWorkbook(is)

            def sheet = wb.sheets[sheetIndex]

            String pName, cName
            for (i in beginRow..<sheet.rows) {
                Map<String, String> item = [:]
                cols.eachWithIndex { cc, j ->
                    def cell = sheet.getCell(j, i)
                    String content = null
                    if (cell.getType() == CellType.DATE) {
                        DateCell dc = (DateCell) cell;
                        Date date = dc.getDate();    //获取单元格的date类型
                        content = sdf.format(date);
                    } else {
                        content = cell.getContents()
                    }

                  /*  if (!content && j == 0) {
                        content = pName
                    } else if (content && j == 0) {
                        pName = content
                    }

                    if (!content && j == 1) {
                        content = cName
                    } else if (content && j == 1) {
                        cName = content
                    }*/

                    item[cc] = content
                }
                ll << item
            }
        } catch (Exception e) {
            log.error('parse excel error', e)
        } finally {
            if (wb != null)
                wb.close()
            is.close()
        }

        ll
    }
}
