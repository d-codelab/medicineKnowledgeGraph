package org.zuel.medicineknowledge.model.dto.articles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.zuel.medicineknowledge.common.PageRequest;

import java.io.Serializable;


/**
 * 查询请求
 *

 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 搜索词
     */
    private String searchText;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    private Integer categoryId;

    private static final long serialVersionUID = 1L;
}