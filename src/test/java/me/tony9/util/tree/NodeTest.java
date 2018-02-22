package me.tony9.util.tree;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NodeTest extends TestCase {

    private static Log logger = LogFactory.getLog(NodeTest.class);

    private Map<String, String> loadTestCases() {

        try {
            //load test cases
            URL url = NodeTest.class.getResource("/test-case/NoteTest.txt");
            String file = url.getPath();
            BufferedReader reader = new BufferedReader(new FileReader(file));

            Map<String, String> expects = new HashMap<>();
            String testCaseName = null;
            StringBuffer str = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("#")) {
                    //comments, do nothing
                } else if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                    if (testCaseName != null) {
                        if (expects.containsKey(testCaseName)) {
                            throw new RuntimeException("Duplicated Test Case Name!");
                        }
                        expects.put(testCaseName, str.toString().trim());
                        str = new StringBuffer();
                    }
                    testCaseName = line.trim();
                } else {
                    str.append(System.getProperty("line.separator")).append(line);
                }
            }
            expects.put(testCaseName, str.toString().trim());

            return expects;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test cases!", e);
        }

    }

    public void test_toTreeString() {

        Map<String, String> expects = loadTestCases();

        //check results
        Node<String> root = new Node(".");
        Assert.assertEquals(expects.get("[new root]"), root.toTreeString().trim());

        Node<String> a = new Node("a");
        root.addLast(a);
        Assert.assertEquals(expects.get("[add a to root]"), root.toTreeString().trim());

        Node<String> b = new Node("b");
        root.addLast(b);
        Assert.assertEquals(expects.get("[add b to root]"), root.toTreeString().trim());

        Node<String> a1 = new Node("a1");
        a.addLast(a1);
        Assert.assertEquals(expects.get("[add a1 to a]"), root.toTreeString().trim());

        Node<String> a2 = new Node("a2");
        a.addLast(a2);
        Assert.assertEquals(expects.get("[add a2 to a]"), root.toTreeString().trim());

        Node<String> a21 = new Node("a21");
        a2.addLast(a21);
        Assert.assertEquals(expects.get("[add a21 to a2]"), root.toTreeString().trim());

        Node<String> a211 = new Node("a211");
        a21.addLast(a211);
        Assert.assertEquals(expects.get("[add a211 to a21]"), root.toTreeString().trim());

        Node<String> b1 = new Node("b1");
        b.addLast(b1);
        Assert.assertEquals(expects.get("[add b1 to b]"), root.toTreeString().trim());

    }
}
