<template>
  <div class="article-detail">
    <div class="menu-nav">
      <MenuCard ref="MenuCard"></MenuCard>
    </div>
    <div class="detail-container">
      <div class="detail-box" v-if="article">
        <div style="display: flex;align-items: center;" @click="goBack">
          <span style="font-size: 18px;color: black;cursor:pointer;">⬅返回</span>
        </div>
          <h1 class="article-title">{{ article.title }}</h1>
        <img :src="article.picture" class="article-image" />
        <!-- 使用 v-html 渲染富文本内容 -->
        <div class="article-content" v-html="article.content"></div>
      </div>
      <div v-else>
        <p>Loading...</p>
      </div>
    </div>
  </div>
</template>

<script>
import MenuCard from '@/components/MenuCard.vue'
import { fetchArticleID } from '@/api/api'
export default {
  components: {
    MenuCard
  },
  name: 'ArticleDetail',
  data() {
    return {
      article: {
        // title: '促进晚期癌症患者预立照护计划实施的证据总结医疗',
        // picture: require('../assets/article1.jpg'),
        // summary: '促进晚期癌症患者预立照护计划实施的证据总结医疗',
        // time: '2024-04-08',
      }
    };
  },
  methods:{
    goBack(){
      this.$router.go(-1)
    }
  },
  created() {
    const article1 = this.article
    const articleId = this.$route.params.id;
    // 根据id获取后端数据
    fetchArticleID({ id: articleId }).then(res => {
      this.article = res.data;
    }).catch(e => {
      console.log('1111',article1)
      this.article = article1
    })
  }
};
</script>

<style>
.article-detail {
  background: url(../assets/background2.gif);
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  background-attachment:fixed;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: auto;
}

.menu-nav {
  width: 100%;
  background-color: rgba(24, 34, 69, 0.9);
}

.detail-container {
  width: 70%;
  margin: 20px auto;
  box-sizing: content-box;
}

.detail-box {
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.article-title {
  color: #000;
  font-size: 24px;
  margin-top: 4px;
}

.article-image {
  display: block;
  margin: 0px auto 20px auto;
  max-width: 80%;
  border-radius: 8px;
}

.article-content {
  color: #333;
  font-size: 16px;
  line-height: 1.6;
}
</style>
