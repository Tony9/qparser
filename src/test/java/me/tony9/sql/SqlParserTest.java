package me.tony9.sql;

import junit.framework.Assert;
import junit.framework.TestCase;
import me.tony9.util.tree.Node;
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

    private Map<String, String> loadAllTestCases() {

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

    private String[][] loadTestCases(String testName) {

        Map<String, String> allTests = loadAllTestCases();

        Map<String, String> sqls = allTests.entrySet().stream()
                .filter(map -> map.getKey().split(":").length == 2)
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        String suffix = ":" + testName + "]";
        Map<String, String> expected = allTests.entrySet().stream()
                .filter(map -> map.getKey().endsWith(suffix))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        //return tests
        Set<String> keySet = expected.keySet().stream()
                .map(p -> p.substring(0, p.length()-suffix.length())+"]")
                .collect(Collectors.toSet());
        String[] keys = keySet.toArray(new String[keySet.size()]);
        Arrays.sort(keys);

        String[][] tests = new String[keys.length][];
        for (int i = 0; i < keys.length; i ++) {
            String key = keys[i];
            tests[i] = new String[3];
            tests[i][0] = key;
            tests[i][1] = sqls.get(key);
            tests[i][2] = expected.get(key.substring(0, key.length()-1)+suffix);
        }

        return tests;
    }



    public void test_SQLTokenTree() {

        String[][] tests = loadTestCases("tree");

        for (int i = 0; i < tests.length; i ++) {

            String key = tests[i][0];
            String sql = tests[i][1];
            String expected = tests[i][2];

            logger.info(String.format("%s", key));

//            if (!key.startsWith("[sql-030:")) { continue; }

            Node node = new SqlParser().parse(sql);

            String actual = node.toTreeString();

            logger.info(String.format("\n%s\n[%s]\n %s\n[%s]",
                    sql.replaceAll("(\r|\n)+", " "),
                    key.substring(1, key.length()-1),
                    actual,
                    key.substring(1, key.length()-1)));
            Assert.assertEquals(expected.toString().trim(), actual.toString().trim());
        }
    }

    /**
     * - 获取数据表
     *
     */
    public void test_数据表() {

        String[][] tests = loadTestCases("find-all-tables");

        for (int i = 0; i < tests.length; i ++) {

            String key = tests[i][0];
            String sql = tests[i][1];
            String expected = "[" + tests[i][2] + "]";

            logger.info(String.format("%s", key));

            Node node = new SqlParser().parse(sql);

            String tree = node.toTreeString();

            final List<String> tables = new ArrayList<String>();

            node.getAllChildren().stream()
                    .filter(t -> {
                        Node n = (Node)t;
                        return n.toString().equals("`NAME`")
                                && n.getParent().toString().equals("`TABLE`")
                                && !n.getParent().getChildren().get(0).toString().equals("`STATEMENT`");
                    })
                    .forEach(n -> {
                        Node t = (Node)n;
                        Node tableNameNode = (Node)t.getChildren().get(0);
                        if (!tableNameNode.toString().equals("`STATEMENT`")) {
                            tables.add(tableNameNode.toString());
                        }
                    });

            String actual = tables.toString();


            logger.info(String.format("\n%s\n[%s]\n %s\n[%s]",
                    sql.replaceAll("(\r|\n)+", " "),
                    key.substring(1, key.length()-1),
                    actual,
                    key.substring(1, key.length()-1)));
            Assert.assertEquals(expected.toString(), actual.toString());
        }
    }

    public void test_查询语句的返回列() {

        String[][] tests = loadTestCases("result-columns");

        for (int i = 0; i < tests.length; i ++) {

            String key = tests[i][0];
            String sql = tests[i][1];
            String expected = "[" + tests[i][2] + "]";

            logger.info(String.format("%s", key));

            Node node = new SqlParser().parse(sql);

            String tree = node.toTreeString();

            logger.info(String.format("\n%s\n %s",
                    sql.replaceAll("(\r|\n)+", " "),
                    tree));

            final List<String> columns = new ArrayList<String>();

            List<Node> nodes = node.getAllChildren();
            nodes.stream()
                    .filter(n -> {
                        Node t = (Node)n;
                        boolean bColumnName = t.toString().equals("`NAME`")
                                && t.getParent().toString().equals("`COLUMN`")
                                && t.getParent().getParent().getParent().getParent() == null;

                        boolean bColumnExpr = t.toString().equals("`EXPR`")
                                && t.getParent().toString().equals("`COLUMN`")
                                && t.getParent().getParent().getParent().getParent() == null
                                && t.getParent().getChildren().size() == 1;

                        return bColumnName || bColumnExpr;

                    })
                    .forEach(n -> {
                        Node t = (Node)n;
                        LinkedList<Node> children = t.getChildren();
                        Node lastNode = children.get(children.size()-1);
                        //TODO: bug, "a", "a as a", "f(x,y,z)": 第3个场景没有as，应该报错
                        columns.add(lastNode.toString());

                    });

            String actual = columns.toString();


            logger.info(String.format("\n%s\n[%s]\n %s\n[%s]",
                    sql.replaceAll("(\r|\n)+", " "),
                    key.substring(1, key.length()-1),
                    actual,
                    key.substring(1, key.length()-1)));
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
