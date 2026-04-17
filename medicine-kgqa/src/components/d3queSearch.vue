<template>
  <div style="margin-top: 20px;width: 500px;">
  </div>
</template>

<script>
import { queNode } from '@/api/api'
export default {
  props: {
    userInput: String
  },
  name: 'd3queSearch',
  data() {
    return {
      input: '',
      mode: '1',
      // 后台请求到的json数据
      // data: require('../data/staMedicine.json'),
      data :[],
      results: []
    }
  },
  created (){
    // queNode(this.userInput).then(response => {
    //         console.log('search-->')
    //         if (response) {
    //             console.log(response.data)
    //             return response.data;
    //         } else {
    //             return require('../data/staMedicine.json')
    //         }
    //     })
    //         .then(data => {
    //             this.data = data;
    //             this.$emit('getData', this.data);
    //         })
    //         .catch(error => {
    //             console.error('Failed to fetch data from the backend:', error);
    //             this.data = require('../data/staMedicine.json')
    //             this.$emit('getData', this.data)
    //         })
  },
  mounted() {
    this.$emit('getData', this.data)
    this.results = this.loadAll()
  },
  methods: {
    query(message) {
      // console.log(typeof this.mode)
      // if (this.data.length <= 20) {
      //     this.data = require('../data/staMedicine.json')
      // } else {
      //     this.data = require('../data/staMedicine.json')
      // }
      console.log('query')
      queNode(message).then(response => {
        console.log('search-->')
        if (response) {
          console.log('queResponse:',response.data)
          return response.data;
        } else {
          return require('../data/top5test.json')
        }
      })
        .then(data => {
          this.data = data;
          this.$emit('getData', this.data);
        })
        .catch(error => {
          console.error('Failed to fetch data from the backend:', error);
          this.data = require('../data/top5test.json')
          this.$emit('getData', this.data)
        })
    },
    querySearch(queryString, cb) {
      var res = this.results
      var results = queryString ? res.filter(this.createFilter(queryString)) : res
      // 调用 callback 返回建议列表的数据
      cb(results)
    },
    createFilter(queryString) {
      return (res) => {
        return (res.value.toLowerCase().indexOf(queryString.toLowerCase()) !== -1)
      }
    },
    // 模拟加载数据
    loadAll() {
      return []
    },
    handleSelect(item) {
      console.log(item)
    }
  }
}
</script>

<style lang='scss' scoped>
.el-select {
  width: 120px;
  // background-color: #fff;
}

.input-with-select .el-input-group__prepend {
  background-color: #6ecbf3;
}
</style>
