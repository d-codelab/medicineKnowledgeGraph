<template>
  <div class="article-recommend">
    <MenuCard ref="MenuCard"></MenuCard>
    <!-- 搜索框 -->
    <div class="search-box">
      <el-input placeholder="请输入搜索内容" v-model="searchText" class="search-input"></el-input>
      <el-button type="primary" icon="el-icon-search" @click="search">搜索</el-button>
    </div>
    <!-- 分类标签 -->
    <div class="category-buttons">
      <span>类别：</span>
      <el-button
        v-for="(tag, index) in tags"
        :key="index"
        size="small"
        round
        @click="filterArticles(tag.categoryId)"
      >
        {{ tag.name }}
      </el-button>
      <!-- 全部 -->
      <el-button
        size="small"
        round
        checked
        @click="clearFilters"
      >
        全部
      </el-button>
    </div>
    <!-- 精选文章板块 -->
    <div class="featured-articles">
      <h2 class="section-title">精选文章</h2>
      <div class="article-list">
        <el-card v-for="(article, index) in articles" :key="index" class="article" shadow="hover"
                 @click.native="itemclick(article)">
          <img :src="article.picture" class="article-image" />
          <div class="article-summary">{{ article.title }}</div>
          <div class="article-details">
            <div class="time">发布时间：{{ formatDateTime(article.updatedAt) }}</div>
            <div class="read-more">
              <a class="detail-link" @click="itemclick(article)">详情 >></a>
            </div>
          </div>
        </el-card>
      </div>
      <!-- 分页器 -->
      <el-pagination class="pagination" :current-page="currentPage" :page-size="pageSize" :total="totalArticles"
            @current-change="handleCurrentChange" background layout="prev, pager, next"  :hide-on-single-page="true">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import MenuCard from '@/components/MenuCard.vue'
import { fetchArticles } from '@/api/api'
import {fetchArticleTag} from "../api/api";
import {Message} from "element-ui";
export default {
  components: {
    MenuCard
  },
  name: 'ArticleRecommend',
  data() {
    return {
      searchText: '',
      tags: [],
      articles:[],
      currentPage: 1,
      pageSize: 8, // 每页显示的文章数量
      totalArticles: 10 // 总文章数
    }
  },
  created() {
    const query = {
      current: this.currentPage,
      pageSize: this.pageSize,
    };
    fetchArticles(query).then(res => {
      this.articles = res.data || [];
      // this.totalArticles = res.data.length || 0;
      console.log('res-->', res)
      console.log('length',this.totalArticles)
    })
    fetchArticleTag().then(res => {
      this.tags = res.data || [];
      console.log('tags-->',this.tags)
    });
  },
  methods: {
    clearFilters() {
      this.searchText = '';
      this.currentPage = 1;
      this.fetchArticles();
    },
    formatDateTime(dateTimeString) {
      const date = new Date(dateTimeString);
      if (isNaN(date.getTime())) {
        // 如果解析失败，返回原始字符串或其他默认值
        return dateTimeString;
      }
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0'); // getMonth() 是从 0 开始的
      const day = String(date.getDate()).padStart(2, '0');
      const hours = String(date.getHours()).padStart(2, '0');
      const minutes = String(date.getMinutes()).padStart(2, '0');
      return `${year}-${month}-${day} ${hours}:${minutes}`;
    },
    // 点击分页时更新文章列表
    handleCurrentChange(val) {
      this.loading = true;
      Message({
        message: '加载中，请稍等！',
        type: 'info'
      });
      this.currentPage = val;
      fetchArticles({ current: this.currentPage, pageSize: this.pageSize }).then(res => {
        this.article = res.article;
        console.log('handleCurrentChange');
        console.log(res)
        this.articles = res.data || [];
      })
    },
    // 跳转到文章详情页
    itemclick(article) {
      this.$router.push({
        path: `/articleDetail/${article.id}`,
      });
    },
    // 文章搜索
    search() {
      this.loading = true;
      fetchArticles({ searchText: this.searchText }).then(res => {

        this.articles = res.data;
        // 更新总文章数
        this.totalArticles = this.articles.length;
        console.log('Search results:', res);
      }).catch(error => {
        console.error('Error fetching articles:', error);
      }).finally(() => {
        this.loading = false;
      });
    },
    // 标签分类查询
    filterArticles(categoryId) {
      this.loading = true;
      Message({
        message: '加载中，请稍等！',
        type: 'info'
      });
      this.currentPage = 1
      fetchArticles({ current: this.currentPage, pageSize: this.pageSize, categoryId: categoryId }).then(res => {
        this.articles = res.data;
        // 更新总文章数
        this.totalArticles = this.articles.length;
        console.log('Search results:', res);
        console.log('length-->',this.totalArticles)
      }).catch(error => {
        console.error('Error fetching articles:', error);
      }).finally(() => {
        // 结束加载状态
        this.loading = false;
      });
    }
  },
  mounted() {
    // this.fetchArticles(); // 组件挂载时获取文章数据
  }
}
</script>

<style scoped>
.article-recommend {
  background: url(../assets/background2.gif);
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  background-attachment:fixed;
  height: 100vh;
  overflow:auto;
}

.search-box {
  text-align: center;
  margin-bottom: 20px;
  margin-top: 20px;
}

.search-input {
  width: 500px;
  margin-right: 10px;
}

/* 调整搜索按钮样式 */
.el-button--primary {
  background-color: #409EFF;
  border-color: #409EFF;
}

.el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}

.category-buttons {
  display: flex; /* 让按钮横向排列 */
  align-items: center; /* 垂直居中 */
  justify-content: center;
}

.section-title {
  text-align: left;
  margin-bottom: 20px;
  font-size: 15px;
  color: #fff;
  padding-left: 10px;
}

.featured-articles {
  padding: 10px;
}

.article-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start;
  gap: 0.5%;
}

.article .el-card__body {
  padding: 0px !important;
}

.article {
  width: calc(25% - 10px);
  margin-bottom: 20px;
  background-color: rgba(206, 206, 206, 0.5);
  border: none;

}

.article .el-card__body {
  padding: 0;
}

.article-image {
  width: 100%;
  height: 130px;
  border-radius: 5px;
}

.article-summary {
  margin-top: 10px;
  color: white;
  text-align: left;
  font-size: 15px;
}

.article-details {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
}

.time {
  color: #ffff;
}

.likes {
  color: #ffff;
}

.likes i {
  margin-left: 65px;
}

.read-more {
  text-align: right;
  flex: 1;
  color: #ffff;
}

.detail-link {
  color: white;
  text-decoration: none;
  font-size: 13px;
  cursor: pointer;
}

.detail-link:hover {
  text-decoration: underline;
}

/* 分页器样式 */
.el-pagination {
  text-align: center;
  margin-top: 20px;
}

/* 分页器按钮默认背景色 */
.el-pagination__item:not(.is-active) {
  background-color: #2242CF;
  color: #fff;
  /* 将按钮文字颜色设为白色 */
}

/* 分页器按钮悬停和聚焦时的背景色 */
.el-pagination__item:not(.is-active):hover,
.el-pagination__item:not(.is-active):focus {
  background-color: rgba(34, 66, 207, 0.7);
  /* 设置70%透明度的蓝色 */
}

.el-pagination__jump,
.el-pagination__page {
  color: #fff;
  /* 将 "前往" 和 "页" 文字颜色设为白色 */
}
</style>
