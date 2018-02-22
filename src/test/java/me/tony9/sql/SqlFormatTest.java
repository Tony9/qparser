package me.tony9.sql;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tony on 2017/12/16.
 */
public class SqlFormatTest extends TestCase {

    private static Log logger = LogFactory.getLog(SqlFormatTest.class);

    public void test_SQL_prettify() throws IOException {

        URL url = SqlFormatTest.class.getResource("/test-case/SqlFormatTest.txt");
        String file = url.getPath();
        BufferedReader reader = new BufferedReader(new FileReader(file));

        List<String> sqls = new ArrayList<>();
        StringBuffer str = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("--")) {
                //do nothing
            } else if (line.trim().equals(";")) {
                sqls.add(str.toString().trim());
                str = new StringBuffer();
            } else {
                str.append(System.getProperty("line.separator")).append(line);
            }
        }

        assert (sqls.size() % 2 == 0);

        for (int i = 0; i < sqls.size(); i += 2) {

            String sql = sqls.get(i);
//            String formattedSql = new SqlFormat().format(sql);

            String expected = sqls.get(i+1);

//            logger.info("\n\n" + sql + "\n=>\n" + formattedSql);
//            Assert.assertEquals(expected.toString(), formattedSql);
        }
    }



}
