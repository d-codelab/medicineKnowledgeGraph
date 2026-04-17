<template>
  <div style="margin-top: 4px;width: 500px;">
<!--    <el-autocomplete style="width: 500px;padding-bottom: 10px;" class="inline-input" v-model="input"-->
<!--                     placeholder="请输入希望查询的实体名称，支持模糊匹配" :trigger-on-focus="false" clearable>-->
<!--      &lt;!&ndash; <el-select-->
<!--        v-model="mode"-->
<!--        slot="prepend"-->
<!--        placeholder="关键字查询"-->
<!--      >-->
<!--        <el-option label="关键字查询" value="1"></el-option>-->
<!--        <el-option label="单实体查询" value="2"></el-option>-->
<!--        <el-option label="关联查询" value="3"></el-option>-->
<!--      </el-select> &ndash;&gt;-->
<!--      <el-button slot="append" type="success" icon="el-icon-search" @click="query">搜索</el-button>-->
<!--    </el-autocomplete>-->
    <div class="query">
      <el-input v-model="input" placeholder="请输入希望查询的实体名称，支持模糊匹配"></el-input>
      <el-button type="primary" @click="query">发送</el-button>
    </div>
    <strong style="font-size: 14px;">提示：2D视图下选中节点，单击右键探索更多功能！</strong>
  </div>
</template>

<script>
import { getGraph, searchNode } from '@/api/api'
import {Message} from "element-ui";
export default {
  name: 'gSearch',
  data() {
    return {
      input: '',
      mode: '1',
      // data: require('../data/recordsTest.json'),
      data: '',
      results: []
    }
  },
  mounted() {
    // 在组件挂载时调用方法从后端获取数据
    this.loadData();
  //   this.loadData().then(() => {
  //   // 数据加载完成后再传递给父组件
  //   this.$emit('getData', this.data);
  // });
  },
  methods: {
    query () {
      // console.log(typeof this.mode)
      // if (this.data.length <= 20) {
      //   this.data = require('../data/top5test.json')
      // } else {
      //   this.data = require('../data/recordsTest.json')
      // }
      console.log('query')
      searchNode(this.input).then(response => {
        console.log('search-->')
        if(response){
          console.log(response.data)
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
    loadData() {
      Message({
        message: '加载中，请稍等！',
        type: 'info'
      });
      getGraph().then(response => {
        console.log('res:',response);
        if(response){
          // this.data = data;
          // this.$emit('getData', this.data);
          return response.data;
        } else {
          return require('../data/recordsTest.json')
        }
    })
    .then(data => {
      this.data = data;
      this.$emit('getData', this.data);
      // console.log('data:',this.data);
    })
    .catch(error => {
      console.error('Failed to fetch data from the backend:', error);
      this.data = require('../data/recordsTest.json')
      this.$emit('getData', this.data)
    })
    // .finally(() => {
    //   this.isLoading = false;
    // });
    },
    querySearch(queryString, cb) {
      // 在这里使用 this.results 进行搜索逻辑
      var results = queryString ? this.results.filter(this.createFilter(queryString)) : this.results;
      // 调用 callback 返回建议列表的数据
      cb(results);
    },
    createFilter(queryString) {
      return (res) => {
        return res.value.toLowerCase().indexOf(queryString.toLowerCase()) !== -1;
      }
    },
    // handleSelect(item) {
    //   console.log(item);
    // }
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
.query{
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-bottom: 10px;
}
</style>
