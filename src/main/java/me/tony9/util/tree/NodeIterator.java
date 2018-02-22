package me.tony9.util.tree;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NodeIterator {

    private static Log logger = LogFactory.getLog(NodeIterator.class);

//    private static <R> void iterate(Node root, Function<Node, R> fn) {
//        NodeList nodes = root.getChildren();
//        if (nodes != null) {
//            for (int i = 0; i < nodes.size(); i ++) {
//                Node node = nodes.get(i);
//                iterate(node, fn);
//            }
//        }
//
//        fn.apply(root);
//    }
//
//    /**
//     * 按前序返回符合要求的Node
//     * @param root
//     * @param fn
//     * @param <R>
//     * @return
//     */
//    private static <R> List<Node> findAll(Node root, Predicate<Node> fn) {
//
//        List<Node> r = new ArrayList<>();
//
//        if (fn.test(root)) {
//            r.add(root);
//        }
//
//        NodeList nodes = root.getChildren();
//        if (nodes != null) {
//            for (int i = 0; i < nodes.size(); i ++) {
//                Node node = nodes.get(i);
//                r.addAll(findAll(node, fn));
//            }
//        }
//
//        return r;
//    }
//
//    private static boolean match(Node node, String pattern) {
//        return node.toString().equals(pattern);
//    }
//
//    /**
//     * match("/"): root
//     * match("/A/B"): B是A的子孙节点
//     * match("A/\*\*\/B"): B是A的直接子节点
//     * match("A :variable"):
//     * @param root
//     * @param pattern
//     * @return
//     */
//    public static List<Node> findAll(Node root, String pattern) {
//        return findAll(root, new String[] {pattern});
//    }
//
//    private static List<Node> findAll(Node root, String[] pattern) {
//
//        List<Node> r = new ArrayList<>();
//        if (pattern.length == 0) {
//            r.add(root);
//            return r;
//        } else if (pattern.length == 1) {
//            r =  findAll(root, node -> { return match(node, pattern[0]); });
//            return r;
//        } else {
//            List<Node> nodes = findAll(root, node -> { return match(node, pattern[0]); });
//            String[] newPattern = new String[pattern.length-1];
//            System.arraycopy(pattern, 1, newPattern, 0, newPattern.length-1);
//            for (Node node: nodes) {
//                List<Node> t = findAll(node, newPattern);
//                r.addAll(t);
//            }
//            return r;
//        }
//
//     }
}
