package org.zuel.medicineknowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.zuel.medicineknowledge.model.domain.Categories;
import com.baomidou.mybatisplus.extension.service.IService;
import org.zuel.medicineknowledge.model.dto.category.CategoryQueryRequest;

/**
* @author mumu
* @description 针对表【categories】的数据库操作Service
* @createDate 2024-04-23 21:06:28
*/
public interface CategoriesService extends IService<Categories> {

    boolean validCategories(Categories post, boolean add);

    QueryWrapper<Categories> getQueryWrapper(CategoryQueryRequest categoriesQueryRequest);
}
