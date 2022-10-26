package cn.crisp.filesystem.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HelpVo implements Serializable {
    public static final long serialVersionUID = 12L;
    private List<String> CMDList;
    private List<String> CMDDescription;
}
