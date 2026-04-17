<template>
  <div class="hello">
    <MenuCard ref="MenuCard"></MenuCard>
    <!-- <h1>{{ msg }}</h1> -->
    <!-- 轮播图 -->
    <div class="carousel-container">
      <el-carousel :interval="4000" type="card">
        <el-carousel-item v-for="item in images" :key="item.url">
          <img class="banner-img" :src="item.url" alt="无法加载图片" />
        </el-carousel-item>
      </el-carousel>
    </div>
    <!-- 快速导航 -->
    <div class="divider">
      <hr class="divider-line">
      <span class="divider-text">快速导航</span>
      <hr class="divider-line">
    </div>
    <!-- 导航按钮 -->
    <div class="button-container">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-md-3 mb-3">
            <router-link to="/3dView" class="btn btn-lg btn-circle btn-blue"  @click.native="handleItemClick('1')">{{ buttons[0].text }}</router-link>
          </div>
          <div class="col-md-3 mb-3">
            <router-link to="/2dView" class="btn btn-lg btn-circle btn-blue" @click.native="handleItemClick('1')">{{ buttons[1].text }}</router-link>
          </div>
          <div class="col-md-3 mb-3">
            <router-link to="/que" class="btn btn-lg btn-circle btn-blue" @click.native="handleItemClick('4')">{{ buttons[2].text }}</router-link>
          </div>
          <div class="col-md-3 mb-3">
            <router-link to="/articles" class="btn btn-lg btn-circle btn-blue" @click.native="handleItemClick('5')">{{ buttons[3].text }}</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MenuCard from '@/components/MenuCard.vue'
export default {
  components: {
    MenuCard
  },
  name: 'HelloHome',
  props: {
    msg: String
  },
  data () {
    return {
      images: [
      {
        url: require('../assets/article1.jpg')
      },
      {
        url: require('../assets/article2.jpg')
      },
      {
        url: require('../assets/article3.jpg')
      },
      {
        url: require('../assets/article4.jpg')
      }
      ],
      buttons: [
        { url: '/3dView', text: '3D大屏', color: 'btn-primary' },
        { url: '/2dView', text: '知识图谱', color: 'btn-secondary' },
        { url: '/que', text: '智能问答', color: 'btn-success' },
        { url: '/articles', text: '文章推荐', color: 'btn-danger' }
      ]
    }
  },
  methods:{
    handleItemClick(index) {
      console.log('click index-->',index)
      this.$root.indexNow = index
      // const clickedItem = this.NavigateItem.find(item => item.key === index.toString())
      // if (clickedItem) {
      //   if (clickedItem.path) {
      //     // 检查当前路由是否与要导航的路由相同
      //     if (this.$route.path !== clickedItem.path) {
      //       this.$router.push(clickedItem.path)
      //     } else {
      //       console.warn('尝试导航到当前路由位置:', clickedItem.path)
      //     }
      //   } else {
      //     console.log('执行其他操作...')
      //   }
      // }
    }
  }
}
</script>

<style scoped>
.hello {
  /*background-color: #182245;*/
  background: url(../assets/background2.gif);
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  background-attachment:fixed;
  height: 100vh;
  overflow:auto;
}
.banner-img {
  width: 100%;
}
.carousel-container{
  margin-top: 30px;
  padding-left: 20px;
  padding-right: 20px;
}
.divider {
  display: flex;
  align-items: center;
  margin: 20px;
}

.divider-line {
  flex-grow: 1;
  border: 1px solid white;
}

.divider-text {
  margin: 0 10px;
  color: white;
  font-size: 20px;
}
.el-carousel__item h3 {
    color: #475669;
    font-size: 14px;
    opacity: 0.75;
    line-height: 300px;
    margin: 0;
}

.el-carousel__item:nth-child(2n) {
    background-color: #99a9bf;
}

.el-carousel__item:nth-child(2n+1) {
    background-color: #d3dce6;
}
.button-container .row {
  display: flex; /* 让按钮水平排列 */
}

.button-container .col-md-3 {
  flex: 1; /* 让每个按钮均匀占据横向空间 */
  text-align: center; /* 文本水平居中 */
}

.btn-circle {
  display: inline-block;
  border-radius: 50%;
  width: 130px;
  height: 130px;
  line-height: 130px;
  text-align: center;
  font-size: 20px;
  margin: 20px;
  text-decoration: none;
}

.btn-blue {
  width: 130px;
  height: 130px;
  background: url("../assets/blue.png");
  /* 背景图垂直、水平均居中 */
  background-position: center center;
  background-size: 100% 100%;
  color: white;
  font-weight: bold;
}

</style>
