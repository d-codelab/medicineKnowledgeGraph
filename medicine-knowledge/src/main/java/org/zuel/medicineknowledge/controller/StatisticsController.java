package org.zuel.medicineknowledge.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zuel.medicineknowledge.common.BaseResponse;
import org.zuel.medicineknowledge.common.ResultUtils;
import org.zuel.medicineknowledge.constant.UrlConstant;
import org.zuel.medicineknowledge.model.domain.Articles;
import org.zuel.medicineknowledge.model.domain.Categories;
import org.zuel.medicineknowledge.model.vo.*;
import org.zuel.medicineknowledge.service.ArticlesService;
import org.zuel.medicineknowledge.service.CategoriesService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistics")
@Api(tags = "数据统计API", description = "提供网站数据统计相关的接口")
@Slf4j
public class StatisticsController {

    @Autowired
    ArticlesService articlesService;
    @Autowired
    CategoriesService categoriesService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @GetMapping("/admin/get")
    public BaseResponse<DataVO> getAnalysis(){
        // 查询数据库中的文章数据
        List<Articles> articlesList = articlesService.list();

        // 按照分类进行分组，并统计各个分类文章的数量
        Map<Integer, Long> articleCountsByCategory = articlesList.stream()
                .collect(Collectors.groupingBy(Articles::getCategoryId, Collectors.counting()));

        // 查询类别表以获取类别名称
        Map<Integer, String> categoryNameMap = categoriesService.list().stream()
                .collect(Collectors.toMap(Categories::getId, Categories::getName));

        // 将统计结果封装成相应的数据结构
        List<CategoryCount> categoryCounts = articleCountsByCategory.entrySet().stream()
                .map(entry -> new CategoryCount(categoryNameMap.get(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList());

        QueryWrapper<Articles> articlesQueryWrapper = new QueryWrapper<>();
        articlesQueryWrapper.orderByDesc("countv").last("limit 5");
        List<Articles> topArticles = articlesService.list(articlesQueryWrapper);
        //前五点击文章
        List<TopArticle> topArticleList = topArticles.stream()
                .map(article -> new TopArticle(article.getId().toString(), article.getTitle(), article.getCountv()))
                .collect(Collectors.toList());

        //从redis中获取pv、uv

        DataVO data = new DataVO();
        data.setArticlesByCategory(categoryCounts);
        data.setTop5Articles(topArticleList);
        // 获取近七日的日期列表
        List<String> dates = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentDate.minusDays(i);
            dates.add(date.format(formatter));
        }

        // 获取近七日的 PV
        List<DateCount> pvLast7Days = new ArrayList<>();
        for (String date : dates) {
            String key = "pv:" + date;
            String pvValue = stringRedisTemplate.opsForValue().get(key);
            int pvCount = pvValue != null ? Integer.parseInt(pvValue) : 0;
            pvLast7Days.add(new DateCount(date, pvCount));
        }
        data.setPvLast7Days(pvLast7Days);

        // 获取近七日的 UV
        List<DateCount> uvLast7Days = new ArrayList<>();
        for (String date : dates) {
            String key = "uv:" + date;
            Long uvCount = stringRedisTemplate.opsForHyperLogLog().size(key);
            uvLast7Days.add(new DateCount(date, uvCount != null ? uvCount.intValue() : 0));
        }
        data.setUvLast7Days(uvLast7Days);
        //获取page统计
        // 获取近七日各个页面的统计数据
        List<String> pageTypes = Arrays.asList(UrlConstant.ARTICLE_PAGE, UrlConstant.AI_PAGE, UrlConstant.NODES_PAGE, UrlConstant.HOME_PAGE);

        ArrayList<PageViewsData> pageViewsDatas = new ArrayList<>();
        for (String pageType : pageTypes){
            PageViewsData pageViewsData = new PageViewsData();
            List<Integer> counts = new ArrayList<>();
            for (String date : dates) {
                String key = pageType + ":" + date;
                String value = stringRedisTemplate.opsForValue().get(key);
                int count = value != null ? Integer.parseInt(value) : 0;
                counts.add(count);
            }
            pageViewsData.setViews(counts);
            pageViewsData.setDays(dates);
            pageViewsData.setPage(pageType);
            pageViewsDatas.add(pageViewsData);
        }
        data.setPageViewsLast7Days(pageViewsDatas);
        return ResultUtils.success(data);
    }

}
