<template>
  <div class="container">
    <MenuCard ref="MenuCard"></MenuCard>
    <div class="gContainer">

      <!-- <d3graph /> -->
      <gSearch @getData="update" />
      <d3graph :data="data" :names="names" :labels="labels" :linkTypes="linkTypes" />
    </div>
  </div>
</template>

<script>
import gSearch from '@/components/gSearch.vue'
import d3graph from '@/components/d3graph.vue'
import MenuCard from '@/components/MenuCard.vue'
export default {
  components: {
    gSearch,
    d3graph,
    MenuCard
  },
  data() {
    return {
      // d3jsonParser()处理 json 后返回的结果
      data: {
        nodes: [],
        links: [],
        user: {
          userPassword: 1,
          username: 1,
        }
      },
      names: ['疾病', '医疗科目', '症状', '诊断检查项目','食物','药品','在售药品'],
      labels: ['Disease', 'Department', 'Symptom', 'Check','Food','Drug','Producer'],
      linkTypes: ['', 'belongs_to', 'has_symptom', 'need_check','recommand_eat','do_eat','drugs_of','no_eat','recommand_drug','acompany_with','common_drug']
    }
  },
  mounted() {
    // 在组件挂载后，开始加载数据
    // this.loadALL();
  },
  methods: {
    // 视图更新
    update(json) {
      console.log('update')
      console.log('解析前:',json)
      this.d3jsonParser(json)
    },
    /*eslint-disable*/
    // 解析json数据，主要负责数据的去重、标准化
    d3jsonParser(json) {
      const nodes = []
      const links = [] // 存放节点和关系
      const nodeSet = [] // 存放去重后nodes的id

      for (let item of json) {
        if (item.p.segments) {
          // console.log('item.p.segments',item.p.segments)
        for (let segment of item.p.segments) {
          // 重新更改data格式
          if (nodeSet.indexOf(segment.start.identity) == -1) {
            nodeSet.push(segment.start.identity)
            nodes.push({
              id: segment.start.identity,
              label: segment.start.labels[0],
              properties: segment.start.properties
            })
          }
          if (nodeSet.indexOf(segment.end.identity) == -1) {
            nodeSet.push(segment.end.identity)
            nodes.push({
              id: segment.end.identity,
              label: segment.end.labels[0],
              properties: segment.end.properties
            })
          }
          links.push({
            source: segment.relationship.start,
            target: segment.relationship.end,
            type: segment.relationship.type,
            properties: segment.relationship.properties
          })
        }
      }
      console.log(nodes)
      console.log(links)
      this.data = { nodes, links }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.container{
  //background: url(../assets/background2.gif);
  ///* 背景图垂直、水平均居中 */
  //background-position: center center;
  //background-size: 100% 100%;
  //background-attachment:fixed;
  background-color: #182245;
  height: 100vh;
  overflow:auto;
}
.gContainer {
  position: relative;
  //border: 2px #000 solid;
  //background-color: #9dadc1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  //height: 100vh;
}
</style>
