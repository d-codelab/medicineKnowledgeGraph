<template>
  <div>
    <MenuCard ref="MenuCard"></MenuCard>
    <div class="gContainer">
      <gSearch @getData="update" />
      <!-- <threeGraph /> -->
      <threeGraph
        :data="data"
        :names="names"
        :labels="labels"
        :linkTypes="linkTypes"
      />
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
import gSearch from '@/components/gSearch.vue'
import threeGraph from '@/components/threeGraph.vue'
import MenuCard from '@/components/MenuCard.vue'

export default {
  name: 'threeView',
  components: {
    gSearch,
    threeGraph,
    MenuCard
  },
  data () {
    return {
      // d3jsonParser()处理 json 后返回的结果
      data: {
        nodes: [],
        links: []
      },
      names: ['疾病', '医疗科目', '症状', '诊断检查项目','食物','药品','在售药品'],
      labels: ['Disease', 'Department', 'Symptom', 'Check','Food','Drug','Producer'],
      linkTypes: ['', 'belongs_to', 'has_symptom', 'need_check','recommand_eat','do_eat','drugs_of','no_eat','recommand_drug','acompany_with','common_drug']
    }
  },
  methods: {
    // 视图更新
    update (json) {
      console.log('update')
      console.log(json)
      this.d3jsonParser(json)
    },
    /*eslint-disable*/
    // 解析json数据，主要负责数据的去重、标准化
    d3jsonParser (json) {
      const nodes =[]
      const links = [] // 存放节点和关系
      const nodeSet = [] // 存放去重后nodes的id

      // 使用vue直接通过require获取本地json，不再需要使用d3.json获取数据
      // d3.json('./../data/records.json', function (error, data) {
      //   if (error) throw error
      //   graph = data
      //   console.log(graph[0].p)
      // })

      for (let item of json) {
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
      // this.links = links
      // this.nodes = nodes
      this.data = { nodes, links }
      // return { nodes, links }
    }
  }
}
</script>

<style lang="scss" scoped>
.gContainer {
  position: relative;
  //border: 2px #000 solid;
  //background-color: #9dadc1;
  background-color: #182245;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  min-height: 100vh;
}
</style>
