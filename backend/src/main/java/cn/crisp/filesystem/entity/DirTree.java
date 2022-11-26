package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 	链表结构，主要属性有：
 * 		inode：代表目录树这个结点对应的i结点。
 * 		next：为目录树子节点数组，存储下一层的信息。
 * 		parent：目录树结点的父节点
 */
@AllArgsConstructor
@Data
public class DirTree implements Serializable {
    public static final long serialVersionUID = 9L;
    private Inode inode;
    private List<DirTree> next;
    private DirTree parent = null;

    public DirTree() {
        inode = new Inode();
        next = new ArrayList<>();
        parent = null;
    }

    public String toString() {
        return "inode: " + inode.toString() + "      next: " + next.toString();
    }

}
