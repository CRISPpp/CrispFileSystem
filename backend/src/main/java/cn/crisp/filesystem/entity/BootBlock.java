package cn.crisp.filesystem.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Data
public class BootBlock implements Serializable {
    public static final long serialVersionUID = 6L;
    private ConcurrentHashMap<String, String> BootInfo;
    private ConcurrentHashMap<String, Integer> UserLimit;

    public BootBlock(){
        BootInfo = new ConcurrentHashMap<>();
        UserLimit = new ConcurrentHashMap<>();
        BootInfo.put("root", "root");
        BootInfo.put("userOne", "userOne");
        BootInfo.put("userTwo", "userTwo");
        BootInfo.put("userThree", "userThree");
        UserLimit.put("root", 0);
        UserLimit.put("userOne", 0);
        UserLimit.put("userTwo", 1);
        UserLimit.put("userThree", 2);
    }
}
