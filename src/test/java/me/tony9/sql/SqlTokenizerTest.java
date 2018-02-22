package me.tony9.sql;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Created by Tony on 2017/12/16.
 */
public class SqlTokenizerTest extends TestCase {

    private static Log logger = LogFactory.getLog(SqlTokenizerTest.class);

    public void test_单引号() {

        String[][] tests = new String[][] {
                new String[] {"''", "['']"},
                new String[] {"'a'", "['a']"},
                new String[] {"'abc'", "['abc']"},
                new String[] {"''''", "['''']"},
                new String[] {"''a''", "['' / a / '']"},
                new String[] {"'a''b'", "['a''b']"},
                new String[] {"'a''f(b,1, 2) \t\r\nc'", "['a''f(b,1, 2) \t\r\nc']"},
                new String[] {"'a''b''''c''''''d'", "['a''b''''c''''''d']"},
                new String[] {"'a' 'b'", "['a' / 'b']"},
                new String[] {"'a','b'", "['a' / , / 'b']"},
                new String[] {"select 'a','b' from t", "[select / 'a' / , / 'b' / from / t]"},
        };

        for (int i = 0; i < tests.length; i ++) {
            StringBuffer actual = new StringBuffer();
            String sql = tests[i][0];
            List<SqlToken> tokens = new SqlTokenizer().split(sql);

            actual.append('[');
            for (int j = 0; j < tokens.size(); j ++) {
                if (j > 0) actual.append(" / ");
                actual.append(tokens.get(j));
            }
            actual.append(']');
            String expected = tests[i][1];

            logger.info(String.format("s = %s", sql.replaceAll("(\r|\n)+", " ")));
            Assert.assertEquals(expected.toString(), actual.toString());

            for (SqlToken token: tokens) {
                Assert.assertEquals(sql.substring(token.getStartPos(), token.getEndPos()), token.getText());
            }

        }
    }

    public void test_SQL语句_不合并() {

        String[][] tests = new String[][] {
                new String[] {"select 'a','b' from t", "[select / 'a' / , / 'b' / from / t]"},
                new String[] {"/*TEST COMMENT*/select 'a' --SingleLineComment\n,'b' from t", "[/*TEST COMMENT*/ / select / 'a' / --SingleLineComment / , / 'b' / from / t]"},
                new String[] {" SELECT 'a',b,'c' FROM t", "[SELECT / 'a' / , / b / , / 'c' / FROM / t]"},
                new String[] {"select 'a',substr(b, 1, 2),'c' from t", "[select / 'a' / , / substr / ( / b / , / 1 / , / 2 / ) / , / 'c' / from / t]"},
                new String[] {"select x,y from t left join t1 on 1=1 right join t2 on 1>2 and 1<3", "[select / x / , / y / from / t / left / join / t1 / on / 1 / = / 1 / right / join / t2 / on / 1 / > / 2 / and / 1 / < / 3]"},
                new String[] {"create    table xxx as (\nselect #a,substr(b, #start, #end),'select * from t' \nfrom t \n)with test", "[create / table / xxx / as / ( / select / #a / , / substr / ( / b / , / #start / , / #end / ) / , / 'select * from t' / from / t / ) / with / test]"},
        };

        for (int i = 0; i < tests.length; i ++) {
            StringBuffer actual = new StringBuffer();
            String sql = tests[i][0];
            List<SqlToken> tokens = new SqlTokenizer().split(sql);

            actual.append('[');
            for (int j = 0; j < tokens.size(); j ++) {
                if (j > 0) actual.append(" / ");
                actual.append(tokens.get(j));
            }
            actual.append(']');
            String expected = tests[i][1];

            logger.info(String.format("s = %s", sql.replaceAll("(\r|\n)+", " ")));
            Assert.assertEquals(expected.toString(), actual.toString());
        }
    }
}
