package me.tony9.util.tree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class NodeList<T> implements Iterable<Node<T>> {

    private LinkedList<Node<T>> nodes;

    public NodeList() {
        this.nodes = new LinkedList<>();
    }

    public void addFirst(Node<T> node) {
        this.nodes.addFirst(node);
    }

    public void addLast(Node<T> node) {
        this.nodes.addLast(node);
    }

    public void addAll(List<? extends Node<T>> nodes) {
        if (nodes != null) {
            for (Node node: nodes) this.addLast(node);
        }
    }

    public int size() {
        return this.nodes.size();
    }

    public int indexOf(Node<T> node) {
        return this.nodes.indexOf(node);
    }

    public Node get(int i) {
        return this.nodes.get(i);
    }

    @Override
    public Iterator<Node<T>> iterator() {
        return this.nodes.iterator();
    }
}
