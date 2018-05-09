package com.rmxue.concurrent.datamodel;

/**
 * Copyright: Copyright (c) 2018 Asiainfo
 *
 * @ClassName: com.rmxue.concurrent.datamodel.BinaryTree
 * @Description: 该类的功能描述
 * @version: v1.0.0
 * @author: xuecy
 * @date: 2018/4/23 17:01
 * <p>
 * Modification History:
 * Date         Author          Version            Description
 * ------------------------------------------------------------
 * 2018/4/23      xuecy           v1.0.0               修改原因
 */
/*
 * 二叉树
 * */
public class BinaryTree {
    public BinaryTree left; //左节点

    public BinaryTree right; //右节点

    public String data;  //树的内容

    public BinaryTree() {
    }

    /**
     * 构造方法
     *
     * @param data
     * @param left
     * @param right
     */
    public BinaryTree(String data, BinaryTree left, BinaryTree right) {
        this.left = left;
        this.right = right;
        this.data = data;
    }

    /**
     * 构造方法
     *
     * @param data
     */
    public BinaryTree(String data) {
        this(data, null, null);
    }

    public BinaryTree getLeft() {
        return left;
    }

    public void setLeft(BinaryTree left) {
        this.left = left;
    }

    public BinaryTree getRight() {
        return right;
    }

    public void setRight(BinaryTree right) {
        this.right = right;
    }

    /**
     * 插入节点 ，如果当前的节点没有左节点，我们就创建一个新节点，然后将其设置为当前节点的左节点。
     *
     * @param node
     * @param value
     */
    public static void insertLeft(BinaryTree node, String value) {
        if (node != null) {
            if (node.left == null) {
                node.setLeft(new BinaryTree(value));
            } else {
                BinaryTree newNode = new BinaryTree(value);
                newNode.left = node.left;
                node.left = newNode;
            }
        }
    }

    /**
     * 同插入左结点
     *
     * @param node
     * @param value
     */
    public static void insertRight(BinaryTree node, String value) {
        if (node != null) {
            if (node.right == null) {
                node.setRight(new BinaryTree(value));
            } else {
                BinaryTree newNode = new BinaryTree(value);
                newNode.right = node.right;
                node.right = newNode;
            }
        }
    }
}
