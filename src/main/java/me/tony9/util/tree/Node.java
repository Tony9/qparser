package me.tony9.util.tree;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {

    private Node<T> parent;
    private NodeList<T> children;
    private T data;

    public Node() {
        this.children = new NodeList<T>();
        this.data = null;
    }

    public Node(T data) {
        this.children = new NodeList<T>();
        this.data = data;
    }

    public NodeList<T> getChildren() {
        return children;
    }

    public void addFirst(Node<T> node) {
        node.parent = this;
        this.children.addFirst(node);
    }

    public void addLast(Node<T> node) {
        node.parent = this;
        this.children.addLast(node);
    }

    public void addAll(List<? extends Node<T>> nodes) {
        if (nodes != null) {
            for (Node node: nodes) this.addLast(node);
        }
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public String toString() {
        String t = (this.data == null)? "nil":this.data.toString();
        return t;
    }

    public String toTreeString() {
        return NodeFormat.toTreeString(this, this).toString();
    }

    /**
     * 返回所有子孙节点
、     * @return
     */
    public List<Node> getAllChildren() {

        ArrayList<Node> nodes = new ArrayList<>();

        nodes.add(this);
        int index = -1;

        while (index < nodes.size()-1) {
            index ++;
            Node node = nodes.get(index);
            NodeList children = node.getChildren();
            for (int i = 0; i < children.size(); i ++) {
                nodes.add(children.get(i));
            }
        }


        return nodes;
    }


}
