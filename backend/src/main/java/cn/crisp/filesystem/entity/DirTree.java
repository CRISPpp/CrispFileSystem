package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
