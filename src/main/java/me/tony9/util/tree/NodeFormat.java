package me.tony9.util.tree;

import java.util.LinkedList;

public class NodeFormat {

    /**
     * 按Tree风格，递归输出指定startNode的格式化字符串。
     * 这里, root必须是startNode的祖先节点。
     *
     * 比如， 指定root=null，输出整棵树：
     * .
     * ├──`SELECT`
     * ├──a
     * ├──`FROM`
     * ├──.
     * │  ├──(
     * │  ├──`SELECT`
     * │  ├──b
     * │  ├──`FROM`
     * │  ├──.
     * │  │  ├──(
     * │  │  ├──`SELECT`
     * │  │  ├──c
     * │  │  ├──`FROM`
     * │  │  ├──t1
     * │  │  └──)
     * │  ├──t2
     * │  └──)
     * └──t3
     * @param root root of Tree.
     * @param startNode
     * @return
     */
    public static StringBuffer toTreeString(Node root, Node startNode) {

        //calc parents
        LinkedList<Node> parents = new LinkedList<>();
        Node currNode = startNode;

        while (true) {

            if (currNode == null) {
                if (root == null) {
                    break;
                } else {
                    throw new RuntimeException("Internal Error: root is NOT a parent of statement");
                }
            } else {
                parents.addFirst(currNode);
            }

            if (currNode == root) {
                break;
            } else {
                currNode = currNode.getParent();
            }
        }

        //parents ==> prefix
        StringBuffer prefix = new StringBuffer();

        for (int i = 1; i < parents.size(); i ++) {
            Node p1 = parents.get(i-1);
            Node p2 = parents.get(i);
            int index = p1.getChildren().indexOf(p2);
            if (index > -1 && index < p1.getChildren().size()-1) {
                prefix.append(" │ ");
            } else {
                prefix.append("   ");
            }
        }

        // print children of startNode
        StringBuffer s = new StringBuffer();

        s.append(startNode.toString());
        s.append("\n");

        for (int i = 0; i < startNode.getChildren().size(); i ++) {

            Node node = startNode.getChildren().get(i);
            if (i < startNode.getChildren().size()-1) {
                s.append(prefix).append(" ├─ ");
            } else {
                s.append(prefix).append(" └─ ");
            }

            if (node.getChildren().size() > 0) {
                s.append((toTreeString(root, node)));  //递归
            } else {
                s.append(node.toString());
                s.append("\n");
            }

        }

        return s;
    }
}
