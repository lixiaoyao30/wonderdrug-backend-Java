package org.wonder.wonderdrugs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

/**
 * 站点健康状态数据传输对象
 */
@Data                   // 自动生成 getter/setter/toString/equals/hashCode
@Builder               // 生成建造者模式代码
@NoArgsConstructor    // 生成无参构造函数
@AllArgsConstructor   // 生成全参构造函数
public class SiteHealthDTO {
    /** 站点ID */
    private String id;

    /** 站点名称 */
    private String name;

    /** 站点编号 */
    private String number;

    /** 站点状态 */
    private String status;

    /** 国家ID */
    private String countryId;

    /** 国家名称 */
    private String country;

    /** 健康评分 */
    private int healthScore;

    /** 健康状态描述 */
    private String healthStatus;

    /** 趋势 */
    private String trend;

    /** 问题列表 */
    private List<String> issues;

    /** 最后更新时间 */
    private Date lastUpdated;

    /** Vault URL */
    private String vaultUrl;
}
