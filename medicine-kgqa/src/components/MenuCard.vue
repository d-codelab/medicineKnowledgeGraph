<template>
  <div>
    <div class="navbar">
      <div>
        <span class="Menutitle">智医图说</span>
      </div>
      <el-menu class="el-menu-demo" background-color="#182245" text-color="#fff" active-text-color="#409EFF"
               mode="horizontal" :default-active="this.$root.indexNow">

        <el-menu-item index="2" @click="handleItemClick('2')">首页</el-menu-item>
        <el-menu-item index="1" @click="handleItemClick('1')">知识图谱</el-menu-item>
        <el-menu-item index="4" @click="handleItemClick('4')">智能问答</el-menu-item>
        <el-menu-item index="5" @click="handleItemClick('5')">文章推荐</el-menu-item>

        <!-- 用户图标及下拉菜单 -->
<!--        <el-submenu class="user-menu" index="user" style="float: right;">-->
<!--          <template slot="title">-->
<!--            <i class="el-icon-user"></i>-->
<!--          </template>-->
<!--          <el-menu-item index="userCenter">用户中心</el-menu-item>-->
<!--          &lt;!&ndash; 其他下拉菜单项 &ndash;&gt;-->
<!--        </el-submenu>-->
      </el-menu>


    </div>
  </div>
</template>

<script>
import { Menu, MenuItem, Submenu } from 'element-ui'
export default {
  components: {
    'el-menu': Menu,
    'el-menu-item': MenuItem,
    // 'el-submenu': Submenu
  },
  methods: {
    handleItemClick(index) {
      this.$root.indexNow = index
      const clickedItem = this.NavigateItem.find(item => item.key === index.toString())
      if (clickedItem) {
        if (clickedItem.path) {
          // 检查当前路由是否与要导航的路由相同
          if (this.$route.path !== clickedItem.path) {
            this.$router.push(clickedItem.path)
          } else {
            console.warn('尝试导航到当前路由位置:', clickedItem.path)
          }
        } else {
          console.log('执行其他操作...')
        }
      }
    }
  },
  data() {
    return {
      NavigateItem: [
        {
          title: '知识图谱',
          key: '1',
          path: '/2dView',
          icon: 'map',
          items: []
        },
        {
          title: '首页',
          key: '2',
          path: '/home',
          icon: 'home',
          items: []
        },
        {
          title: '智能问答',
          key: '4',
          path: '/que',
          icon: '',
          items: []
        },
        {
          title: '文章推荐',
          key: '5',
          path: '/articles',
          icon: 'read',
          items: []
        }
      ]
    }
  }
}
</script>

<style scoped>
.navbar {
  background-color: #182245;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0px 10px 20px;
}

.Menutitle {
  color: aliceblue;
  padding-left: 10px;
  font-size: 26px;
  font-weight: bold;
}

.el-menu {
  background-color: #182245;
  padding: 0 20px;
}
.el-menu-item {
  margin-right: 20px;
  font-size :15px;
}
.el-menu-demo {
  flex: 1;
}

.user-menu {
  color: white;
  font-size: 20px;
}

::v-deep .el-menu-item{
  font-size: 18px;
}
::v-deep .el-menu.el-menu--horizontal{
  border: none;
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .Menutitle {
    font-size: medium;
    /* 调整标题大小 */
  }

  .el-menu {
    padding: 0 10px;
  }
}
</style>
