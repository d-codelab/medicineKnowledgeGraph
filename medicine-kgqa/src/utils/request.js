import axios from "axios";
import qs from "qs";
import { Message } from "element-ui";
import router from "../router";
import store from "../store";

// 自定义的transformRequest函数，根据请求方法决定转换方式
function customTransformRequest(data, method) {
  if (method === "post" || method === "put" || method === "patch") {
    // POST请求使用JSON.stringify
    return JSON.stringify(data);
  } else if (method === "get" || method === "delete") {
    // GET请求使用qs.stringify
    return qs.stringify(data);
  }
  // 其他方法或未指定方法时，不转换数据
  return data;
}

//导出request方法，供其它地方使用
export function request(config) {
  const instance = axios.create({
    baseURL: "http://localhost:8090/",
    timeout: 300000,
    // 允许在向服务器发送前，修改请求数据
    transformRequest: [
      function (data) {
        // 对 data 做序列化处理
        return JSON.stringify(data);
      },
    ],
  });

  instance.defaults.headers.post["Content-Type"] = "application/json";

  //------------------请求拦截-------------------//
  instance.interceptors.request.use(
    (config) => {
      // 在请求拦截器中转换请求数据
      // config.data = customTransformRequest(config.data, config.method);
      // 若存在token则带token
      // const token = store.state.token;
      const token = store.state.token || sessionStorage.getItem("token");
      if (token) {
        config.headers.Authorization = token;
      }
      // 放行
      return config;
    },
    (err) => {
      console.log("请求拦截=>", err);
      return err;
    }
  );

  //------------------响应拦截-------------------//
  instance.interceptors.response.use(
    (res) => {
      //后端数据处理错误，并返回错误原因；前端获取错误原因并展示
      console.log("响应拦截=>", res);
      if (res.data.success === false || res.data.data == null) {
        Message({
          message: res.data.message + "，请重试！",
          type: "warning",
        });
      }
      return res ? res.data : res;
    },
    (err) => {
      console.log("响应拦截错误完整信息=>", err);
      // Message({
      //   message: '加载中！',
      //   type: 'info'
      // });
      //也可以在这里对不同的错误码做不同的展示处理
      if (err.response.status === 401) {
        store.commit("setToken", null);
        // 跳转到首页
        router.push({
          path: "/login",
        });
      }
      throw err;
    }
  );
  return instance(config);
}
