package me.tony9.sql;

import junit.framework.Assert;
import junit.framework.TestCase;
import me.tony9.util.tree.Node;
import me.tony9.util.tree.NodeList;
import me.tony9.util.tree.NodeTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Tony on 2017/12/16.
 */
public class SqlParserTest extends TestCase {

    private static Log logger = LogFactory.getLog(SqlParserTest.class);

    private Map<String, String> loadTestCases() {

        try {
            //load test cases
            URL url = NodeTest.class.getResource("/test-case/SqlParserTest.txt");
            String file = url.getPath();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            Map<String, String> tests = new TreeMap<>();
            String testCaseName = null;
            StringBuffer str = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    //comments, do nothing
                } else if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                    if (testCaseName != null) {
                        if (tests.containsKey(testCaseName)) {
                            throw new RuntimeException(String.format("Duplicated Test Case Name '%s'!", testCaseName));
                        }
                        tests.put(testCaseName, str.toString().trim());
                        str = new StringBuffer();
                    }
                    testCaseName = line.trim();
                } else {
                    str.append(System.getProperty("line.separator")).append(line);
                }
            }
            tests.put(testCaseName, str.toString().trim());

            return tests;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test cases!", e);
        }

    }

    public void test_SQLTokenTree() {

        Map<String, String> tests = loadTestCases();

        Map<String, String> sqls = tests.entrySet().stream()
                .filter(map -> !map.getKey().endsWith(":tree]"))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        Map<String, String> trees = tests.entrySet().stream()
                .filter(map -> map.getKey().endsWith(":tree]"))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        for (String key: sqls.keySet()) {

            String sql = sqls.get(key);
            Node node = new SqlParser().parse(sql);

            String actual = node.toTreeString();

            String expected = trees.get(String.format("[%s:tree]", key.substring(1, key.length()-1)));

            logger.info(String.format("\n%s\n[%s:tree]\n %s",
                    sql.replaceAll("(\r|\n)+", " "),
                    key.substring(1, key.length()-1),
                    actual));
            Assert.assertEquals(expected.toString().trim(), actual.toString().trim());
        }
    }

    /**
     * - 获取数据表
     *
     */
    public void test_数据表() {

        String[][] tests = new String[][] {
                new String[] {"select * from a as A, b", "[a, b]"},
                new String[] {"select * from a,b join c on 1=1", "[a, b, c]"},
                new String[] {"select (a+1) as a, b, (case when a=1 then 1 when a=2 then 2 else 0 end) as c from (select b+f(x,g(y,z)) as b, c as c from (select c from t1) t2) t3", "[t1]"},
        };

        for (int i = 0; i < tests.length; i ++) {

            String sql = tests[i][0];
            Node node = new SqlParser().parse(sql);

            String tree = node.toTreeString();

            logger.info(String.format("\n%s\n %s",
                    sql.replaceAll("(\r|\n)+", " "),
                    tree));

            final List<String> tables = new ArrayList<String>();

            node.getAllChildren().stream()
                    .filter(n -> n.toString().equals("`TABLE`"))
                    .forEach(n -> {
                        Node t = (Node)n;
                        Node tableNameNode = t.getChildren().get(0);
                        if (!tableNameNode.toString().equals("`STATEMENT`")) {
                            tables.add(tableNameNode.toString());
                        }
                    });

            String actual = tables.toString();


            String expected = tests[i][1];

            logger.info("\n\n" + sql);
            Assert.assertEquals(expected.toString(), actual.toString());
        }
    }

    public void test_查询语句的返回列() {
        String[][] tests = new String[][] {
                new String[] {"select x, y as y1, f(x,y,0) as z from a as A, b", "[x, y1, z]"},
                new String[] {"select (a+1) as a2, b2, (case when a=1 then 1 when a=2 then 2 else 0 end) as c2 from (select b+f(x,g(y,z)) as b1, c as c1 from (select c from t1) t2) t3", "[a2, b2, c2]"},
        };

        for (int i = 0; i < tests.length; i ++) {

            String sql = tests[i][0];
            Node node = new SqlParser().parse(sql);

            String tree = node.toTreeString();

            logger.info(String.format("\n%s\n %s",
                    sql.replaceAll("(\r|\n)+", " "),
                    tree));

            final List<String> columns = new ArrayList<String>();

            node.getAllChildren().stream()
                    .filter(n -> {
                        Node t = (Node)n;
                        return t.toString().equals("`COLUMN`") && t.getParent().getParent().getParent() == null;
                    })
                    .forEach(n -> {
                        Node t = (Node)n;
                        NodeList children = t.getChildren();
                        Node lastNode = children.get(children.size()-1);
                        //TODO: bug, "a", "a as a", "f(x,y,z)": 第3个场景没有as，应该报错
                        columns.add(lastNode.toString());

                    });

            String actual = columns.toString();


            String expected = tests[i][1];

            logger.info("\n\n" + sql);
            Assert.assertEquals(expected.toString(), actual.toString());
        }

    }

    public void test_基于位置的变量替换() {

    }

    public void test_JOIN语句重写() {

    }

    public void test_表达式重写() {

    }

}
